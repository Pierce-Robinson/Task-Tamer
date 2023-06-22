package com.varsitycollege.tasktamer.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varsitycollege.tasktamer.R
import com.varsitycollege.tasktamer.data.Category
import com.varsitycollege.tasktamer.data.CategoryAdapter
import com.varsitycollege.tasktamer.data.TimeSheetEntry
import com.varsitycollege.tasktamer.databinding.FragmentStatisticsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatisticsFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    private lateinit var taskArrayList: ArrayList<TimeSheetEntry>
    private lateinit var startDate: String
    private lateinit var endDate: String
    private var _binding: FragmentStatisticsBinding? = null
    private lateinit var viewModel: StatisticsViewModel
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryArrayList: ArrayList<Category>

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        categoryRecyclerView = binding.recycleCategories
        categoryRecyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext)
        categoryRecyclerView.setHasFixedSize(true)
        categoryArrayList = arrayListOf()
        //Get unfiltered task data to initially populate category list
        getTaskData(false)
        getCategoryData()

        return binding.root
    }

    private fun getCategoryData() {

        database =
            FirebaseDatabase.getInstance("https://task-tamer-9c5f3-default-rtdb.europe-west1.firebasedatabase.app/")
        ref = database.getReference("Categories")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    categoryArrayList.clear()
                    for (catSnapshot in snapshot.children) {
                        val category = catSnapshot.getValue(Category::class.java)
                        if (category?.userId.equals(currentUserId)) {
                            categoryArrayList.add(category!!)
                        }
                    }
                    categoryRecyclerView.adapter = CategoryAdapter(categoryArrayList, taskArrayList)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskArrayList = arrayListOf()


        binding.DatePickerBtnF.setOnClickListener {
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select dates")
                    .setSelection(
                        Pair.create(
                            MaterialDatePicker.thisMonthInUtcMilliseconds(),
                            MaterialDatePicker.todayInUtcMilliseconds()
                        )
                    ).setTheme(R.style.MaterialCalendarTheme).build()

            dateRangePicker.addOnPositiveButtonClickListener { datePicked ->
                startDate = convertLongToDate(datePicked.first)
                endDate = convertLongToDate(datePicked.second)
                binding.DatePickerBtnF.text = "$startDate ➡️ $endDate"
                // Get filtered task data to send to category adapter
                getTaskData(true)
                getCategoryData()
            }

            dateRangePicker.show(parentFragmentManager, "Tag_picker")
        }
    }

    private fun getTaskData(isFiltered: Boolean) {
        // Initialize Firebase Database and DatabaseReference
        database =
            FirebaseDatabase.getInstance("https://task-tamer-9c5f3-default-rtdb.europe-west1.firebasedatabase.app/")
        ref = database.getReference("TimeSheetEntries")

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Query the tasks assigned to the current user
        val query = ref.orderByChild("userId").equalTo(currentUserId)

        // Add a ValueEventListener to fetch the tasks from Firebase
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    taskArrayList.clear()
                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(TimeSheetEntry::class.java)
                        if (isFiltered) {
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                            val buttonDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
                            try {
                                val date = task?.date?.let { dateFormat.parse(it) }
                                if (date != null) {
                                    if (date >= buttonDateFormat.parse(startDate) && date <= buttonDateFormat.parse(
                                            endDate
                                        )
                                    ) {
                                        taskArrayList.add(task)
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    activity?.applicationContext,
                                    e.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            taskArrayList.add(task!!)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    activity?.applicationContext,
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                ref.removeEventListener(this)
            }
        })
    }

    private fun convertLongToDate(longDate: Long): String {
        val date = Date(longDate)
        val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return format.format(date)
    }
}