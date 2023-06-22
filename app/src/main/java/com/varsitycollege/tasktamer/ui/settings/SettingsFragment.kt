package com.varsitycollege.tasktamer.ui.settings

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varsitycollege.tasktamer.MainActivity
import com.varsitycollege.tasktamer.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    private lateinit var user: String

    // Dark mode
    private var currentThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    private var isRecreating = false

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.LogoutBtn.setOnClickListener {
            auth = FirebaseAuth.getInstance()
            val currentUser: FirebaseUser? = auth.currentUser
            if (currentUser != null) {
                logOut()
            }
        }

        binding.DeleteAccBtn.setOnClickListener {
            auth = FirebaseAuth.getInstance()
            try {
                val id = auth.currentUser?.uid
                auth.currentUser?.delete()?.addOnSuccessListener {
                    database = FirebaseDatabase.getInstance("https://task-tamer-9c5f3-default-rtdb.europe-west1.firebasedatabase.app/")
                    ref = database.getReference("Users")
                    if (id != null) {
                        ref.child(id).removeValue()
                    }
                    activity?.let {
                        val intent = Intent(it, MainActivity::class.java)
                        it.startActivity(intent)
                        it.finish() // Finish the current activity after logout
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    activity?.applicationContext,
                    "Please login, then try again.",
                    Toast.LENGTH_SHORT
                ).show()
                logOut()
            }
        }

        //Display current goals for logged in user
        database = FirebaseDatabase.getInstance("https://task-tamer-9c5f3-default-rtdb.europe-west1.firebasedatabase.app/")
        ref = database.getReference("Users")
        user = FirebaseAuth.getInstance().currentUser?.uid.toString()
        getHours()

        //Edit min and max goals
        binding.minGoal.setOnClickListener {
            val value = binding.setMinGoalEditText.text.toString().toDouble()
            if (value in 0.0..24.0 && value < binding.setMaxGoalEditText.text.toString().toDouble()) {
                ref.child(user).child("minHours").setValue(value).addOnSuccessListener {
                    Toast.makeText(
                        activity?.applicationContext,
                        "Min hours updated!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    activity?.applicationContext,
                    "Invalid number of hours.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.maxGoal.setOnClickListener {
            val value = binding.setMaxGoalEditText.text.toString().toDouble()
            if (value in 0.0..24.0 && value > binding.setMinGoalEditText.text.toString().toDouble()) {
                ref.child(user).child("maxHours").setValue(value).addOnSuccessListener {
                    Toast.makeText(
                        activity?.applicationContext,
                        "Max hours updated!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    activity?.applicationContext,
                    "Invalid number of hours.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        //Dark mode btn
        binding.DarkModeSwitchBtn.setOnCheckedChangeListener { _, isChecked ->

                currentThemeMode = if (isChecked) {
                    AppCompatDelegate.MODE_NIGHT_YES // Enable dark mode
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO // Disable dark mode
                }
                AppCompatDelegate.setDefaultNightMode(currentThemeMode) // Apply the new theme mode


        }

        return binding.root
    }

    private fun getHours() {
        ref.addValueEventListener( object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val userId = user
                if (snapshot.exists()) {
                    for (taskSnapshot in snapshot.children) {
                        if (userId == taskSnapshot.child("id").value) {
                            val min = taskSnapshot.child("minHours").value
                            val max = taskSnapshot.child("maxHours").value
                            _binding?.setMinGoalEditText?.setText(min.toString())
                            _binding?.setMaxGoalEditText?.setText(max.toString())
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
              ref.removeEventListener(this)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logOut() {
        auth.signOut()
        activity?.let {
            val intent = Intent(it, MainActivity::class.java)
            it.startActivity(intent)
            it.finish() // Finish the current activity after logout
        }
    }
}
