package com.smartnotepad.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartnotepad.R
import com.smartnotepad.model.Countdown
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CountdownAdapter(
    private var countdowns: List<Countdown>,
    private val onItemLongClick: (Countdown) -> Unit
) : RecyclerView.Adapter<CountdownAdapter.CountdownViewHolder>() {

    fun updateData(newCountdowns: List<Countdown>) {
        countdowns = newCountdowns
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountdownViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_countdown, parent, false)
        return CountdownViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountdownViewHolder, position: Int) {
        holder.bind(countdowns[position])
    }

    override fun getItemCount() = countdowns.size

    inner class CountdownViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_countdown_title)
        private val tvDays: TextView = itemView.findViewById(R.id.tv_countdown_days)
        private val tvDetail: TextView = itemView.findViewById(R.id.tv_countdown_detail)

        fun bind(countdown: Countdown) {
            tvTitle.text = countdown.title

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDate = sdf.parse(countdown.targetDate)
            val now = Date()

            if (targetDate != null) {
                val diffMs = targetDate.time - now.time
                val days = TimeUnit.MILLISECONDS.toDays(diffMs)

                if (days > 0) {
                    tvDays.text = days.toString()
                    tvDays.setTextColor(itemView.context.getColor(R.color.green_success))
                    tvDetail.text = "距离 ${countdown.targetDate}"
                } else if (days == 0L) {
                    tvDays.text = "今天!"
                    tvDays.setTextColor(itemView.context.getColor(R.color.orange_accent))
                    tvDetail.text = countdown.targetDate
                } else {
                    tvDays.text = "已过"
                    tvDays.setTextColor(itemView.context.getColor(R.color.red_alert))
                    tvDetail.text = "${-days}天前 (${countdown.targetDate})"
                }
            }

            itemView.setOnLongClickListener {
                onItemLongClick(countdown)
                true
            }
        }
    }
}
