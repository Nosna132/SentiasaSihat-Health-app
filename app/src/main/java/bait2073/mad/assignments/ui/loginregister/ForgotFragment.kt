package bait2073.mad.assignments.ui.loginregister

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import bait2073.mad.assignments.databinding.FragmentForgotBinding
import com.google.firebase.auth.FirebaseAuth


class ForgotFragment : Fragment() {

    private var _binding: FragmentForgotBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()

        binding.submitBtn.setOnClickListener {
            resetPassword()
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        return root
    }

    private fun resetPassword() {
        val email = binding.editEmailAddress.text.toString()

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }else{
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Password reset email sent successfully
                        Toast.makeText(
                            requireContext(),
                            "Password reset email sent to $email",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Navigate back to the login fragment
                        findNavController().popBackStack()
                    } else {
                        // Password reset email sending failed
                        Toast.makeText(
                            requireContext(),
                            "Failed to send password reset email: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }


    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}