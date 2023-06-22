package com.varsitycollege.tasktamer

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varsitycollege.tasktamer.data.Category
import com.varsitycollege.tasktamer.data.ColourAdapter
import com.varsitycollege.tasktamer.databinding.ActivityAddCategoryBinding
import java.util.Locale


class AddCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var colourPickerDialog: AlertDialog
    private lateinit var colour_picker_button: Button
    private lateinit var dateTimePickerButton: Button
    private val calendar = Calendar.getInstance()
    private lateinit var hex: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hex = "#D03838"
        colour_picker_button = findViewById(R.id.colour_picker_button)

        val colorPickerButton = findViewById<Button>(R.id.colour_picker_button)
        colorPickerButton.setOnClickListener {
            showColourPickerDialog()
        }

        val addCategory = findViewById<MaterialButton>(R.id.cat_AddCategoryBtn)
        addCategory.setOnClickListener {
            // Reset errors
            binding.catName.error = null
            // Prevent adding if some fields are blank
            if (binding.catNameEditText.text.toString().isBlank()) {
                binding.catName.error = "Required"
            } else {
                checkTitle(binding.catNameEditText.text.toString())
            }
        }

        dateTimePickerButton = findViewById(R.id.datePickerButton)
        dateTimePickerButton.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun checkTitle(title: String) {

        val database =
            FirebaseDatabase.getInstance("https://task-tamer-9c5f3-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = database.getReference("Categories")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var titleDuplicate = false
                    for (catSnapshot in snapshot.children) {
                        val category = catSnapshot.getValue(Category::class.java)
                        if (category?.title == title && currentUserId == category.userId) {
                            titleDuplicate = true
                            binding.catName.error = "Duplicate category name."
                            break
                        }
                    }
                    //If not duplicate, add the category
                    if (!titleDuplicate) {
                        try {
                            var desc = binding.catDescriptionEditText.text.toString()
                            if (desc.isBlank()) {
                                desc = "No Description"
                            }
                            var cli = binding.catClientEditText.text.toString()
                            if (cli.isBlank()) {
                                cli = "No Client"
                            }
                            var dead = dateTimePickerButton.text.toString()
                            if (dead == "Select a Deadline (Optional)") {
                                dead = "No Deadline"
                            }
                            val key = ref.push().key
                            val category = Category(
                                id = key!!,
                                title = binding.catNameEditText.text.toString(),
                                description = desc,
                                client = cli,
                                deadline = dead,
                                colour = hex,
                                userId = FirebaseAuth.getInstance().currentUser!!.uid
                            )
                            category.id?.let { it1 ->
                                ref.child(it1).setValue(category).addOnSuccessListener {
                                    Toast.makeText(applicationContext, "Success.", Toast.LENGTH_LONG).show()
                                    //Go back to task creation after successful category creation
                                    //TODO: set newly created category as selected item
//                                    val sharedPreferences = getPreferences(MODE_PRIVATE)
//                                    val editor = sharedPreferences.edit()
//                                    editor.putString("name", category.title)
//                                    editor.apply()
                                    finish()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    applicationContext,
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun showDateTimePicker() {
        val initialDateTime = calendar.time

        val dateTimePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
                val initialMinute = calendar.get(Calendar.MINUTE)

                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        val selectedDateTime = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                        }

                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val formattedDateTime = dateFormat.format(selectedDateTime.time)
                        dateTimePickerButton.text = formattedDateTime
                    },
                    initialHour,
                    initialMinute,
                    true
                )

                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dateTimePickerDialog.show()
    }


    private fun showColourPickerDialog() {

        val colors = listOf(
            Color.parseColor("#FFF44336"),
            Color.parseColor("#FFE91E63"),
            Color.parseColor("#FF9C27B0"),
            Color.parseColor("#FF3F51B5"),
            Color.parseColor("#FF00BCD4"),
            Color.parseColor("#FFC107"),
            Color.parseColor("#FF009688"),
            Color.parseColor("#FF8BC34A"),
            Color.parseColor("#FFFF9800"),
            Color.parseColor("#FFFF5722"),
            Color.parseColor("#FFFF9E80"),
            Color.parseColor("#FF6D00"),
            Color.parseColor("#FF313435"),
            Color.parseColor("#FFFF80AB"),
            Color.parseColor("#FFB388FF"),
            Color.parseColor("#FF84FFFF"),
            Color.parseColor("#FFF50057"),
            Color.parseColor("#404551"),
            Color.parseColor("#D03838"),
            Color.parseColor("#4B8CFF"),
            Color.parseColor("#FA661B"),
            Color.parseColor("#FBB03B"),
            Color.parseColor("#4B9E69"),
            Color.parseColor("#C2185B"),
            Color.parseColor("#4A148C"),
            Color.parseColor("#0277BD"),
            Color.parseColor("#675D50"),
            Color.parseColor("#795548"),
            Color.parseColor("#00FF00"),
            Color.parseColor("#FFD700")
        )

        val numColumns = 5 // Desired number of columns
        val padding = dpToPx(15) // Convert 15 dp to pixels
        val spacing = dpToPx(15) // Set the spacing between items in dp

        val recyclerView = RecyclerView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = GridLayoutManager(this@AddCategoryActivity, numColumns)
            setPadding(padding, dpToPx(20), padding, padding) // Convert padding to pixels
            adapter = ColourAdapter(this@AddCategoryActivity, colors) { selectedColor ->
                // Do something with the selected color

                // Change Background Color
                colour_picker_button.setBackgroundColor(selectedColor)
                hex = "#" + Integer.toHexString(selectedColor)
                // Change the App Bar Background Color
                colourPickerDialog.dismiss()

            }
            addItemDecoration(GridSpacingItemDecoration(numColumns, spacing, true))
        }

        colourPickerDialog = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
            .setTitle("Choose a color")
            .setView(recyclerView)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        colourPickerDialog.show()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

}



