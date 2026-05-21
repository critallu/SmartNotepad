package com.smartnotepad.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartnotepad.R
import com.smartnotepad.model.CalendarEvent

class EventAdapter(
    private var events: List<CalendarEvent>,
    private val onItemClick: (CalendarEvent) -> Unit,
    private val onItemLongClick: (CalendarEvent) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    fun updateData(newEvents: List<CalendarEvent>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_event_title)
        private val tvDesc: TextView = itemView.findViewById(R.id.tv_event_desc)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_event_time)
        private val ivReminder: ImageView = itemView.findViewById(R.id.iv_reminder_icon)

        fun bind(event: CalendarEvent) {
            tvTitle.text = event.title
            tvDesc.text = event.description.ifEmpty { "无描述" }
            tvTime.text = event.time.ifEmpty { "全天" }
            ivReminder.visibility = if (event.enableReminder) View.VISIBLE else View.GONE

            itemView.setOnClickListener { onItemClick(event) }
            itemView.setOnLongClickListener {
                onItemLongClick(event)
                true
            }
        }
    }
}
