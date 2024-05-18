package bait2073.mad.assignments.ui.home

import android.content.Intent
import android.net.Uri
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
import bait2073.mad.assignments.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var articleUrlsRef: DatabaseReference
    private var article1Url: String? = null
    private var article2Url: String? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val fitnessicon: ImageView = binding.FitnessIcon
        val fitnesstext: TextView = binding.FitnesstextView
        val nutritionicon: ImageView = binding.NutritionIcon
        val nutritiontext: TextView = binding.Nutritiontext
        val healthicon: ImageView = binding.HealthSupIcon
        val healthtext: TextView = binding.HealthSuptextView
        val ArticleImage1: ImageView = binding.ArticleImage1
        val ArticleImage2: ImageView = binding.ArticleImage2

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Firebase
        articleUrlsRef = FirebaseDatabase.getInstance().reference.child("articleUrls")
        articleUrlsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                article1Url = snapshot.child("article1").getValue(String::class.java)
                article2Url = snapshot.child("article2").getValue(String::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        fitnessicon.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_fitnessmain)
        }
        fitnesstext.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_fitnessmain)
        }
        nutritionicon.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nutritionFragment)
        }
        nutritiontext.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nutritionFragment)
        }
        healthicon.setOnClickListener {
            findNavController().navigate(R.id.nav_mentalHealthSupport)
        }
        healthtext.setOnClickListener {
            findNavController().navigate(R.id.nav_mentalHealthSupport)
        }

        ArticleImage1.setOnClickListener {
            article1Url?.let { openUrl(it) }
        }

        ArticleImage2.setOnClickListener {
            article2Url?.let { openUrl(it) }
        }

        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openUrl(url: String) {
        val webURL = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webURL)
        startActivity(intent)
    }
}
