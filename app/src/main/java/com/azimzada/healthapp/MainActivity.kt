package com.azimzada.healthapp
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azimzada.healthapp.helpers.auth.google.GoogleSignInHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.FirebaseApp

//import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInHelper: GoogleSignInHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        googleSignInHelper = GoogleSignInHelper(this, this)

        findViewById<Button>(R.id.Signin).setOnClickListener {
            Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
            googleSignInHelper.signInGoogle()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleSignInHelper.REQ_CODE) {
            googleSignInHelper.handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data)) {
                updateUIAfterSignIn(it)
            }
        }
    }

    private fun updateUIAfterSignIn(account: GoogleSignInAccount) {
        googleSignInHelper.updateUIAfterSignIn(account) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

}