package bait2073.mad.assignments.ui.mentalsupport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import bait2073.mad.assignments.databinding.FragmentMentalSupportListBinding


class MentalHealthSupport : Fragment() {

    private var _binding: FragmentMentalSupportListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMentalSupportListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView: RecyclerView = binding.recyclerView
        val adapter = MentalHealthAssociationAdapter { phoneNumber ->
            dialPhoneNumber(phoneNumber)
        }

        val mentalHealthAssociations: List<Mental_Health_Association> = getMentalHealthAssociations()

        adapter.submitList(mentalHealthAssociations)
        recyclerView.adapter = adapter


        return root
    }

    private fun getMentalHealthAssociations(): List<Mental_Health_Association> {
        // Replace this with your logic to fetch mental health associations data from your data source
        // For example:
        val association1 = Mental_Health_Association("Malaysian Mental Health Association (MMHA)", "9:00 AM - 5:00 PM", "0327806803")
        val association2 = Mental_Health_Association("TALIAN HEAL 15555 Helpline", "8:00 AM–11:59 PM", "15555")
        val association3 = Mental_Health_Association("Sage Centre", "24/7", "60123397121")
        return listOf(association1, association2, association3)
    }
    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

}
