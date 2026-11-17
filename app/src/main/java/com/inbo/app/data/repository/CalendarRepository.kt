package com.inbo.app.data.repository

import com.inbo.app.data.local.CalendarDao
import com.inbo.app.model.CalendarDeadline
import com.inbo.app.model.CalendarEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth repository managing Inbo calendar events and deadlines.
 * Exposes reactive StateFlows for UI consumption and handles all SQLite operations
 * asynchronously through CalendarDao.
 */
class CalendarRepository(
    private val calendarDao: CalendarDao,
    private val scope: CoroutineScope
) {
    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    private val _deadlines = MutableStateFlow<List<CalendarDeadline>>(emptyList())
    val deadlines: StateFlow<List<CalendarDeadline>> = _deadlines.asStateFlow()

    init {
        scope.launch {
            _events.value = calendarDao.getAllEvents()
            _deadlines.value = calendarDao.getAllDeadlines()
        }
    }

    fun addEvent(
        title: String,
        timeRange: String,
        type: String,
        location: String? = null,
        date: String = ""
    ) {
        scope.launch {
            val created = calendarDao.insertEvent(title, timeRange, type, location, date)
            _events.value = _events.value + created
        }
    }

    fun deleteEvent(id: Int) {
        _events.value = _events.value.filter { it.id != id }
        scope.launch {
            calendarDao.deleteEvent(id)
        }
    }

    fun toggleDeadlineCompletion(id: Int, completed: Boolean) {
        // Optimistic UI update
        _deadlines.value = _deadlines.value.map {
            if (it.id == id) it.copy(completed = completed) else it
        }
        scope.launch {
            calendarDao.updateDeadlineCompletion(id, completed)
        }
    }

    fun addDeadline(
        title: String,
        dueDate: String,
        tag: String,
        date: String? = null
    ) {
        scope.launch {
            val created = calendarDao.insertDeadline(title, dueDate, tag, date)
            _deadlines.value = _deadlines.value + created
        }
    }

    fun deleteDeadline(id: Int) {
        _deadlines.value = _deadlines.value.filter { it.id != id }
        scope.launch {
            calendarDao.deleteDeadline(id)
        }
    }
}
