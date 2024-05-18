package bait2073.mad.assignments.ui.loginregister

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
import bait2073.mad.assignments.R
import bait2073.mad.assignments.database.UserDB
import bait2073.mad.assignments.databinding.FragmentRegisterBinding
import bait2073.mad.assignments.databinding.TermnconDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firebase: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebase = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        binding.signUpBtn.setOnClickListener {
            registerUser()
        }

        binding.termconinfo.setOnClickListener{
            showInfoDialog()
        }

        binding.nextToSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.selectPhoto.setOnClickListener {
            chooseImage()
        }

        binding.dobselect.setOnClickListener {
            showDatePickerDialog()
        }

        return root
    }

    private fun showInfoDialog() {
        val dialogBinding = TermnconDialogBinding.inflate(layoutInflater)
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

    private fun registerUser() {
        val email = binding.editEmailAddress.text.toString()
        val password = binding.editPassword.text.toString()
        val username = binding.editUsername.text.toString()
        val birthString = binding.editBirth.text.toString()
        val gender = binding.genderSpinner.selectedItem.toString()
        val weight = binding.editWeight.text.toString().toFloatOrNull() ?: 0.0f
        val height = binding.editHeight.text.toString().toFloatOrNull() ?: 0.0f

        // Check if the checkbox is checked
        if (!binding.cbTerms.isChecked) {
            Toast.makeText(requireContext(), "Please accept the terms and conditions", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if a profile picture has been selected
        if (filePath == null) {
            Toast.makeText(requireContext(), "Please select a profile picture", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || birthString.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val birthDate = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(birthString)
        } catch (e: Exception) {
            null
        }

        if (birthDate == null) {
            Toast.makeText(
                requireContext(),
                "Invalid birth date format",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Calculate age based on birth date
        val cal = Calendar.getInstance()
        cal.time = birthDate
        val dobYear = cal.get(Calendar.YEAR)
        val dobMonth = cal.get(Calendar.MONTH)
        val dobDay = cal.get(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance()
        val todayYear = today.get(Calendar.YEAR)
        val todayMonth = today.get(Calendar.MONTH)
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        var age = todayYear - dobYear

        if (todayMonth < dobMonth || (todayMonth == dobMonth && todayDay < dobDay)) {
            age--
        }

        // Check if age is 18 or above
        if (age < 18) {
            Toast.makeText(
                requireContext(),
                "You must be 18 years old or above to register",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val formattedBirthDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(birthDate)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val authUser = auth.currentUser
                    authUser?.let {
                        uploadImageAndRegister(
                            it.uid,
                            username,
                            email,
                            password,
                            formattedBirthDate,
                            gender,
                            weight,
                            height
                        )
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Registration failed: ${authTask.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
                binding.uploadImage.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImageAndRegister(
        userId: String,
        username: String,
        email: String,
        password: String,
        formattedBirthDate: String,
        gender: String,
        weight: Float,
        height: Float
    ) {
        filePath?.let { uri ->
            val imageName = "profile${userId}" // Use userId to generate image name
            val imageRef = storageRef.child("profile_images/$imageName")

            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                        val imageUrlString = imageUrl.toString()

                        val newUser = UserDB(
                            userId = userId,
                            username = username,
                            email = email,
                            password = password,
                            dateOfBirth = formattedBirthDate,
                            gender = gender,
                            weight = weight,
                            height = height,
                            profileImageName = imageName // Save image name
                        )
                        writeToFirebase(newUser)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            // If user didn't select an image, proceed without uploading image

            val newUser = UserDB(
                userId = userId,
                username = username,
                email = email,
                password = password,
                weight = weight,
                height = height,
                dateOfBirth = formattedBirthDate,
                gender = gender,
                profileImageName = ""
            )
            writeToFirebase(newUser)
        }
    }

    private fun writeToFirebase(user: UserDB) {
        dbRef = firebase.getReference("users/${user.userId}")
        dbRef.setValue(user)
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Registration successful. Data saved to Firebase.",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error saving user data to Firebase.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error ${it.toString()}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        context?.let {
            val datePickerDialog = DatePickerDialog(it, this, year, month, day)
            datePickerDialog.show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)

        binding.editBirth.text = formattedDate
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}