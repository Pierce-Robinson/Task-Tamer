package com.varsitycollege.tasktamer.data

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.varsitycollege.tasktamer.R

class DashAdapter(context: Context, cardModelArrayList: ArrayList<TimeSheetEntry?>) :
    ArrayAdapter<TimeSheetEntry?>(context, 0, cardModelArrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var listItemView = convertView
        if (listItemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listItemView = LayoutInflater.from(context).inflate(R.layout.dash_item, parent, false)
        }

        val timeModel: TimeSheetEntry? = getItem(position)
        val item = listItemView!!.findViewById<CardView>(R.id.dashContainer)
        item.setCardBackgroundColor(Color.parseColor(timeModel!!.colour))

        val title = listItemView.findViewById<TextView>(R.id.dashTask)
        val length = listItemView.findViewById<TextView>(R.id.dashLength)

        title.text = timeModel.description
        length.text = "${timeModel.startTime} to ${timeModel.endTime}"

        return listItemView
    }

}