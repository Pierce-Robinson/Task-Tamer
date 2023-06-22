package com.varsitycollege.tasktamer.data

import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.varsitycollege.tasktamer.R
import java.util.Locale


//adapted from
//https://www.youtube.com/watch?v=VVXKVFyYQdQ&ab_channel=Foxandroid
//accessed 4 June 2023
class TimeAdapter(private val timeList: ArrayList<TimeSheetEntry>) :
    RecyclerView.Adapter<TimeAdapter.TimeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {

        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TimeViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return timeList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val currentItem = timeList[position]
        holder.description.text = currentItem.description


        try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(currentItem.date)
            val formattedDate = outputFormat.format(date)
            holder.length.text = formattedDate
        } catch (e: Exception) {
            holder.length.text = "Invalid date value."
        }

        //Set background colour of current item
        //https://stackoverflow.com/questions/49836702/when-i-change-the-background-of-a-card-view-the-corner-radius-is-reset
        //user answered
        //https://stackoverflow.com/users/7018008/dulanga
        //accessed 4 June 2023
        holder.test.setCardBackgroundColor(Color.parseColor(currentItem.colour))
        //Button icon colour
        //https://www.tutorialkart.com/kotlin-android/change-icon-color-of-floating-action-button-in-android/
        //accessed 5 June 2023
        holder.button.imageTintList = ColorStateList.valueOf(Color.parseColor(currentItem.colour))
        if (currentItem.imageId.isNullOrEmpty()) {
            holder.shapeableImageView.setImageResource(R.drawable.main_logo)
        } else {
            // Load the image from the download URL
            Glide.with(holder.itemView.context)
                .load(currentItem.imageId)
                .centerCrop()
                .into(holder.shapeableImageView)
        }

    }

    class TimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.tvTask)
        val length: TextView = itemView.findViewById(R.id.tvLength)
        val test: CardView = itemView.findViewById(R.id.taskContainer)
        val button: FloatingActionButton = itemView.findViewById(R.id.fab_addTask)
        val shapeableImageView: ShapeableImageView = itemView.findViewById(R.id.RecycleImageView)
    }

}