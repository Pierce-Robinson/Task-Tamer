package com.varsitycollege.tasktamer

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity() {

    private lateinit var  auth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        var currentUser: FirebaseUser? = auth.currentUser
        //If has just registered, go straight to dashboard
        if (currentUser != null) {
            //Not coming from register, just opened app while previously signed in
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_main)

        val actionBar = supportActionBar
        actionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.white)))
        supportActionBar?.elevation = 0f
        //Login to home
        val login = findViewById<Button>(R.id.login_loginButton)
        login.setOnClickListener{
            login()
        }

        //redirect to sign up
        val signUp = findViewById<TextView>(R.id.login_signUp)
        signUp.setOnClickListener{
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)
        }
    }

    private fun login(){
        try {
            //Reset errors
            findViewById<TextInputLayout>(R.id.login_emailTextField).error = null
            findViewById<TextInputLayout>(R.id.login_passwordTextField).error = null

            val email = findViewById<EditText>(R.id.login_emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.login_passwordEditText).text.toString()

            if  (email.isBlank()){
                findViewById<TextInputLayout>(R.id.login_emailTextField).error = "Required."
                return
            }
            if (password.isBlank()) {
                findViewById<TextInputLayout>(R.id.login_passwordTextField).error = "Required."
                findViewById<TextInputLayout>(R.id.login_passwordTextField).setEndIconTintList(
                    ColorStateList.valueOf(getColor(R.color.Btn_Red)))
                return
            }

            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext,e.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }

}