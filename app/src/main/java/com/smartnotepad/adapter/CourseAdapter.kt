package com.smartnotepad.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartnotepad.R
import com.smartnotepad.model.Course

class CourseAdapter(
    private var courses: List<Course>,
    private val onItemLongClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    fun updateData(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount() = courses.size

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tv_course_time)
        private val tvName: TextView = itemView.findViewById(R.id.tv_course_name)
        private val tvTeacher: TextView = itemView.findViewById(R.id.tv_course_teacher)
        private val tvClassroom: TextView = itemView.findViewById(R.id.tv_course_classroom)

        fun bind(course: Course) {
            tvTime.text = "${course.startTime} - ${course.endTime}"
            tvName.text = course.courseName
            tvTeacher.text = "教师: ${course.teacher.ifEmpty { "未知" }}"
            tvClassroom.text = "📍 ${course.classroom.ifEmpty { "未指定" }}"

            itemView.setOnLongClickListener {
                onItemLongClick(course)
                true
            }
        }
    }
}
