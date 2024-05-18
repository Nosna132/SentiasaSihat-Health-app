package bait2073.mad.assignments

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivityLogReg : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_log_reg)

        if (isLoggedIn()) {
            navigateToMainActivity()
        } else {
            Toast.makeText(this, "Welcome to SentiasaSihat~", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLoggedIn(): Boolean {
        return false
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}