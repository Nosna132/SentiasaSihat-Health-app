package bait2073.mad.assignments.ui.fitness

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import bait2073.mad.assignments.databinding.FragmentTestuseBinding
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*

class testuse : Fragment() {
    private var _binding: FragmentTestuseBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestuseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        databaseReference = FirebaseDatabase.getInstance().reference.child("exerciseData")
        storageReference = FirebaseStorage.getInstance().reference

        binding.uploadimage.setOnClickListener {
            chooseImage()
        }

        binding.uploadfirebase.setOnClickListener {
            uploadImage()
        }

        // Update UI with total values
        readDataAndUpdateUI()

        return root
    }

    private fun readDataAndUpdateUI() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalCaloriesBurned = 0
                var totalWorkoutDuration = 0

                // Iterate through each exercise data
                for (exerciseSnapshot in dataSnapshot.children) {
                    val caloriesBurned = exerciseSnapshot.child("caloriesBurned").getValue(Int::class.java) ?: 0
                    val workoutDuration = exerciseSnapshot.child("workoutDuration").getValue(Int::class.java) ?: 0

                    // Update total calories burned and total workout duration
                    totalCaloriesBurned += caloriesBurned
                    totalWorkoutDuration += workoutDuration
                }

                // Update UI with total values
                updateUI(totalCaloriesBurned, totalWorkoutDuration)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun updateUI(totalCaloriesBurned: Int, totalWorkoutDuration: Int) {
        // Update TextViews with total values
        binding.totalcaloriesburn.text = "Total Calories Burned: $totalCaloriesBurned"
        binding.totalworkoutdu.text = "Total Workout Duration: $totalWorkoutDuration minutes"
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, filePath)
                binding.imageView2.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        if (filePath != null) {
            val ref = storageReference.child("profile/" + UUID.randomUUID().toString())
            ref.putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot ->
                    Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    // Handle successful image upload, if needed
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(requireContext(), "Select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}