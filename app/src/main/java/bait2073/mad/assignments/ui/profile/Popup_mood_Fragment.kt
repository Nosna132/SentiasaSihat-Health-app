package bait2073.mad.assignments.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import bait2073.mad.assignments.databinding.FragmentPopupMoodBinding


class Popup_mood_Fragment : DialogFragment() {

    private var _binding: FragmentPopupMoodBinding?= null
    private lateinit var viewModel: MoodViewModel
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(requireActivity()).get(MoodViewModel::class.java)
        // Inflate the layout for this fragment
        _binding = FragmentPopupMoodBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val Emoji1: ImageView = binding.Emoji1
        val Emoji2: ImageView = binding.Emoji2
        val Emoji3: ImageView = binding.Emoji3
        val Emoji4: ImageView = binding.Emoji4
        val Emoji5: ImageView = binding.Emoji5


        Emoji1.setOnClickListener { selectMood(1) }
        Emoji2.setOnClickListener { selectMood(2) }
        Emoji3.setOnClickListener { selectMood(3) }
        Emoji4.setOnClickListener { selectMood(4) }
        Emoji5.setOnClickListener { selectMood(5) }

        return root
    }

    private fun selectMood(moodLevel: Int) {
        viewModel.setSelectedMoodLevel(moodLevel)
        dismiss()
    }



}