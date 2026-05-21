package com.smartnotepad.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.smartnotepad.R
import com.smartnotepad.adapter.EventAdapter
import com.smartnotepad.data.DataStore
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var dataStore: DataStore
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataStore = DataStore.getInstance(requireContext())

        setupGreeting(view)
        setupQuickCards(view)
        setupTodayEvents(view)
    }

    override fun onResume() {
        super.onResume()
        refreshTodayEvents()
    }

    private fun setupGreeting(view: View) {
        val tvGreeting = view.findViewById<TextView>(R.id.tv_greeting)
        val tvDate = view.findViewById<TextView>(R.id.tv_date)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..5 -> "夜深了"
            in 6..11 -> "早上好"
            in 12..13 -> "中午好"
            in 14..17 -> "下午好"
            else -> "晚上好"
        }
        tvGreeting.text = "$greeting！"

        val sdf = SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINESE)
        tvDate.text = sdf.format(Date())
    }

    private fun setupQuickCards(view: View) {
        view.findViewById<MaterialCardView>(R.id.card_schedule).setOnClickListener {
            findNavController().navigate(R.id.navigation_schedule)
        }
        view.findViewById<MaterialCardView>(R.id.card_calendar).setOnClickListener {
            findNavController().navigate(R.id.navigation_calendar)
        }
        view.findViewById<MaterialCardView>(R.id.card_countdown).setOnClickListener {
            findNavController().navigate(R.id.navigation_countdown)
        }
        view.findViewById<MaterialCardView>(R.id.card_notes).setOnClickListener {
            findNavController().navigate(R.id.navigation_notes)
        }
    }

    private fun setupTodayEvents(view: View) {
        val rvEvents = view.findViewById<RecyclerView>(R.id.rv_today_events)
        rvEvents.layoutManager = LinearLayoutManager(requireContext())

        eventAdapter = EventAdapter(
            events = emptyList(),
            onItemClick = { event ->
                // 点击事件 - 可以查看详情
            },
            onItemLongClick = { event ->
                // 长按删除
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("删除事件")
                    .setMessage("确定要删除「${event.title}」吗？")
                    .setPositiveButton("确定") { _, _ ->
                        dataStore.deleteEvent(event.id)
                        refreshTodayEvents()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        )
        rvEvents.adapter = eventAdapter
    }

    private fun refreshTodayEvents() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val todayEvents = dataStore.getEventsByDate(today)
        eventAdapter.updateData(todayEvents)
    }
}
