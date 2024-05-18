package bait2073.mad.assignments.ui.mentalsupport

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bait2073.mad.assignments.databinding.MentalSupportBinding

class MentalHealthAssociationAdapter(private val onPhoneCallClick: (String) -> Unit) : RecyclerView.Adapter<MentalHealthAssociationAdapter.ViewHolder>() {

    private var mentalHealthAssociations: List<Mental_Health_Association> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MentalSupportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mentalHealthAssociation = mentalHealthAssociations[position]
        holder.bind(mentalHealthAssociation)
    }

    override fun getItemCount(): Int {
        return mentalHealthAssociations.size
    }

    fun submitList(list: List<Mental_Health_Association>) {
        mentalHealthAssociations = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: MentalSupportBinding ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mentalHealthAssociation: Mental_Health_Association) {
            binding.apply {
                // Populate the UI elements with data from the MentalHealthAssociation object
                binding.textViewAssociationName.text = mentalHealthAssociation.name
                binding.textViewWorkingHour.text = mentalHealthAssociation.workingHours
                binding.textViewPhoneNum.text= mentalHealthAssociation.phoneNumber
                textViewPhoneNum.setOnClickListener {
                    onPhoneCallClick(mentalHealthAssociation.phoneNumber)
                }
            }
        }
    }
}