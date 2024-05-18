package bait2073.mad.assignments.ui.profile

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import bait2073.mad.assignments.R
import bait2073.mad.assignments.database.UserDB
import bait2073.mad.assignments.databinding.DialogInfoFitnessBinding
import bait2073.mad.assignments.databinding.FragmentProfileBinding
import bait2073.mad.assignments.databinding.MoodinfodesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var viewModel: MoodViewModel
    private val binding get() = _binding!!
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(MoodViewModel::class.java)
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val editProfileView: ImageView = binding.EditProfileView
        val infomood: ImageView = binding.moodinfo

        editProfileView.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profileFragment_to_edit_profile)
        }

        infomood.setOnClickListener {
            showInfoDialog()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val viewEmotion: ImageView = binding.btnEmotion
        val viewMoreInfo: ImageView = binding.MoreInfoView

        viewEmotion.setOnClickListener {
            val showPopup = Popup_mood_Fragment()
            showPopup.show((activity as AppCompatActivity).supportFragmentManager, "showPopup")
        }

        viewMoreInfo.setOnClickListener {
            val showPopup = More_Info_Fragment()
            showPopup.show((activity as AppCompatActivity).supportFragmentManager, "showPopup")
        }

        if (userId.isNotEmpty()) {
            databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userData = dataSnapshot.getValue(UserDB::class.java)
                    userData?.let {
                        binding.textViewProfileName.text = it.username
                        binding.textViewHeight.text = it.height.toString()
                        binding.textViewWeight.text = it.weight.toString()
                        calBMI(it.weight, it.height)
                        calculateAge(it.dateOfBirth)
                        setGenderIcon(it.gender)
                        loadProfilePictureFromStorage(it.profileImageName)

                        calculateAndDisplayTotalCalories(dataSnapshot)
                        calculateAndDisplayTotalWorkoutDuration(dataSnapshot)
                        calculateAndDisplayTotalCaloriesBurned(dataSnapshot)

                        // Adjust weight based on caloric balance
                        val currentWeight = it.weight
                        val totalCaloriesIntake = getTotalCaloriesIntake(dataSnapshot)
                        val totalCaloriesBurned = getTotalCaloriesBurned(dataSnapshot)
                        val adjustedWeight = adjustWeight(currentWeight, totalCaloriesIntake, totalCaloriesBurned)
                        binding.textViewWeight.text = adjustedWeight.toString()

                        // Retrieve and display the mood level if it exists
                        val moodLevel = dataSnapshot.child("mood").getValue(Int::class.java)
                        moodLevel?.let { updateUI(it) }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Profile", "Failed to read user data.", databaseError.toException())
                }
            })
        }

        viewModel.selectedMoodLevel.observe(viewLifecycleOwner, Observer { moodLevel ->
            updateUI(moodLevel)
            saveMoodToDatabase(moodLevel)
        })
    }

    private fun calBMI(weight: Float, height: Float) {
        val bmi = weight / (height / 100).pow(2)

        when {
            bmi < 18.5 -> {
                binding.textViewBMI.text = String.format("%.2f", bmi)
                binding.textViewBMI.setTextColor(Color.parseColor("#004AAD"))
            }
            bmi in 18.5..24.9 -> {
                binding.textViewBMI.text = String.format("%.2f", bmi)
                binding.textViewBMI.setTextColor(Color.parseColor("#7ED957"))
            }
            bmi >= 25 -> {
                binding.textViewBMI.text = String.format("%.2f", bmi)
                binding.textViewBMI.setTextColor(Color.parseColor("#FF3131"))
            }
        }
    }

    private fun calculateAge(dateOfBirth: String): CharSequence? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dobDate: Date = dateFormat.parse(dateOfBirth) ?: return null
        val dobCalendar = Calendar.getInstance().apply {
            time = dobDate
        }
        val currentDate = Calendar.getInstance()

        if (dobCalendar.timeInMillis > currentDate.timeInMillis) {
            Toast.makeText(requireContext(), "Invalid date of birth", Toast.LENGTH_SHORT).show()
            return null
        }

        var years = currentDate.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
        var months = currentDate.get(Calendar.MONTH) - dobCalendar.get(Calendar.MONTH)
        var days = currentDate.get(Calendar.DAY_OF_MONTH) - dobCalendar.get(Calendar.DAY_OF_MONTH)

        if (months < 0 || (months == 0 && days < 0)) {
            years--
            months += 12
            if (days < 0) {
                val maxDayOfMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
                days += maxDayOfMonth
            }
        }

        binding.textViewAge.text = years.toString()
        return binding.textViewAge.text
    }

    private fun setGenderIcon(gender: String) {
        val imageViewGender = binding.imageViewGender
        if (gender.equals("female", ignoreCase = true)) {
            imageViewGender.setImageResource(R.drawable.baseline_female_24)
        } else {
            imageViewGender.setImageResource(R.drawable.baseline_male_24)
        }
    }

    private fun loadProfilePictureFromStorage(profileImageName: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val profilePicRef = storageRef.child("profile_images/$profileImageName")

        profilePicRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(binding.profileImage)
        }.addOnFailureListener { exception ->
            Log.e("Profile", "Failed to download profile picture: $exception")
        }
    }

    private fun updateUI(moodLevel: Int) {
        binding.textViewMood.visibility = View.VISIBLE
        binding.textViewMood.text = when (moodLevel) {
            1 -> "Almost Dead"
            2 -> "Sad"
            3 -> "Neutral"
            4 -> "Happy"
            5 -> "Very Happy"
            else -> "Unknown Mood"
        }
    }

    private fun saveMoodToDatabase(moodLevel: Int) {
        if (userId.isNotEmpty()) {
            val moodRef = databaseReference.child(userId).child("mood")
            moodRef.setValue(moodLevel)
                .addOnSuccessListener {
                    Log.d("Profile", "User's mood saved successfully: $moodLevel")
                }
                .addOnFailureListener { e ->
                    Log.e("Profile", "Failed to save user's mood: ${e.message}")
                }
        }
    }

    private fun getTotalCaloriesIntake(dataSnapshot: DataSnapshot): Int {
        var totalCalories = 0
        dataSnapshot.child("intakecaloriesData").children.forEach { dateSnapshot ->
            dateSnapshot.children.forEach { intakeSnapshot ->
                intakeSnapshot.child("calories").getValue(Int::class.java)?.let { calories ->
                    totalCalories += calories
                }
            }
        }
        return totalCalories
    }

    private fun getTotalCaloriesBurned(dataSnapshot: DataSnapshot): Int {
        var totalCaloriesBurned = 0
        dataSnapshot.child("exerciseData").children.forEach { exerciseSnapshot ->
            exerciseSnapshot.child("caloriesBurned").getValue(Int::class.java)?.let { caloriesBurned ->
                totalCaloriesBurned += caloriesBurned
            }
        }
        return totalCaloriesBurned
    }

    private fun adjustWeight(currentWeight: Float, totalCaloriesIntake: Int, totalCaloriesBurned: Int): Float {
        val caloricBalance = totalCaloriesIntake - totalCaloriesBurned
        val conversionFactor = 3500 // Calories per pound for weight change
        val weightChangeRatio = caloricBalance / conversionFactor

        // Calculate adjusted weight
        val adjustedWeight = currentWeight + weightChangeRatio

        // Update weight in the database
        updateWeightInDatabase(adjustedWeight)

        return adjustedWeight
    }

    private fun updateWeightInDatabase(newWeight: Float) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (userId.isNotEmpty()) {
            databaseReference.child(userId).child("weight").setValue(newWeight.toDouble())
                .addOnSuccessListener {
                    Log.d("Profile", "Weight updated successfully to $newWeight")
                }
                .addOnFailureListener { e ->
                    Log.e("Profile", "Failed to update weight: ${e.message}")
                }
        }
    }

    private fun calculateAndDisplayTotalCalories(dataSnapshot: DataSnapshot) {
        var totalCalories = 0
        dataSnapshot.child("intakecaloriesData").children.forEach { dateSnapshot ->
            dateSnapshot.children.forEach { intakeSnapshot ->
                intakeSnapshot.child("calories").getValue(Int::class.java)?.let { calories ->
                    totalCalories += calories
                }
            }
        }
        binding.textViewCalories.text = totalCalories.toString()
    }

    private fun calculateAndDisplayTotalWorkoutDuration(dataSnapshot: DataSnapshot) {
        var totalWorkDuration = 0
        dataSnapshot.child("exerciseData").children.forEach { exerciseSnapshot ->
            exerciseSnapshot.child("workoutDuration").getValue(Int::class.java)?.let { duration ->
                totalWorkDuration += duration
            }
        }
        binding.textViewWorkDuration.text = totalWorkDuration.toString()
    }

    private fun calculateAndDisplayTotalCaloriesBurned(dataSnapshot: DataSnapshot) {
        var totalCaloriesBurned = 0
        dataSnapshot.child("exerciseData").children.forEach { exerciseSnapshot ->
            exerciseSnapshot.child("caloriesBurned").getValue(Int::class.java)?.let { caloriesBurned ->
                totalCaloriesBurned += caloriesBurned
            }
        }
        binding.textViewCalBurn.text = totalCaloriesBurned.toString()
    }

    private fun showInfoDialog() {
        val dialogBinding = MoodinfodesBinding.inflate(layoutInflater)
        val dialogView = dialogBinding.root

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialogBinding.buttonOk.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}