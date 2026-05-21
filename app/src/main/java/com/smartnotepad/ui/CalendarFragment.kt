package com.smartnotepad.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.view.Gravity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.smartnotepad.R
import com.smartnotepad.adapter.EventAdapter
import com.smartnotepad.alarm.BootReceiver
import com.smartnotepad.data.DataStore
import com.smartnotepad.model.CalendarEvent
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var dataStore: DataStore
    private lateinit var eventAdapter: EventAdapter
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private lateinit var tvMonthYear: TextView
    private lateinit var layoutCalendarGrid: GridLayout
    private lateinit var tvSelectedDate: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataStore = DataStore.getInstance(requireContext())

        tvMonthYear = view.findViewById(R.id.tv_month_year)
        layoutCalendarGrid = view.findViewById(R.id.layout_calendar_grid)
        tvSelectedDate = view.findViewById(R.id.tv_selected_date)

        setupWeekHeader(view)
        setupNavigation(view)
        setupEventList(view)
        setupFab(view)

        updateCalendar()
    }

    override fun onResume() {
        super.onResume()
        refreshEvents()
    }

    private fun setupWeekHeader(view: View) {
        val layout = view.findViewById<GridLayout>(R.id.layout_week_header)
        val weekDays = arrayOf("日", "一", "二", "三", "四", "五", "六")
        for (day in weekDays) {
            val tv = TextView(requireContext()).apply {
                text = day
                gravity = Gravity.CENTER
                textSize = 14f
                setTextColor(resources.getColor(R.color.gray_medium, null))
                layoutParams = ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
                layoutParams = (layoutParams as ViewGroup.LayoutParams).also {
                    it.width = 0
                }
            }
            layout.addView(tv)

            // 设置列权重
            val lp = tv.layoutParams as GridLayout.LayoutParams
            lp.width = 0
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            tv.layoutParams = lp
        }
    }

    private fun setupNavigation(view: View) {
        view.findViewById<ImageButton>(R.id.btn_prev_month).setOnClickListener {
            currentMonth--
            if (currentMonth < 0) {
                currentMonth = 11
                currentYear--
            }
            updateCalendar()
        }
        view.findViewById<ImageButton>(R.id.btn_next_month).setOnClickListener {
            currentMonth++
            if (currentMonth > 11) {
                currentMonth = 0
                currentYear++
            }
            updateCalendar()
        }
    }

    private fun updateCalendar() {
        tvMonthYear.text = String.format("%d年%02d月", currentYear, currentMonth + 1)
        layoutCalendarGrid.removeAllViews()

        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0=周日
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 填充空白
        for (i in 0 until firstDayOfWeek) {
            val tv = TextView(requireContext()).apply {
                text = ""
                layoutParams = ViewGroup.LayoutParams(0, 60.dpToPx())
            }
            layoutCalendarGrid.addView(tv)
            val lp = tv.layoutParams as GridLayout.LayoutParams
            lp.width = 0
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            tv.layoutParams = lp
        }

        // 填充日期
        for (day in 1..daysInMonth) {
            val dateStr = String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, day)
            val isToday = dateStr == today
            val isSelected = dateStr == selectedDate

            val tv = TextView(requireContext()).apply {
                text = day.toString()
                gravity = Gravity.CENTER
                textSize = 16f
                setPadding(8, 8, 8, 8)

                if (isSelected) {
                    setTextColor(resources.getColor(R.color.white, null))
                    setBackgroundColor(resources.getColor(R.color.blue_primary, null))
                } else if (isToday) {
                    setTextColor(resources.getColor(R.color.blue_dark, null))
                    setBackgroundResource(R.drawable.bg_today)
                } else {
                    setTextColor(resources.getColor(R.color.black, null))
                    setBackgroundColor(resources.getColor(android.R.color.transparent, null))
                }

                setOnClickListener {
                    selectedDate = dateStr
                    updateCalendar()
                    refreshEvents()
                }

                layoutParams = ViewGroup.LayoutParams(0, 60.dpToPx())
            }
            layoutCalendarGrid.addView(tv)
            val lp = tv.layoutParams as GridLayout.LayoutParams
            lp.width = 0
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            tv.layoutParams = lp
        }

        // 更新选中日期标题
        val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE)
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (date != null) {
                tvSelectedDate.text = sdf.format(date)
            }
        } catch (e: Exception) {
            tvSelectedDate.text = selectedDate
        }

        refreshEvents()
    }

    private fun setupEventList(view: View) {
        val rvEvents = view.findViewById<RecyclerView>(R.id.rv_events)
        rvEvents.layoutManager = LinearLayoutManager(requireContext())

        eventAdapter = EventAdapter(
            events = emptyList(),
            onItemClick = { event ->
                showEditEventDialog(event)
            },
            onItemLongClick = { event ->
                AlertDialog.Builder(requireContext())
                    .setTitle("删除事件")
                    .setMessage("确定要删除「${event.title}」吗？")
                    .setPositiveButton("确定") { _, _ ->
                        dataStore.deleteEvent(event.id)
                        BootReceiver.cancelAlarm(requireContext(), event.id)
                        refreshEvents()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        )
        rvEvents.adapter = eventAdapter
    }

    private fun setupFab(view: View) {
        view.findViewById<FloatingActionButton>(R.id.fab_add_event).setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun showAddEventDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_event, null)

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_event_title)
        val etDesc = dialogView.findViewById<TextInputEditText>(R.id.et_event_desc)
        val etDate = dialogView.findViewById<TextInputEditText>(R.id.et_event_date)
        val etTime = dialogView.findViewById<TextInputEditText>(R.id.et_event_time)
        val cbEnableReminder = dialogView.findViewById<CheckBox>(R.id.cb_enable_reminder)
        val layoutReminderOptions = dialogView.findViewById<View>(R.id.layout_reminder_options)
        val etRemindBefore = dialogView.findViewById<TextInputEditText>(R.id.et_remind_before)
        val spinnerRemindUnit = dialogView.findViewById<Spinner>(R.id.spinner_remind_unit)
        val cbVibrate = dialogView.findViewById<CheckBox>(R.id.cb_vibrate)

        etDate.setText(selectedDate)
        etDate.setOnClickListener {
            val parts = selectedDate.split("-")
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val dateStr = String.format("%04d-%02d-%02d", year, month + 1, day)
                    etDate.setText(dateStr)
                },
                parts[0].toInt(),
                parts[1].toInt() - 1,
                parts[2].toInt()
            ).show()
        }

        etTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    etTime.setText(String.format("%02d:%02d", hour, minute))
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        cbEnableReminder.setOnCheckedChangeListener { _, isChecked ->
            layoutReminderOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                etTitle.error = "请输入事件标题"
                return@setOnClickListener
            }

            val event = CalendarEvent(
                title = title,
                description = etDesc.text.toString().trim(),
                date = etDate.text.toString(),
                time = etTime.text.toString(),
                enableReminder = cbEnableReminder.isChecked,
                remindBefore = etRemindBefore.text.toString().toIntOrNull() ?: 10,
                remindUnit = spinnerRemindUnit.selectedItem.toString(),
                vibrate = cbVibrate.isChecked
            )
            dataStore.addEvent(event)

            // 设置提醒
            if (event.enableReminder) {
                BootReceiver.scheduleAlarm(requireContext(), event)
            }

            refreshEvents()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditEventDialog(event: CalendarEvent) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_event, null)

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_event_title)
        val etDesc = dialogView.findViewById<TextInputEditText>(R.id.et_event_desc)
        val etDate = dialogView.findViewById<TextInputEditText>(R.id.et_event_date)
        val etTime = dialogView.findViewById<TextInputEditText>(R.id.et_event_time)
        val cbEnableReminder = dialogView.findViewById<CheckBox>(R.id.cb_enable_reminder)
        val layoutReminderOptions = dialogView.findViewById<View>(R.id.layout_reminder_options)
        val etRemindBefore = dialogView.findViewById<TextInputEditText>(R.id.et_remind_before)
        val spinnerRemindUnit = dialogView.findViewById<Spinner>(R.id.spinner_remind_unit)
        val cbVibrate = dialogView.findViewById<CheckBox>(R.id.cb_vibrate)

        etTitle.setText(event.title)
        etDesc.setText(event.description)
        etDate.setText(event.date)
        etTime.setText(event.time)
        cbEnableReminder.isChecked = event.enableReminder
        etRemindBefore.setText(event.remindBefore.toString())
        cbVibrate.isChecked = event.vibrate

        // 设置提醒单位
        val units = resources.getStringArray(R.array.remind_units)
        for (i in units.indices) {
            if (units[i] == event.remindUnit) {
                spinnerRemindUnit.setSelection(i)
                break
            }
        }

        layoutReminderOptions.visibility = if (event.enableReminder) View.VISIBLE else View.GONE

        etDate.setOnClickListener {
            val parts = event.date.split("-")
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                },
                parts[0].toInt(),
                parts[1].toInt() - 1,
                parts[2].toInt()
            ).show()
        }

        etTime.setOnClickListener {
            val parts = event.time.split(":")
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    etTime.setText(String.format("%02d:%02d", hour, minute))
                },
                parts[0].toInt(),
                parts[1].toInt(),
                true
            ).show()
        }

        cbEnableReminder.setOnCheckedChangeListener { _, isChecked ->
            layoutReminderOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                etTitle.error = "请输入事件标题"
                return@setOnClickListener
            }

            // 取消旧提醒
            BootReceiver.cancelAlarm(requireContext(), event.id)

            val updatedEvent = event.copy(
                title = title,
                description = etDesc.text.toString().trim(),
                date = etDate.text.toString(),
                time = etTime.text.toString(),
                enableReminder = cbEnableReminder.isChecked,
                remindBefore = etRemindBefore.text.toString().toIntOrNull() ?: 10,
                remindUnit = spinnerRemindUnit.selectedItem.toString(),
                vibrate = cbVibrate.isChecked
            )
            dataStore.updateEvent(updatedEvent)

            // 设置新提醒
            if (updatedEvent.enableReminder) {
                BootReceiver.scheduleAlarm(requireContext(), updatedEvent)
            }

            refreshEvents()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun refreshEvents() {
        val events = dataStore.getEventsByDate(selectedDate)
        eventAdapter.updateData(events)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
