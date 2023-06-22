package com.varsitycollege.tasktamer

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.varsitycollege.tasktamer.data.Category
import com.varsitycollege.tasktamer.data.ColourAdapter
import com.varsitycollege.tasktamer.data.TimeSheetEntry
import com.varsitycollege.tasktamer.databinding.ActivityAddTaskBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalTime
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    //viewbinding
    //https://www.youtube.com/watch?v=z0F2QTAKsWU&ab_channel=PhilippLackner
    //accessed 3 June 2023
    private lateinit var binding: ActivityAddTaskBinding
    private val calendar = Calendar.getInstance()

    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    //camera vars
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var photoFile: File
    private lateinit var photoPreview: ImageView
    private var downloadUrl: String? = null

    //date vvars
    private val blankHour = 0
    private val blankMin = 0


    //need to possible change this
    private val FILE_NAME = "photo.jpg"

    //spinner vars
    private lateinit var selectCategorySpinner: Spinner
    private lateinit var adapter: ArrayAdapter<String>

    //switch vars
    private lateinit var billableSwitch: Switch
    private lateinit var billableLayout: TextInputLayout
    private lateinit var billableEditText: TextInputEditText

    //colour picker stuff
    private lateinit var colour_picker_button: Button
    private lateinit var colourPickerDialog: AlertDialog
    private lateinit var hex: String

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            //TODO: set newly created category as selected item
//            try {
//                val sharedPreferences = getPreferences(MODE_PRIVATE)
//                val createdCat = sharedPreferences.getString("name", "default")
//                val pos = adapter.getPosition(createdCat)
//                selectCategorySpinner.setSelection(pos)
//            } catch (e: Exception) {
//               //
//            }
        } else {
            logOut()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //camera stuff
        val addPhotoButton: MaterialButton = findViewById(R.id.addPhoto)
        addPhotoButton.setOnClickListener {
            launchCamera()
        }
        //setting colour picker btn
        hex = "#D03838"
        // Initialize the photoPreview ImageView
        photoPreview = findViewById(R.id.photoPreview)
        //hiding/showing billable
        billableSwitch = findViewById(R.id.BillableSwitch)
        billableLayout = findViewById(R.id.billable)
        billableEditText = findViewById(R.id.addtask_billableEditText)
        billableSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                billableLayout.visibility = View.VISIBLE
                billableEditText.visibility = View.VISIBLE
            } else {
                billableLayout.visibility = View.INVISIBLE
                billableEditText.visibility = View.INVISIBLE
                billableEditText.text?.clear()
            }
        }
        //listner for the spinner
        selectCategorySpinner = binding.selectCategory
        binding.dropDownCard.setOnClickListener {
            selectCategorySpinner.performClick()
        }
        //colour picker listner
        colour_picker_button = findViewById(R.id.colour_picker_button)

        val colorPickerButton = findViewById<Button>(R.id.colour_picker_button)
        colorPickerButton.setOnClickListener {
            showColourPickerDialog()
        }
        //Get categories from database and populate spinner
        getCategoryData()
        //date/time picker buttons
        binding.taskStartTime.setOnClickListener {
            showTimePickerStart(binding.taskStartTime)
        }
        binding.taskEndTime.setOnClickListener {
            showTimePickerEnd(binding.taskEndTime)
        }
        binding.DatePickerBtn.setOnClickListener {
            showDatePicker(binding.DatePickerBtn)
        }
        binding.categoryButton.setOnClickListener {
            val intent = Intent(this, AddCategoryActivity::class.java)
            startActivity(intent)
        }

        binding.addTaskButton.setOnClickListener {
            var valid: Boolean = true
            // Reset errors
            // TODO: Make this work with dark mode
            binding.addtaskTitle.error = null
            binding.taskStartTime.setTextColor(getColor(R.color.txt_colour_light))
            binding.taskEndTime.setTextColor(getColor(R.color.txt_colour_light))
            binding.DatePickerBtn.setTextColor(getColor(R.color.txt_colour_light))

            // Write a task to the database
            try {
                if (binding.addtaskTitleEditText.text.toString().isBlank()) {
                    binding.addtaskTitle.error = "Required."
                    valid = false
                }
                if (binding.taskStartTime.text.toString() == "Start Time") {
                    binding.taskStartTime.setTextColor(getColor(R.color.Btn_Red))
                    valid = false
                }else {
                    val startTimeText = binding.taskStartTime.text.toString()
                    val endTimeText = binding.taskEndTime.text.toString()
                    val startTime = LocalTime.parse(startTimeText)
                    val endTime = LocalTime.parse(endTimeText)
                    if (endTime.isBefore(startTime)) {
                        binding.taskStartTime.setTextColor(getColor(R.color.Btn_Red))
                        valid = false
                    }
                }
                if (binding.taskEndTime.text.toString() == "End Time") {
                    binding.taskEndTime.setTextColor(getColor(R.color.Btn_Red))
                    valid = false
                }else {
                    val startTimeText = binding.taskStartTime.text.toString()
                    val endTimeText = binding.taskEndTime.text.toString()
                    val startTime = LocalTime.parse(startTimeText)
                    val endTime = LocalTime.parse(endTimeText)
                    if (startTime.isAfter(endTime)) {
                        Toast.makeText(this, "Error: End time cannot be before start time", Toast.LENGTH_LONG).show()
                        binding.taskEndTime.setTextColor(getColor(R.color.Btn_Red))
                        valid = false
                    }
                }
                if (binding.DatePickerBtn.text.toString() == "Task Date") {
                    binding.DatePickerBtn.setTextColor(getColor(R.color.Btn_Red))
                    valid = false
                }
                if (valid) {
                    database =
                        FirebaseDatabase.getInstance("https://task-tamer-9c5f3-default-rtdb.europe-west1.firebasedatabase.app/")
                    ref = database.getReference("TimeSheetEntries")
                    val key = ref.push().key
                    val task = TimeSheetEntry(
                        id = key!!,
                        description = binding.addtaskTitleEditText.text.toString(),
                        date = binding.DatePickerBtn.text.toString(),
                        startTime = binding.taskStartTime.text.toString(),
                        endTime = binding.taskEndTime.text.toString(),
                        billable = binding.BillableSwitch.isChecked,
                        imageId = downloadUrl,
                        colour = hex,
                        categoryId = binding.selectCategory.selectedItem.toString(),
                        userId = FirebaseAuth.getInstance().currentUser!!.uid
                    )
                    if (task.billable == true) {
                        task.rate = binding.addtaskBillableEditText.text.toString().toDouble()
                    }
                    task.id?.let { it1 ->
                        ref.child(it1).setValue(task).addOnSuccessListener {
                            Toast.makeText(applicationContext, "Success.", Toast.LENGTH_LONG).show()
                            //Go back to home activity after successful creation
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getCategoryData() {
        database =
            FirebaseDatabase.getInstance("https://task-tamer-9c5f3-default-rtdb.europe-west1.firebasedatabase.app/")
        ref = database.getReference("Categories")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Query the tasks assigned to the current user
        val query = ref.orderByChild("userId").equalTo(currentUserId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val categoryList: MutableList<String> = mutableListOf()
                    for (catSnapshot in snapshot.children) {
                        val category = catSnapshot.getValue(Category::class.java)
                        if (category != null) {
                            category.title?.let { it1 -> categoryList.add(it1) }
                        }
                    }
                    adapter = ArrayAdapter(
                        applicationContext,
                        R.layout.spinner_item,
                        categoryList
                    )
                    adapter.setDropDownViewResource(R.layout.spinner_item)
                    binding.selectCategory.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

//time picker
    private fun showTimePickerEnd(button: Button) {
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(selectedTime.time)
                button.text = formattedTime
                binding.taskEndTime.setTextColor(getColor(R.color.txt_colour_light))
            },
            initialHour,
            initialMinute,
            true // Set to true for 24-hour format, false for AM/PM format
        )
        timePickerDialog.show()
    }

    private fun showTimePickerStart(button: Button) {
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(selectedTime.time)
                button.text = formattedTime
                binding.taskStartTime.setTextColor(getColor(R.color.txt_colour_light))
            },
            initialHour,
            initialMinute,
            true // Set to true for 24-hour format, false for AM/PM format
        )
        timePickerDialog.show()
    }

    //date picker
    private fun showDatePicker(button: Button) {
        val initialDate = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                initialDate.set(Calendar.YEAR, year)
                initialDate.set(Calendar.MONTH, month)
                initialDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(initialDate.time)
                button.text = formattedDate
                binding.DatePickerBtn.setTextColor(getColor(R.color.txt_colour_light))
            },
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }


    //camera functions
    private fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFile(FILE_NAME)
        // takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)
        val fileProvider =
            FileProvider.getUriForFile(this, "com.varsitycollege.fileprovider", photoFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // val imageBitmap = data?.extras?.get("data") as Bitmap
            val imageBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            photoPreview.setImageBitmap(imageBitmap)

            uploadImageToFirebaseStorage(imageBitmap)
        }
    }

    private fun uploadImageToFirebaseStorage(imageBitmap: Bitmap) {
        // Create a unique file name for the image
        val fileName = "photo_${System.currentTimeMillis()}.jpg"

        // Get a reference to the Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("images/")

        // Create a reference to the file location in Firebase Storage
        val imageRef = storageRef.child(fileName)

        // Convert the Bitmap to bytes
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        // Upload the image to Firebase Storage
        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Image upload successful
                Toast.makeText(
                    this,
                    "Image uploaded to Firebase Storage",
                    Toast.LENGTH_SHORT
                ).show()

                imageRef.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uri = task.result
                        downloadUrl = uri.toString()
                    } else {
                        // Image upload failed
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    //colour picker
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
            layoutManager = GridLayoutManager(this@AddTaskActivity, numColumns)
            setPadding(padding, dpToPx(20), padding, padding) // Convert padding to pixels
            adapter = ColourAdapter(this@AddTaskActivity, colors) { selectedColor ->
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

    fun logOut() {
        Toast.makeText(
            applicationContext,
            "Could not find user, logging out.",
            Toast.LENGTH_LONG
        ).show()
        FirebaseAuth.getInstance().signOut()
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}
