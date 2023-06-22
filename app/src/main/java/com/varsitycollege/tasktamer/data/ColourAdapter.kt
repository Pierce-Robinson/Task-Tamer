package com.varsitycollege.tasktamer.data

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.tasktamer.R

class ColourAdapter(
    private val context: Context,
    private val colours: List<Int>,
    private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ColourAdapter.ColourViewHolder>() {

        inner class ColourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val colourView: View = itemView.findViewById(R.id.colourView)

            fun bind(colour: Int) {
                val backgroundDrawable = ContextCompat.getDrawable(context,
                    R.drawable.square_rounded_corners
                )?.mutate()
                backgroundDrawable?.colorFilter = PorterDuffColorFilter(colour, PorterDuff.Mode.SRC_IN)
                colourView.background = backgroundDrawable
                itemView.setOnClickListener { onItemClick(colour) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColourViewHolder {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.colour_item, parent, false)
            return ColourViewHolder(view)
        }

        override fun onBindViewHolder(holder: ColourViewHolder, position: Int) {
            val colour = colours[position]
            holder.bind(colour)
        }

        override fun getItemCount(): Int {
            return colours.size
        }
    }
