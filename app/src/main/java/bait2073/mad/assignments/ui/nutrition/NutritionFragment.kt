package bait2073.mad.assignments.ui.nutrition

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import bait2073.mad.assignments.R
import bait2073.mad.assignments.databinding.FragmentNutritionBinding

class NutritionFragment : Fragment() {

    private var _binding: FragmentNutritionBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseReference: DatabaseReference
    private lateinit var UserId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNutritionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        databaseReference = FirebaseDatabase.getInstance().reference
        UserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Calculate and display nutrition advice based on BMI
        calculateAndDisplayNutritionAdvice()

        // Check if the user has already eaten breakfast, lunch, or dinner today
        checkIntakeStatus(UserId)

        // Calculate and display total intake calories
        calculateAndDisplayTotalIntakeCalories(UserId)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("NutritionIntake", Context.MODE_PRIVATE)

        val breakfastCheckBox = binding.breakfastCheckBox
        val lunchCheckBox = binding.lunchCheckBox
        val dinnerCheckBox = binding.dinnerCheckBox

        // Set click listeners for checkboxes
        breakfastCheckBox.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange(isChecked, "breakfast", UserId, sharedPreferences)
        }

        lunchCheckBox.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange(isChecked, "lunch", UserId, sharedPreferences)
        }

        dinnerCheckBox.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange(isChecked, "dinner", UserId, sharedPreferences)
        }
    }

    private fun calculateAndDisplayNutritionAdvice() {
        databaseReference.child("users").child(UserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val weight = dataSnapshot.child("weight").getValue(Double::class.java) ?: 0.0
                val height = dataSnapshot.child("height").getValue(Double::class.java) ?: 0.0

                // Ensure weight and height are greater than 0 to avoid division by zero
                if (weight > 0 && height > 0) {
                    val bmi = calculateBMI(weight, height)
                    displayNutritionAdvice(bmi)
                } else {
                    // Handle case where weight or height is missing or invalid
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    private fun calculateBMI(weight: Double, height: Double): Double {
        // Calculate BMI using the formula: BMI = weight (kg) / (height (m) * height (m))
        val heightInMeters = height / 100 // Convert height from centimeters to meters
        return weight / (heightInMeters * heightInMeters)
    }


    private fun displayNutritionAdvice(bmi: Double) {
        val textViewBreakfast = binding.topicBreakfast
        val textViewLunch = binding.topicLunch
        val textViewDinner = binding.topicDinner
        val recipedesbreakfast = binding.breakfastRecipe
        val recipedeslunch = binding.lunchRecipe
        val recipedesdinner = binding.dinnerRecipe

        // Display nutrition advice based on BMI
        when {
            bmi < 18.5 -> {
                textViewBreakfast.text = getString(R.string.underweight_breakfast_topic)
                textViewLunch.text = getString(R.string.underweight_lunch_topic)
                textViewDinner.text = getString(R.string.underweight_dinner_topic)
                recipedesbreakfast.text = getString(R.string.underweight_breakfast_recipe)
                recipedeslunch.text = getString(R.string.underweight_lunch_recipe)
                recipedesdinner.text = getString(R.string.underweight_dinner_recipe)
            }
            bmi > 18.5 && bmi < 24.9 -> {
                textViewBreakfast.text = getString(R.string.normal_weight_breakfast_topic)
                textViewLunch.text = getString(R.string.normal_weight_lunch_topic)
                textViewDinner.text = getString(R.string.normal_weight_dinner_topic)
                recipedesbreakfast.text = getString(R.string.normal_weight_breakfast_recipe)
                recipedeslunch.text = getString(R.string.normal_weight_lunch_recipe)
                recipedesdinner.text = getString(R.string.normal_weight_dinner_recipe)
            }
            else -> {
                textViewBreakfast.text = getString(R.string.overweight_breakfast_topic)
                textViewLunch.text = getString(R.string.overweight_lunch_topic)
                textViewDinner.text = getString(R.string.overweight_dinner_topic)
                recipedesbreakfast.text = getString(R.string.overweight_breakfast_recipe)
                recipedeslunch.text = getString(R.string.overweight_lunch_recipe)
                recipedesdinner.text = getString(R.string.overweight_dinner_recipe)
            }
        }
    }

    private fun checkIntakeStatus(userId: String) {
        val currentDate = getCurrentDate()
        val sharedPreferences = requireContext().getSharedPreferences("NutritionIntake", Context.MODE_PRIVATE)

        // Check intake status for the specified user ID
        databaseReference.child("users").child(userId).child("intakecaloriesData").child(currentDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach { brunchSnapshot ->
                        val intakeCalories = brunchSnapshot.child("calories").getValue(Int::class.java)
                        when (brunchSnapshot.key) {
                            "breakfast" -> {
                                val breakfastCheckBox = binding.breakfastCheckBox
                                if (intakeCalories != null && intakeCalories > 0) {
                                    // User has already eaten breakfast today
                                    breakfastCheckBox.isChecked = true
                                    breakfastCheckBox.isEnabled = false
                                } else {
                                    // Check if breakfast was already checked today
                                    if (isCheckboxAlreadyChecked("breakfast", sharedPreferences)) {
                                        breakfastCheckBox.isChecked = true
                                    } else {
                                        breakfastCheckBox.isEnabled = true
                                    }
                                }
                            }
                            "lunch" -> {
                                val lunchCheckBox = binding.lunchCheckBox
                                if (intakeCalories != null && intakeCalories > 0) {
                                    // User has already eaten lunch today
                                    lunchCheckBox.isChecked = true
                                    lunchCheckBox.isEnabled = false
                                } else {
                                    // Check if lunch was already checked today
                                    if (isCheckboxAlreadyChecked("lunch", sharedPreferences)) {
                                        lunchCheckBox.isChecked = true
                                    } else {
                                        lunchCheckBox.isEnabled = true
                                    }
                                }
                            }
                            "dinner" -> {
                                val dinnerCheckBox = binding.dinnerCheckBox
                                if (intakeCalories != null && intakeCalories > 0) {
                                    // User has already eaten dinner today
                                    dinnerCheckBox.isChecked = true
                                    dinnerCheckBox.isEnabled = false
                                } else {
                                    // Check if dinner was already checked today
                                    if (isCheckboxAlreadyChecked("dinner", sharedPreferences)) {
                                        dinnerCheckBox.isChecked = true
                                    } else {
                                        dinnerCheckBox.isEnabled = true
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                }
            })
    }

    private fun getCurrentDate(): String {
        // Get current date in the format "yyyy-MM-dd"
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun calculateAndDisplayTotalIntakeCalories(userId: String) {
        // Calculate total intake calories for the specified user ID
        databaseReference.child("users").child(userId).child("intakecaloriesData")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var totalCalories = 0

                    // Loop through each date in the snapshot
                    dataSnapshot.children.forEach { dateSnapshot ->
                        // Loop through each brunch type in the date snapshot
                        dateSnapshot.children.forEach { brunchSnapshot ->
                            // Retrieve calories for each brunch type and add to total calories
                            val calories = brunchSnapshot.child("calories").getValue(Int::class.java) ?: 0
                            totalCalories += calories
                        }
                    }

                    // Display the total intake calories
                    binding.totalCaloriesTextView.text = getString(R.string.total_calories, totalCalories)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                }
            })
    }

    private fun handleCheckboxChange(isChecked: Boolean, brunchType: String, userId: String, sharedPreferences: SharedPreferences) {
        val currentDate = getCurrentDate()
        val lastCheckedDate = sharedPreferences.getString(brunchType, "")

        if (isChecked && lastCheckedDate != currentDate) {
            val intakeCaloriesRef = databaseReference.child("users").child(userId).child("intakecaloriesData").child(currentDate).child(brunchType)

            intakeCaloriesRef.child("calories").setValue(getBrunchCalories(brunchType))

            sharedPreferences.edit().putString(brunchType, currentDate).apply()

            Toast.makeText(requireContext(), "Intake calories for $brunchType added", Toast.LENGTH_SHORT).show()
        } else if (!isChecked && lastCheckedDate == currentDate) {
            val intakeCaloriesRef = databaseReference.child("users").child(userId).child("intakecaloriesData").child(currentDate).child(brunchType)
            intakeCaloriesRef.removeValue()

            sharedPreferences.edit().remove(brunchType).apply()

            Toast.makeText(requireContext(), "Intake calories for $brunchType removed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isCheckboxAlreadyChecked(brunchType: String, sharedPreferences: SharedPreferences): Boolean {
        val currentDate = getCurrentDate()
        val lastCheckedDate = sharedPreferences.getString(brunchType, "")
        return lastCheckedDate == currentDate
    }

    private fun getBrunchCalories(brunchType: String): Int {
        return when (brunchType) {
            "breakfast" -> {
                val bmiCategory = binding.topicBreakfast.text.toString()
                when (bmiCategory) {
                    getString(R.string.underweight_breakfast_topic) -> 300
                    getString(R.string.normal_weight_breakfast_topic) -> 500
                    getString(R.string.overweight_breakfast_topic) -> 700
                    else -> 0
                }
            }
            "lunch" -> {
                val bmiCategory = binding.topicLunch.text.toString()
                when (bmiCategory) {
                    getString(R.string.underweight_lunch_topic) -> 400
                    getString(R.string.normal_weight_lunch_topic) -> 600
                    getString(R.string.overweight_lunch_topic) -> 800
                    else -> 0
                }
            }
            "dinner" -> {
                val bmiCategory = binding.topicDinner.text.toString()
                when (bmiCategory) {
                    getString(R.string.underweight_dinner_topic) -> 500
                    getString(R.string.normal_weight_dinner_topic) -> 700
                    getString(R.string.overweight_dinner_topic) -> 900
                    else -> 0
                }
            }
            else -> 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}