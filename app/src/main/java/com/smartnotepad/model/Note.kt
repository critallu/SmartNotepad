package com.smartnotepad.model

data class Note(
    var id: Long = System.currentTimeMillis(),
    var title: String = "",
    var content: String = "",
    var createTime: Long = System.currentTimeMillis(),
    var updateTime: Long = System.currentTimeMillis()
)
