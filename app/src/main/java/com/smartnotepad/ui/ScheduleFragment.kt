package com.smartnotepad.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.smartnotepad.R
import com.smartnotepad.adapter.CourseAdapter
import com.smartnotepad.data.DataStore
import com.smartnotepad.model.Course
import java.util.*

class ScheduleFragment : Fragment() {

    private lateinit var dataStore: DataStore
    private lateinit var courseAdapter: CourseAdapter
    private var selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    private val weekDayViews = mutableListOf<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataStore = DataStore.getInstance(requireContext())

        setupWeekDays(view)
        setupCourseList(view)
        setupFab(view)
    }

    private fun setupWeekDays(view: View) {
        val layout = view.findViewById<ViewGroup>(R.id.layout_week_days)
        val dayNames = resources.getStringArray(R.array.week_days)
        val dayShort = resources.getStringArray(R.array.week_day_short)

        // 转换 Android 星期格式 (1=周日, 2=周一, ...) 到我们的格式 (1=周一)
        val todayIndex = when (selectedDay) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        for (i in 0 until 7) {
            val tv = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_week_day, layout, false) as TextView
            tv.text = dayShort[i]
            tv.setOnClickListener {
                selectDay(i, layout)
            }
            layout.addView(tv)
            weekDayViews.add(tv)
        }

        selectDay(todayIndex, layout)
    }

    private fun selectDay(index: Int, parent: ViewGroup) {
        selectedDay = index + 1 // 1=周一, 2=周二, ...
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            child.isSelected = i == index
        }
        refreshCourses()
    }

    private fun setupCourseList(view: View) {
        val rvCourses = view.findViewById<RecyclerView>(R.id.rv_courses)
        rvCourses.layoutManager = LinearLayoutManager(requireContext())

        courseAdapter = CourseAdapter(
            courses = emptyList(),
            onItemLongClick = { course ->
                AlertDialog.Builder(requireContext())
                    .setTitle("删除课程")
                    .setMessage("确定要删除「${course.courseName}」吗？")
                    .setPositiveButton("确定") { _, _ ->
                        dataStore.deleteCourse(course.id)
                        refreshCourses()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        )
        rvCourses.adapter = courseAdapter
    }

    private fun setupFab(view: View) {
        view.findViewById<FloatingActionButton>(R.id.fab_add_course).setOnClickListener {
            showAddCourseDialog()
        }
    }

    private fun showAddCourseDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_course, null)

        val etName = dialogView.findViewById<TextInputEditText>(R.id.et_course_name)
        val etTeacher = dialogView.findViewById<TextInputEditText>(R.id.et_teacher)
        val etClassroom = dialogView.findViewById<TextInputEditText>(R.id.et_classroom)
        val spinnerDay = dialogView.findViewById<Spinner>(R.id.spinner_day)
        val etStartTime = dialogView.findViewById<TextInputEditText>(R.id.et_start_time)
        val etEndTime = dialogView.findViewById<TextInputEditText>(R.id.et_end_time)

        // 时间选择器
        etStartTime.setOnClickListener {
            showTimePicker { time -> etStartTime.setText(time) }
        }
        etEndTime.setOnClickListener {
            showTimePicker { time -> etEndTime.setText(time) }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                etName.error = "请输入课程名称"
                return@setOnClickListener
            }

            val course = Course(
                courseName = name,
                teacher = etTeacher.text.toString().trim(),
                classroom = etClassroom.text.toString().trim(),
                dayOfWeek = spinnerDay.selectedItemPosition + 1,
                startTime = etStartTime.text.toString(),
                endTime = etEndTime.text.toString()
            )
            dataStore.addCourse(course)
            refreshCourses()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                onTimeSelected(String.format("%02d:%02d", hour, minute))
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun refreshCourses() {
        val courses = dataStore.getCoursesByDay(selectedDay)
        courseAdapter.updateData(courses)
    }
}
