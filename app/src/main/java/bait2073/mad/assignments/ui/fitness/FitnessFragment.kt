package bait2073.mad.assignments.ui.fitness

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import bait2073.mad.assignments.R
import bait2073.mad.assignments.databinding.FragmentFitnessmainBinding
import bait2073.mad.assignments.databinding.DialogInfoFitnessBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FitnessFragment : Fragment() {

    private var _binding: FragmentFitnessmainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fitnessViewModel =
            ViewModelProvider(this).get(FitnessViewModel::class.java)

        _binding = FragmentFitnessmainBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val bodyweightImageView: ImageView = binding.bodyweightimage
        val cardioImageView: ImageView = binding.cardioimage
        val pilatesImageView: ImageView = binding.pilatesimage
        val notification: ImageView = binding.notification
        val infoFitnessImageView: ImageView = binding.infofitness

        val textView: TextView = binding.textFitness
        fitnessViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        bodyweightImageView.setOnClickListener {
            findNavController().navigate(R.id.action_nav_fitnessmain_to_body_weight_fragment)
        }

        cardioImageView.setOnClickListener {
            findNavController().navigate(R.id.action_nav_fitnessmain_to_cardioExercisesFragment)
        }

        pilatesImageView.setOnClickListener {
            findNavController().navigate(R.id.action_nav_fitnessmain_to_pilatesExercisesFragment)
        }

        notification.setOnClickListener {
            findNavController().navigate(R.id.action_nav_fitnessmain_to_notificationFragment)
        }

        infoFitnessImageView.setOnClickListener {
            showInfoDialog()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showInfoDialog() {
        val dialogBinding = DialogInfoFitnessBinding.inflate(layoutInflater)
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
}
