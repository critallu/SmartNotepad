package com.smartnotepad.model

data class CalendarEvent(
    var id: Long = System.currentTimeMillis(),
    var title: String = "",
    var description: String = "",
    var date: String = "",          // yyyy-MM-dd
    var time: String = "",          // HH:mm
    var enableReminder: Boolean = false,
    var remindBefore: Int = 10,     // 提前提醒数值
    var remindUnit: String = "分钟前", // 分钟前/小时前/天前
    var vibrate: Boolean = true,
    var createTime: Long = System.currentTimeMillis()
)
