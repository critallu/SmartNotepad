package com.smartnotepad.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smartnotepad.model.CalendarEvent
import com.smartnotepad.model.Countdown
import com.smartnotepad.model.Course
import com.smartnotepad.model.Note

class DataStore private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smart_notepad_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        @Volatile
        private var INSTANCE: DataStore? = null

        fun getInstance(context: Context): DataStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataStore(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // ========== Notes ==========
    fun saveNotes(notes: List<Note>) {
        val json = gson.toJson(notes)
        prefs.edit().putString("notes", json).apply()
    }

    fun getNotes(): List<Note> {
        val json = prefs.getString("notes", "[]") ?: "[]"
        val type = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addNote(note: Note) {
        val notes = getNotes().toMutableList()
        notes.add(0, note)
        saveNotes(notes)
    }

    fun updateNote(note: Note) {
        val notes = getNotes().toMutableList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            notes[index] = note
            saveNotes(notes)
        }
    }

    fun deleteNote(noteId: Long) {
        val notes = getNotes().toMutableList()
        notes.removeAll { it.id == noteId }
        saveNotes(notes)
    }

    // ========== Calendar Events ==========
    fun saveEvents(events: List<CalendarEvent>) {
        val json = gson.toJson(events)
        prefs.edit().putString("events", json).apply()
    }

    fun getEvents(): List<CalendarEvent> {
        val json = prefs.getString("events", "[]") ?: "[]"
        val type = object : TypeToken<List<CalendarEvent>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getEventsByDate(date: String): List<CalendarEvent> {
        return getEvents().filter { it.date == date }
    }

    fun addEvent(event: CalendarEvent) {
        val events = getEvents().toMutableList()
        events.add(event)
        saveEvents(events)
    }

    fun updateEvent(event: CalendarEvent) {
        val events = getEvents().toMutableList()
        val index = events.indexOfFirst { it.id == event.id }
        if (index >= 0) {
            events[index] = event
            saveEvents(events)
        }
    }

    fun deleteEvent(eventId: Long) {
        val events = getEvents().toMutableList()
        events.removeAll { it.id == eventId }
        saveEvents(events)
    }

    // ========== Courses ==========
    fun saveCourses(courses: List<Course>) {
        val json = gson.toJson(courses)
        prefs.edit().putString("courses", json).apply()
    }

    fun getCourses(): List<Course> {
        val json = prefs.getString("courses", "[]") ?: "[]"
        val type = object : TypeToken<List<Course>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getCoursesByDay(dayOfWeek: Int): List<Course> {
        return getCourses().filter { it.dayOfWeek == dayOfWeek }
            .sortedBy { it.startTime }
    }

    fun addCourse(course: Course) {
        val courses = getCourses().toMutableList()
        courses.add(course)
        saveCourses(courses)
    }

    fun updateCourse(course: Course) {
        val courses = getCourses().toMutableList()
        val index = courses.indexOfFirst { it.id == course.id }
        if (index >= 0) {
            courses[index] = course
            saveCourses(courses)
        }
    }

    fun deleteCourse(courseId: Long) {
        val courses = getCourses().toMutableList()
        courses.removeAll { it.id == courseId }
        saveCourses(courses)
    }

    // ========== Countdowns ==========
    fun saveCountdowns(countdowns: List<Countdown>) {
        val json = gson.toJson(countdowns)
        prefs.edit().putString("countdowns", json).apply()
    }

    fun getCountdowns(): List<Countdown> {
        val json = prefs.getString("countdowns", "[]") ?: "[]"
        val type = object : TypeToken<List<Countdown>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addCountdown(countdown: Countdown) {
        val countdowns = getCountdowns().toMutableList()
        countdowns.add(0, countdown)
        saveCountdowns(countdowns)
    }

    fun deleteCountdown(countdownId: Long) {
        val countdowns = getCountdowns().toMutableList()
        countdowns.removeAll { it.id == countdownId }
        saveCountdowns(countdowns)
    }
}
