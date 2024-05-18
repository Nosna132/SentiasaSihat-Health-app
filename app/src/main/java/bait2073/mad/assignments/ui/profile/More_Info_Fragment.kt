package bait2073.mad.assignments.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import bait2073.mad.assignments.databinding.FragmentMoreInfoBinding

class More_Info_Fragment : DialogFragment() {
    private var _binding: FragmentMoreInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMoreInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root;
    }


}