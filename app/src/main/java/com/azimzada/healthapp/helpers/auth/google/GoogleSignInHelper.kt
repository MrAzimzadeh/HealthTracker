package com.azimzada.healthapp.helpers.auth.google
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.azimzada.healthapp.R
import com.azimzada.healthapp.preference.SavedPreference
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Task

class GoogleSignInHelper(private val context: Context, private val activity: Activity) {

    companion object {
        const val REQ_CODE = 123
    }

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
    }


    fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, REQ_CODE)
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>, updateUI: (GoogleSignInAccount) -> Unit) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                updateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun updateUIAfterSignIn(account: GoogleSignInAccount, onComplete: () -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                SavedPreference.setEmail(context, account.email.toString())
                SavedPreference.setUsername(context, account.displayName.toString())
                onComplete()
            }
        }
    }



}
