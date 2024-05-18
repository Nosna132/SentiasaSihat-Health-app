package bait2073.mad.assignments.ui.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import bait2073.mad.assignments.database.UserDB
import bait2073.mad.assignments.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Edit_profile : Fragment(), DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")

        loadUserData()
        binding.btnSaveProfile.setOnClickListener { saveProfileChanges() }
        binding.imageView5.setOnClickListener { showDatePickerDialog() }
        binding.imageViewProfilePic.setOnClickListener { chooseImage() }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userData = dataSnapshot.getValue(UserDB::class.java)
                userData?.let { user ->
                    with(binding) {
                        editTextName.setText(user.username)
                        editTextWeight.setText(user.weight.toString())
                        editTextHeight.setText(user.height.toString())
                        editTextEmailAddress.setText(user.email)
                        editTextPassword.setText(user.password)
                        val genderIndex = if (user.gender == "female") 1 else 0
                        spinnerGender.setSelection(genderIndex)
                        textViewBOD.text = user.dateOfBirth
                    }
                    loadImage(user.profileImageName)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("EditProfileFragment", "Failed to read user data.", databaseError.toException())
            }
        })
    }

    private fun saveProfileChanges() {
        val userId = auth.currentUser?.uid ?: return
        val updatedUsername = binding.editTextName.text.toString()
        val updatedEmail = binding.editTextEmailAddress.text.toString()
        val updatedWeight = binding.editTextWeight.text.toString().toFloatOrNull() ?: 0.0f
        val updatedHeight = binding.editTextHeight.text.toString().toFloatOrNull() ?: 0.0f
        val updatedGender = if (binding.spinnerGender.selectedItemPosition == 1) "female" else "male"
        val updatedDOB = binding.textViewBOD.text.toString()
        val updatePassword = binding.editTextPassword.text.toString()

        databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userData = dataSnapshot.getValue(UserDB::class.java)
                userData?.let { user ->
                    val existingProfileImageName = user.profileImageName
                    val userUpdates = mapOf(
                        "username" to updatedUsername,
                        "email" to updatedEmail,
                        "password" to updatePassword,
                        "weight" to updatedWeight,
                        "height" to updatedHeight,
                        "gender" to updatedGender,
                        "dateOfBirth" to updatedDOB,
                        "profileImageName" to existingProfileImageName
                    )
                    databaseReference.child(userId).updateChildren(userUpdates)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            uploadProfileImage()
                            // Navigate back to the profile fragment
                            findNavController().popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("EditProfileFragment", "Failed to read user data.", databaseError.toException())
            }
        })
    }

    private fun loadImage(profileImageName: String) {
        val profilePicRef = storageReference.child(profileImageName)
        profilePicRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(binding.imageViewProfilePic)
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Failed to load profile: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        context?.let {
            val datePickerDialog = DatePickerDialog(it, { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val todayCalendar = Calendar.getInstance()
                todayCalendar.add(Calendar.YEAR, -18)

                if (selectedCalendar <= todayCalendar) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedCalendar.time)
                    binding.textViewBOD.text = formattedDate
                } else {
                    Toast.makeText(requireContext(), "You must be at least 18 years old.", Toast.LENGTH_SHORT).show()
                }
            }, year, month, day)
            datePickerDialog.show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        binding.textViewBOD.text = formattedDate
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
                binding.imageViewProfilePic.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadProfileImage() {
        val userId = auth.currentUser?.uid ?: return
        filePath?.let { uri ->
            databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userData = dataSnapshot.getValue(UserDB::class.java)
                        userData?.let { user ->
                            val existingProfileImageName = user.profileImageName
                            val imageName = existingProfileImageName
                            val imageRef = storageReference.child(imageName)

                            imageRef.putFile(uri)
                                .addOnSuccessListener {
                                    databaseReference.child(userId).child("profileImageName").setValue(imageName)
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(requireContext(), "Failed to update profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to upload profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(requireContext(), "User does not exist", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("EditProfileFragment", "Failed to read user data.", databaseError.toException())
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}