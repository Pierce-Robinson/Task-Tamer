package com.varsitycollege.tasktamer


import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.varsitycollege.tasktamer.data.Category
import com.varsitycollege.tasktamer.data.CategoryAdapter

class cameratest : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryArrayList: ArrayList<Category>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cameratest)

        categoryRecyclerView = findViewById(R.id.recycle_Categories)
        categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryRecyclerView.setHasFixedSize(true)
        categoryArrayList = arrayListOf()
        getCategoryData()

    }

    //Read categories from firebase and add them to recyclerview, adapted from
    //https://www.youtube.com/watch?v=VVXKVFyYQdQ&ab_channel=Foxandroid
    //accessed 4 June 2023
    private fun getCategoryData() {

        database = FirebaseDatabase.getInstance("https://task-tamer-9c5f3-default-rtdb.europe-west1.firebasedatabase.app/")
        ref = database.getReference("Categories")

        ref.addValueEventListener( object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    for (catSnapshot in snapshot.children) {
                        val category = catSnapshot.getValue(Category::class.java)
                        categoryArrayList.add(category!!)
                    }
                    //categoryRecyclerView.adapter = CategoryAdapter(categoryArrayList)

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

    fun test() {
        //val listV: ListView = findViewById(R.id.lstvCategories)

        //Get categories from database and populate spinner

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var list = mutableListOf<Category>()

                for (pulledCat in snapshot.children) {
                    val cat: Category? = pulledCat.getValue(Category::class.java)
                    if (cat != null) {
                        list.add(cat)
                        Toast.makeText(
                            applicationContext,
                            "${cat.title}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                var catAdapter = ArrayAdapter(
                    this@cameratest,
                    android.R.layout.simple_spinner_dropdown_item, list
                )
                //listV.adapter = catAdapter
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
}

