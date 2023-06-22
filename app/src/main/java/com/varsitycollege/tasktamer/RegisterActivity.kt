package com.varsitycollege.tasktamer

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.varsitycollege.tasktamer.data.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var  auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_register)

        val actionBar = supportActionBar
        actionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.white)))
        supportActionBar?.elevation = 0f
        //Call register method when register is pressed
        val register = findViewById<Button>(R.id.register_registerButton)
        register.setOnClickListener{
            register()
        }

        //Go back to login
        val back = findViewById<TextView>(R.id.register_signUpText)
        back.setOnClickListener{
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }
    }

    private fun register(){
        try {
            //Reset errors
            val emailField = findViewById<TextInputLayout>(R.id.register_emailTextField)
            val nameField = findViewById<TextInputLayout>(R.id.register_nameTextField)
            val passField = findViewById<TextInputLayout>(R.id.register_passwordTextField)
            val confirmField = findViewById<TextInputLayout>(R.id.register_confirmTextField)
            emailField.error = null
            nameField.error = null
            passField.error = null
            confirmField.error = null

            val email = findViewById<EditText>(R.id.register_emailEditText).text.toString()
            val name = findViewById<EditText>(R.id.register_nameEditText).text.toString()
            val password = findViewById<EditText>(R.id.register_passwordEditText).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.register_confirmEditText).text.toString()

            if  (email.isBlank()){
                emailField.error = "Required."
                return
            }
            if (name.isBlank()) {
                nameField.error = "Required."
                return
            }
            if (password.isBlank()) {
                passField.error = "Required."
                passField.setEndIconTintList(
                    ColorStateList.valueOf(getColor(R.color.Btn_Red)))
                return
            }
            if (confirmPassword.isBlank()) {
                confirmField.error = "Required."
                confirmField.setEndIconTintList(
                    ColorStateList.valueOf(getColor(R.color.Btn_Red)))
                return
            }
            if (password == confirmPassword) {
                //Toast.makeText(applicationContext, "Register was successful for $email",Toast.LENGTH_LONG).show()

                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        //Set display name
                        val user = auth.currentUser
                        var builder = UserProfileChangeRequest.Builder()
                        builder.displayName = name
                        var changeRequest: UserProfileChangeRequest = builder.build()
                        user!!.updateProfile(changeRequest)

                        //Create user object for user
                        val userObj = User(id = user.uid, displayName = name, darkMode = false, minHours = 0.0, maxHours = 0.0)
                        val database = FirebaseDatabase.getInstance("https://task-tamer-9c5f3-default-rtdb.europe-west1.firebasedatabase.app/")
                        val ref = database.getReference("Users")
                        userObj.id?.let {
                            ref.child(it).setValue(userObj).addOnSuccessListener {
                                Toast.makeText(applicationContext, "Welcome $name", Toast.LENGTH_LONG).show()
                            }
                        }

                        //Create default category for user
                        try {
                            val database = FirebaseDatabase.getInstance("https://task-tamer-9c5f3-default-rtdb.europe-west1.firebasedatabase.app/")
                            val ref = database.getReference("Categories")
                            val key = ref.push().key
                            val category = com.varsitycollege.tasktamer.data.Category(
                                id = key!!,
                                title = "Uncategorized",
                                description = "Default Category",
                                client = "None",
                                deadline = "None",
                                colour = "#D03838",
                                userId = user.uid
                            )
                            category.id?.let { it1 ->
                                ref.child(it1).setValue(category).addOnSuccessListener {
                                    //Toast.makeText(applicationContext, "Success.", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
                        }

                        //Sign in immediately
                        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                            if(task.isSuccessful){
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }
            else {
                Toast.makeText(applicationContext,"Passwords don't match.",Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext,e.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }
}