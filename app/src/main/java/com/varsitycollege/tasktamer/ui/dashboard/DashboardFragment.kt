package com.varsitycollege.tasktamer.ui.dashboard

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varsitycollege.tasktamer.data.DashAdapter
import com.varsitycollege.tasktamer.data.TimeSheetEntry
import com.varsitycollege.tasktamer.databinding.FragmentDashboardBinding
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    private lateinit var goalRef: DatabaseReference
    private lateinit var taskArrayList: ArrayList<TimeSheetEntry?>
    private lateinit var taskGridView: GridView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        //Ensure data updates when user views fragment
        auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            binding.dashUser.text = "Welcome ${currentUser.displayName} 👋"
            getData(currentUser)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this)[DashboardViewModel::class.java]

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        taskGridView = binding.dashGridView
        taskArrayList = arrayListOf()

        auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            binding.dashUser.text = "Welcome ${currentUser.displayName} 👋"
            getData(currentUser)
        }

        //viewmodel stuff
        //val textView: TextView = binding.textDashboard
        //dashboardViewModel.text.observe(viewLifecycleOwner) {
        //  textView.text = it
        //}
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getData(user: FirebaseUser) {
        //Daytotal records users hours for current day
        var dayTotal = 0f
        //Firebase instance
        database =
            FirebaseDatabase.getInstance("https://task-tamer-9c5f3-default-rtdb.europe-west1.firebasedatabase.app/")
        //Time sheet entries
        ref = database.getReference("TimeSheetEntries")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                if (snapshot.exists()) {
                    taskArrayList.clear()
                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(TimeSheetEntry::class.java)
                        val assignedUser = task?.userId
                        val taskDate = task?.date
                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                        // Check if the task is assigned to the current user
                        if (task != null) {
                            if (assignedUser == currentUserId && taskDate == currentDate) {
                                taskArrayList.add(task)
                                //Add time of task to day total
                                dayTotal += calculateDurationInHours(task.startTime, task.endTime)
                            }
                        }
                    }
                    val adapter = context?.let { DashAdapter(it, taskArrayList) }
                    taskGridView.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    activity?.applicationContext,
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        //Hour goals
        goalRef = database.getReference("Users")
        goalRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    try {
                        var min: Long = snapshot.child(user.uid).child("minHours").value as Long
                        var max: Long = snapshot.child(user.uid).child("maxHours").value as Long
//                        min *= 100
//                        val roundedmin = min.toInt()
//                        min = roundedmin.toLong()
//                        min /=100
//
//                        max *= 100
//                        val roundedmax = max.toInt()
//                        max = roundedmax.toLong()
//                        max /=100
//
//                        dayTotal *= 100
//                        val roundeddayTotal = dayTotal.toInt()
//                        dayTotal = roundeddayTotal.toFloat()
//                        dayTotal /=100
                        if (min == 0L && max == 0L) {
                            binding.timeHeader.text =
                                "You have not yet set goals for hours spent on tasks."
                            binding.timeDescription.text = "Go to settings to set them."
                        } else {
                            dayTotal *= 100
                            val roundedDayTotal = dayTotal.toInt()
                            dayTotal = roundedDayTotal.toFloat()
                            dayTotal /= 100
                            if (dayTotal < min) {
                                val percent = ((dayTotal / min) * 100).toInt()
                                binding.progressbarDash.progress = percent
                                binding.progressbarPercent.text = "${percent}%"
                                var mintotal = min - dayTotal
                                mintotal *= 100
                                val roundedmintotal = mintotal.toInt()
                                mintotal = roundedmintotal.toFloat()
                                mintotal /= 100
                                binding.timeHeader.text =
                                    "You have ${(mintotal)} hours to go to reach your minimum goal."
                                binding.timeDescription.text =
                                    "$dayTotal hours of $min hours spent on tasks."
                            } else if (dayTotal < max) {
                                val percent = 100
                                binding.progressbarDash.progress = percent
                                binding.progressbarPercent.text = "${percent}%"
                                var maxtotal = max - dayTotal
                                maxtotal *= 100
                                val roundedmaxtotal = maxtotal.toInt()
                                maxtotal = roundedmaxtotal.toFloat()
                                maxtotal /= 100
                                binding.timeHeader.text =
                                    "You've reached your minimum goal! Well done!"
                                binding.timeDescription.text =
                                    "You have ${(maxtotal)} hours until you reach your maximum goal."
                            } else if (dayTotal >= max) {
                                val percent = ((dayTotal / max) * 100).toInt()
                                binding.progressbarDash.progress = percent
                                binding.progressbarPercent.text = "${percent}%"
                                var maxtotal2 = dayTotal - max
                                maxtotal2 *= 100
                                val roundedmaxtotal2 = maxtotal2.toInt()
                                maxtotal2 = roundedmaxtotal2.toFloat()
                                maxtotal2 /= 100
                                binding.timeHeader.text =
                                    "You've spent more hours than your maximum goal on tasks today."
                                binding.timeDescription.text = "You are ${(maxtotal2)} hours over."
                            }
                        }
                    } catch (e: Exception) {
//                        Toast.makeText(
//                            activity?.applicationContext,
//                            "Error: ${e.message}",
//                            Toast.LENGTH_LONG
//                        ).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    activity?.applicationContext,
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun calculateDurationInHours(startTime: String?, endTime: String?): Float {
        val dateFormat = java.text.SimpleDateFormat("HH:mm", Locale.ENGLISH)
        try {
            if (startTime != null && endTime != null) {
                val startDate = dateFormat.parse(startTime)
                val endDate = dateFormat.parse(endTime)
                if (startDate != null && endDate != null) {
                    val durationInMillis = endDate.time - startDate.time
                    var hours = durationInMillis.toFloat() / (1000 * 60 * 60)
                    hours *= 100
                    val rounded = hours.toInt()
                    hours = rounded.toFloat()
                    hours /= 100
                    return hours
                }
            }
        } catch (e: java.lang.Exception) {
            return 0f
        }
        return 0f
    }
}