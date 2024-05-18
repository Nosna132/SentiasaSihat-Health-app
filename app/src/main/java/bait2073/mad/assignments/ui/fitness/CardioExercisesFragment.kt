package bait2073.mad.assignments.ui.fitness

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import bait2073.mad.assignments.R
import bait2073.mad.assignments.database.UserExerciseData
import bait2073.mad.assignments.databinding.CardioExercisesFragmentBinding

class CardioExercisesFragment : Fragment() {

    private var _binding: CardioExercisesFragmentBinding? = null
    private val binding get() = _binding!!
    private var isVideoStarted = false

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CardioExercisesFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        // Load video from raw resources
        val videoUri = Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.cardio)

        // Set the video URI to the VideoView
        binding.CardioVideo.setVideoURI(videoUri)

        // Set OnClickListener to start/stop video playback when the user clicks on the VideoView
        binding.CardioVideo.setOnClickListener {
            if (!isVideoStarted) {
                // Start video playback if it hasn't been started
                binding.CardioVideo.start()
                isVideoStarted = true
            } else {
                // Pause video if it's already playing
                if (binding.CardioVideo.isPlaying) {
                    binding.CardioVideo.pause()
                } else {
                    // Resume video if it's paused
                    binding.CardioVideo.start()
                }
            }
        }

        // Set OnClickListener for the "Complete" button
        val completeButton: Button = binding.button
        completeButton.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                completeExercise(userId, "CardioExercise")
            } else {
                Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun completeExercise(userId: String, exerciseKey: String) {
        val userExerciseRef = databaseReference.child(userId).child("exerciseData").child(exerciseKey)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        userExerciseRef.get().addOnSuccessListener { dataSnapshot ->
            val userData = dataSnapshot.getValue(UserExerciseData::class.java)
            val lastCompletionDate = userData?.lastCompletionDate ?: ""

            if (lastCompletionDate != currentDate) {
                // Calculate calories burned and workout duration for the current session
                val caloriesBurnedSession = 500
                val workoutDurationSession = 10

                // Retrieve existing total calories burned and workout duration from the database
                var totalCaloriesBurned = userData?.caloriesBurned ?: 0
                var totalWorkoutDuration = userData?.workoutDuration ?: 0

                // Add current session values to existing totals
                totalCaloriesBurned += caloriesBurnedSession
                totalWorkoutDuration += workoutDurationSession

                // Update the database with new totals and last completion date
                userExerciseRef.child("caloriesBurned").setValue(totalCaloriesBurned)
                userExerciseRef.child("workoutDuration").setValue(totalWorkoutDuration)
                userExerciseRef.child("lastCompletionDate").setValue(currentDate)

                // Show a toast to notify the user
                Toast.makeText(requireContext(), "Exercise completed! Calories burned and workout duration updated!", Toast.LENGTH_SHORT).show()
            } else {
                // Show a toast to inform the user that they have already completed the exercise for the day
                Toast.makeText(requireContext(), "You've already completed this exercise today!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
