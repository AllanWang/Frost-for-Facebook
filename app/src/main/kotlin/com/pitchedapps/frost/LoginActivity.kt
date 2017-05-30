package com.pitchedapps.frost

import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.widget.Button
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.pitchedapps.frost.utils.L
import java.util.*


/**
 * Created by Allan Wang on 2017-05-29.
 */
class LoginActivity : FbActivity() {
    lateinit var callback: CallbackManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_teest)
        val loginButton = findViewById(R.id.login_button) as LoginButton
        loginButton.loginBehavior = LoginBehavior.WEB_VIEW_ONLY
        loginButton.setReadPermissions("email")
        val switchh = findViewById(R.id.switchh) as Button
        switchh.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        // If using in a fragment
//        loginButton.setFragment(this)
        // Other app specific specialization

        // Callback registration
        callback = CallbackManager.Factory.create()
        loginButton.registerCallback(callback, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                L.e("Success")
                L.e("Success ${loginResult.accessToken.token}")
            }

            override fun onCancel() {
                // App code
                L.e("Cancel")
            }

            override fun onError(exception: FacebookException) {
                // App code
                L.e("Error")
            }
        })

//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callback.onActivityResult(requestCode, resultCode, data)
    }
}