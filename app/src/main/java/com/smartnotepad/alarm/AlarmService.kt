package com.smartnotepad.alarm

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AlarmService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 前台服务用于确保提醒能正常工作
        return START_NOT_STICKY
    }
}
