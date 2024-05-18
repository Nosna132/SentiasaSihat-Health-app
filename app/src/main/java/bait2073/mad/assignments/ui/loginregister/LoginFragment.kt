package bait2073.mad.assignments.ui.loginregister

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import bait2073.mad.assignments.MainActivity
import bait2073.mad.assignments.R
import bait2073.mad.assignments.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()

        setupListeners()

        binding.signInBtn.setOnClickListener {
            loginUser()
        }

        binding.nextToSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.textForgot.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_forgotFragment)
        }

        return root
    }

    private fun setupListeners() {
        binding.editEmailAddress.doAfterTextChanged {
            validateFields()
        }

        binding.editPassword.doAfterTextChanged {
            validateFields()
        }
    }

    private fun validateFields() {
        val email = binding.editEmailAddress.text.toString()
        val password = binding.editPassword.text.toString()

        binding.signInBtn.isEnabled = email.isNotEmpty() && password.isNotEmpty()
    }

    private fun loginUser() {
        val email = binding.editEmailAddress.text.toString()
        val password = binding.editPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // Login successful
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        // Put user ID into intent
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.putExtra("USER_ID", it)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                } else {
                    // Login failed
                    Toast.makeText(
                        requireContext(),
                        "Login failed: ${authTask.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}