package com.varsitycollege.tasktamer.data

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.tasktamer.R
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Locale


//adapted from
//https://www.youtube.com/watch?v=VVXKVFyYQdQ&ab_channel=Foxandroid
//accessed 4 June 2023
class CategoryAdapter(private val categoryList: ArrayList<Category>, private val taskList: ArrayList<TimeSheetEntry>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {

        val currentItem = categoryList[position]
        holder.title.text = currentItem.title
        holder.description.text = currentItem.description
        holder.client.text = currentItem.client
        holder.deadline.text = currentItem.deadline

        var time = 0f
        //Calculate hours spent on this category
        for (task in taskList) {
            if (task.categoryId.equals(currentItem.title)) {
                time += calculateDurationInHours(task.startTime, task.endTime)
            }
        }
        time *= 100
        val rounded = time.toInt()
        time = rounded.toFloat()
        time /= 100

        holder.time.text = "$time Hours"
        //Set background colour of current item
        //https://stackoverflow.com/questions/49836702/when-i-change-the-background-of-a-card-view-the-corner-radius-is-reset
        //user answered
        //https://stackoverflow.com/users/7018008/dulanga
        //accessed 4 June 2023
        holder.test.setCardBackgroundColor(Color.parseColor(currentItem.colour))

    }

    private fun calculateDurationInHours(startTime: String?, endTime: String?): Float {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        try {
            if (startTime != null && endTime != null) {
                val startDate = dateFormat.parse(startTime)
                val endDate = dateFormat.parse(endTime)
                if (startDate != null && endDate != null) {
                    val durationInMillis = endDate.time - startDate.time
                    var hours = durationInMillis.toFloat() / (1000 * 60 * 60)
                    return hours
                }
            }
        } catch (e: Exception) {
            return 0f
        }
        return 0f
    }

    class CategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val client: TextView = itemView.findViewById(R.id.tvClient)
        val deadline: TextView = itemView.findViewById(R.id.tvDeadline)
        val time: TextView = itemView.findViewById(R.id.tvTime)

        val test: CardView = itemView.findViewById(R.id.container)

    }

}