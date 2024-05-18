package bait2073.mad.assignments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import bait2073.mad.assignments.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.storage

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.nv
        val bottomNavView: BottomNavigationView = binding.bv
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_fitnessmain,
                R.id.nav_nutrition,
                R.id.nav_testuse,
                R.id.nav_logpage,
                R.id.nav_profileFragment,
                R.id.nav_mentalHealthSupport
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        dbReference = firebaseDatabase.reference.child("users")

        // Set up bottom navigation view
        bottomNavView.setupWithNavController(navController)

        // Ensure correct destination is selected when bottom navigation item is clicked
        bottomNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Navigate to the HomeFragment
                    navController.navigate(R.id.nav_home)
                    true
                }

                R.id.nav_fitnessmain -> {
                    // Navigate to the FitnessFragment
                    navController.navigate(R.id.nav_fitnessmain)
                    true
                }

                R.id.nav_nutrition -> {
                    // Navigate to the NutritionFragment
                    navController.navigate(R.id.nav_nutrition)
                    true
                }

                R.id.nav_testuse -> {
                    // Navigate to the TestUseFragment
                    navController.navigate(R.id.nav_testuse)
                    true
                }

                R.id.nav_logpage -> {
                    // Navigate to the LogPageFragment
                    navController.navigate(R.id.nav_logpage)
                    true
                }

                R.id.nav_profileFragment -> {
                    // Navigate to the LogPageFragment
                    navController.navigate(R.id.nav_profileFragment)
                    true
                }

                R.id.nav_mentalHealthSupport -> {
                    // Navigate to the LogPageFragment
                    navController.navigate(R.id.nav_mentalHealthSupport)
                    true
                }
                // Add other cases for the remaining menu items if needed
                else -> false
            }
        }

        // Get reference to the header view
        val headerView = navView.getHeaderView(0)

        // Load user info when activity is created
        loadUserInfo(headerView)
    }

    private fun loadUserInfo(headerView: View) {
        if (isLoggedIn()) {
            // Retrieve user ID from SharedPreferences
            val userId = intent.getStringExtra("USER_ID")
            userId?.let {
                // Retrieve user information and update UI
                retrieveUserInfo(it, headerView)
                // Display welcome toast
                displayWelcomeToast(it)
            } ?: run {
                // Handle case where userId is null
                navigateToLoginFragment()
            }
        } else {
            navigateToLoginFragment()
        }
    }

    private fun displayWelcomeToast(userId: String) {
        dbReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").getValue(String::class.java)
                username?.let {
                    // Display welcome toast
                    Toast.makeText(
                        this@MainActivity,
                        "Welcome $username to SentiasaSihat~",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("MainActivity", "Failed to retrieve username: $error")
            }
        })
    }

    private fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun navigateToLoginFragment() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.navigate(R.id.action_nav_to_home_to_loginFragment)
    }

    private fun retrieveUserInfo(userId: String, headerView: View) {
        dbReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").getValue(String::class.java)
                val gender = snapshot.child("gender").getValue(String::class.java)
                val profileImageName =
                    snapshot.child("profileImageName").getValue(String::class.java)

                profileImageName?.let {
                    loadProfileImage(it, headerView)
                }

                updateUI(username, gender, headerView)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to retrieve user information",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun loadProfileImage(profileImageName: String, headerView: View) {
        val imgProfile: ImageView = headerView.findViewById(R.id.imgProfile)

        val storageRef = Firebase.storage.reference
        val profilePicRef = storageRef.child("profile_images/$profileImageName")

        profilePicRef.downloadUrl.addOnSuccessListener { uri ->
            // Load the image into ImageView using Glide
            Glide.with(this@MainActivity)
                .load(uri)
                .placeholder(R.drawable.placeholder) // Placeholder image while loading
                .error(R.drawable.baseline_cancel_24) // Image to display if loading fails
                .into(imgProfile)
        }.addOnFailureListener { exception ->
            // Handle any errors that occur during the download
            Log.e("MainActivity", "Failed to load profile image: $exception")
        }
    }

    private fun updateUI(username: String?, gender: String?, headerView: View) {
        // Get reference to the TextViews in the header layout
        val txtName: TextView = headerView.findViewById(R.id.txtName)
        val pronouncetext: TextView = headerView.findViewById(R.id.pronouncetext)

        // Update TextView with username
        txtName.text = username ?: "Name Placeholder"

        // Determine pronoun based on gender
        val pronoun = if (gender.equals("female", ignoreCase = true)) {
            getString(R.string.miss)
        } else {
            getString(R.string.mister)
        }

        pronouncetext.text = pronoun
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
