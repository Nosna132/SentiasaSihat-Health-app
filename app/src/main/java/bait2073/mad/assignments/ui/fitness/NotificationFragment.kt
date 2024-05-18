package bait2073.mad.assignments.ui.fitness

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import bait2073.mad.assignments.NotificationWorker
import bait2073.mad.assignments.databinding.FragmentNotificationBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationFragment : Fragment() {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()
    private val database: FirebaseDatabase = Firebase.database
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe changes in selected time and update UI accordingly
        viewModel.selectedTime.observe(viewLifecycleOwner, Observer { time ->
            binding.selectedTime.text = time
        })

        // Fetch saved alarm time from Firebase database
        fetchSavedAlarmTime()

        binding.selectTimeBtn.setOnClickListener { showTimePicker() }
        binding.setAlarmBtn.setOnClickListener { setAlarm() }
        binding.cancelAlarmBtn.setOnClickListener { cancelAlarm() }
    }

    private fun fetchSavedAlarmTime() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val alarmRef = database.getReference("users").child(userId).child("alarms")
            alarmRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val alarmTime = snapshot.getValue(String::class.java)
                    alarmTime?.let {
                        viewModel.setSelectedTime(it)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun cancelAlarm() {
        // Cancel alarm work
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag("dailyWorkout")

        // Remove alarm time from Firebase
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val alarmRef = database.getReference("users").child(userId).child("alarms")
            alarmRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Alarm Cancelled", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to cancel alarm", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAlarm() {
        viewModel.selectedTime.value?.let { selectedTime ->
            // Save alarm time to Firebase
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val alarmRef = database.getReference("users").child(userId).child("alarms")
                alarmRef.setValue(selectedTime)
                Toast.makeText(requireContext(), "Alarm set Successfully", Toast.LENGTH_SHORT).show()

                // Schedule NotificationWorker for periodic work
                val dailyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
                    .addTag("dailyWorkout")
                    .build()

                WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                    "dailyWorkout",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    dailyWorkRequest
                )
            } else {
                Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Alarm Time")
            .build()
        picker.show(childFragmentManager, "SentiasaSihat")
        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute
            val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }.time
            )
            // Update view model with selected time
            viewModel.setSelectedTime(formattedTime)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}