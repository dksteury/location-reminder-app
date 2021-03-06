package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.google.firebase.*
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    companion object {
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 101
    }

    private val viewModel by viewModels<AuthenticationViewModel>()
//    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_authentication)
//        val binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        val binding = DataBindingUtil.setContentView<ActivityAuthenticationBinding>(this, R.layout.activity_authentication)

//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
//        binding.buttonLogin.setOnClickListener { launchSignInFlow() }

//          TODO: If the user was authenticated, send him to RemindersActivity
        viewModel.authenticationStatic.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    startActivity(Intent(this, RemindersActivity::class.java))
//                    AuthUI.getInstance().signOut(this)
                }
                AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    binding.buttonLogin.setOnClickListener { launchSignInFlow() }
                }
                else -> {Log.i(TAG, "authenticationState could be null!")}
            }

        })

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
//                startActivity(Intent(this, RemindersActivity::class.java))
            } else {
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }
}
