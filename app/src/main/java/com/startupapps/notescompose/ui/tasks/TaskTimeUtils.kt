package com.startupapps.notescompose.ui.tasks

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.ui.graphics.Color
import com.startupapps.notescompose.receiver.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class TaskPriorityStyle(
    val label: String,
    val accent: Color,
    val container: Color
)

fun taskPriorityStyle(priority: Int): TaskPriorityStyle {
    return when (priority) {
        2 -> TaskPriorityStyle(
            label = "Высокий",
            accent = Color(0xFFC2410C),
            container = Color(0xFFFFEDD5)
        )

        1 -> TaskPriorityStyle(
            label = "Средний",
            accent = Color(0xFF0F766E),
            container = Color(0xFFCCFBF1)
        )

        else -> TaskPriorityStyle(
            label = "Низкий",
            accent = Color(0xFF4F46E5),
            container = Color(0xFFE0E7FF)
        )
    }
}

fun formatReminderShort(reminderTime: Long, currentTime: Long): String {
    val calendar = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = reminderTime }
    return when {
        isSameDay(calendar, target) -> "Сегодня"
        isTomorrow(calendar, target) -> "Завтра"
        reminderTime < currentTime -> "Просрочено"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(reminderTime))
    }
}

fun formatReminderDateTime(reminderTime: Long, currentTime: Long): String {
    val now = Calendar.getInstance().apply { timeInMillis = currentTime }
    val target = Calendar.getInstance().apply { timeInMillis = reminderTime }
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(reminderTime))
    return when {
        isSameDay(now, target) -> "Сегодня, $time"
        isTomorrow(now, target) -> "Завтра, $time"
        now.get(Calendar.YEAR) == target.get(Calendar.YEAR) ->
            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(reminderTime))

        else -> SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(reminderTime))
    }
}

fun getTimeLeftText(reminderTime: Long, currentTime: Long): String {
    val diff = reminderTime - currentTime
    if (diff <= 0) return "Время истекло"

    val days = TimeUnit.MILLISECONDS.toDays(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60

    return when {
        days > 0 -> "Осталось $days д. $hours ч."
        hours > 0 -> "Осталось $hours ч. $minutes мин."
        minutes > 0 -> "Скоро: $minutes мин."
        else -> "Меньше минуты"
    }
}

fun scheduleNotification(context: Context, message: String, timeInMillis: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("TASK_MESSAGE", message)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        System.currentTimeMillis().toInt(),
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

internal fun localDateToUtcMidnight(value: Long): Long {
    val localCalendar = Calendar.getInstance().apply {
        timeInMillis = value
    }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.YEAR, localCalendar.get(Calendar.YEAR))
        set(Calendar.MONTH, localCalendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, localCalendar.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

internal fun combineReminderDateAndTime(
    selectedDateMillis: Long,
    hour: Int,
    minute: Int
): Long {
    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = selectedDateMillis
    }
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun isSameDay(first: Calendar, second: Calendar): Boolean {
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}

private fun isTomorrow(now: Calendar, target: Calendar): Boolean {
    val tomorrow = (now.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, 1)
    }
    return isSameDay(tomorrow, target)
}
