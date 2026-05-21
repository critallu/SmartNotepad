package com.smartnotepad.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartnotepad.data.DataStore
import java.text.SimpleDateFormat
import java.util.*

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 设备重启后重新设置所有提醒
            rescheduleAllAlarms(context)
        }
    }

    private fun rescheduleAllAlarms(context: Context) {
        val dataStore = DataStore.getInstance(context)
        val events = dataStore.getEvents()

        for (event in events) {
            if (event.enableReminder) {
                scheduleAlarm(context, event)
            }
        }
    }

    companion object {
        fun scheduleAlarm(context: Context, event: com.smartnotepad.model.CalendarEvent) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val eventDateTime = sdf.parse("${event.date} ${event.time}") ?: return

                // 计算提醒时间
                val remindMillis = when (event.remindUnit) {
                    "分钟前" -> eventDateTime.time - event.remindBefore * 60 * 1000L
                    "小时前" -> eventDateTime.time - event.remindBefore * 60 * 60 * 1000L
                    "天前" -> eventDateTime.time - event.remindBefore * 24 * 60 * 60 * 1000L
                    else -> eventDateTime.time - 10 * 60 * 1000L
                }

                // 如果提醒时间已过，不设置
                if (remindMillis <= System.currentTimeMillis()) return

                val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("event_title", event.title)
                    putExtra("event_desc", event.description)
                    putExtra("vibrate", event.vibrate)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    event.id.toInt(),
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmManager =
                    context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    remindMillis,
                    pendingIntent
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun cancelAlarm(context: Context, eventId: Long) {
            val alarmIntent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                eventId.toInt(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}
