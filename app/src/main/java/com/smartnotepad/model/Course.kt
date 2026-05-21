package com.smartnotepad.model

data class Course(
    var id: Long = System.currentTimeMillis(),
    var courseName: String = "",
    var teacher: String = "",
    var classroom: String = "",
    var dayOfWeek: Int = 1,        // 1=周一, 2=周二, ..., 7=周日
    var startTime: String = "",    // HH:mm
    var endTime: String = "",      // HH:mm
    var createTime: Long = System.currentTimeMillis()
)
