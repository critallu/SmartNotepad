package com.smartnotepad.model

data class Countdown(
    var id: Long = System.currentTimeMillis(),
    var title: String = "",
    var targetDate: String = "",   // yyyy-MM-dd
    var createTime: Long = System.currentTimeMillis()
)
