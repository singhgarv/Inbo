package com.inbo.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.inbo.app.model.CalendarDeadline
import com.inbo.app.model.CalendarEvent
import com.inbo.app.model.CapturedNotification
import com.inbo.app.model.InboSettings
import com.inbo.app.model.InboTask
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SQLite OpenHelper managing the local Inbo database (tasks, calendar events, deadlines, settings, and captured notifications).
 * Uses Android's built-in android.database.sqlite framework, requiring zero extra dependencies
 * while providing robust ACID transactions, single database consolidation, and schema evolution.
 */
class InboDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        const val DATABASE_NAME = "inbo_tasks.db"
        const val DATABASE_VERSION = 5

        // 72-hour retention threshold for captured notification content
        const val RETENTION_PERIOD_MS = 72L * 60L * 60L * 1000L

        // Tasks table
        const val TABLE_TASKS = "tasks"
        const val COLUMN_TASK_ID = "id"
        const val COLUMN_TASK_TITLE = "title"
        const val COLUMN_TASK_COMPLETED = "completed"
        const val COLUMN_TASK_CATEGORY = "category"
        const val COLUMN_TASK_DUE_TIME = "due_time"
        const val COLUMN_TASK_CREATED_AT = "created_at"

        // Calendar Events table
        const val TABLE_EVENTS = "calendar_events"
        const val COLUMN_EVENT_ID = "id"
        const val COLUMN_EVENT_TITLE = "title"
        const val COLUMN_EVENT_TIME_RANGE = "time_range"
        const val COLUMN_EVENT_TYPE = "type"
        const val COLUMN_EVENT_LOCATION = "location"
        const val COLUMN_EVENT_DATE = "date"
        const val COLUMN_EVENT_CREATED_AT = "created_at"

        // Calendar Deadlines table
        const val TABLE_DEADLINES = "calendar_deadlines"
        const val COLUMN_DEADLINE_ID = "id"
        const val COLUMN_DEADLINE_TITLE = "title"
        const val COLUMN_DEADLINE_DUE_DATE = "due_date"
        const val COLUMN_DEADLINE_TAG = "tag"
        const val COLUMN_DEADLINE_COMPLETED = "completed"
        const val COLUMN_DEADLINE_DATE = "date"
        const val COLUMN_DEADLINE_CREATED_AT = "created_at"

        // Settings Key-Value table
        const val TABLE_SETTINGS = "settings"
        const val COLUMN_SETTING_KEY = "key"
        const val COLUMN_SETTING_VALUE = "value"

        // Captured Notifications table
        const val TABLE_NOTIFICATIONS = "captured_notifications"
        const val COLUMN_NOTIF_ID = "id"
        const val COLUMN_NOTIF_KEY = "notification_key"
        const val COLUMN_NOTIF_PACKAGE = "package_name"
        const val COLUMN_NOTIF_SENDER = "sender_or_title"
        const val COLUMN_NOTIF_SNIPPET = "message_snippet"
        const val COLUMN_NOTIF_TIMESTAMP = "timestamp"
        const val COLUMN_NOTIF_IS_GROUP = "is_group"
        const val COLUMN_NOTIF_IS_SUMMARY = "is_summary"
        const val COLUMN_NOTIF_IS_DISMISSED = "is_dismissed"
        const val COLUMN_NOTIF_IS_READ = "is_read"

        // Setting keys
        const val KEY_DARK_MODE = "dark_mode_enabled"
        const val KEY_QUIET_HOURS = "quiet_hours_enabled"
        const val KEY_MORNING_DIGEST = "morning_digest_enabled"
        const val KEY_TASK_REMINDERS = "task_reminders_enabled"
        const val KEY_HAPTIC_FEEDBACK = "haptic_feedback_enabled"
        const val KEY_AUTO_ARCHIVE = "auto_archive_enabled"
        const val KEY_DEFAULT_LAUNCH_SCREEN = "default_launch_screen"
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTasksTable(db)
        seedStarterTasks(db)
        createCalendarTables(db)
        seedStarterCalendar(db)
        createSettingsTable(db)
        seedDefaultSettings(db)
        createNotificationsTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createCalendarTables(db)
            seedStarterCalendar(db)
        }
        if (oldVersion < 3) {
            createSettingsTable(db)
            seedDefaultSettings(db)
        }
        if (oldVersion < 4) {
            createNotificationsTable(db)
        }
        if (oldVersion < 5) {
            // Add is_read column to TABLE_NOTIFICATIONS safely without dropping any tables or data
            try {
                db.execSQL("ALTER TABLE $TABLE_NOTIFICATIONS ADD COLUMN $COLUMN_NOTIF_IS_READ INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) {
                // Handle safe idempotence
            }
        }
    }

    private fun createTasksTable(db: SQLiteDatabase) {
        val createSql = """
            CREATE TABLE IF NOT EXISTS $TABLE_TASKS (
                $COLUMN_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TASK_TITLE TEXT NOT NULL,
                $COLUMN_TASK_COMPLETED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_TASK_CATEGORY TEXT NOT NULL DEFAULT 'General',
                $COLUMN_TASK_DUE_TIME TEXT,
                $COLUMN_TASK_CREATED_AT INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createSql)
    }

    private fun seedStarterTasks(db: SQLiteDatabase) {
        val starterTasks = listOf(
            Triple("Review Q4 product roadmap & goals", "Work", false),
            Triple("Finalize Inbo Android UI design tokens", "Design", false),
            Triple("Clean up project structure and remove noise", "Work", true)
        )
        val now = System.currentTimeMillis()
        starterTasks.forEachIndexed { index, (title, category, completed) ->
            val values = ContentValues().apply {
                put(COLUMN_TASK_TITLE, title)
                put(COLUMN_TASK_COMPLETED, if (completed) 1 else 0)
                put(COLUMN_TASK_CATEGORY, category)
                put(COLUMN_TASK_CREATED_AT, now + index)
            }
            db.insert(TABLE_TASKS, null, values)
        }
    }

    private fun createCalendarTables(db: SQLiteDatabase) {
        val createEventsSql = """
            CREATE TABLE IF NOT EXISTS $TABLE_EVENTS (
                $COLUMN_EVENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EVENT_TITLE TEXT NOT NULL,
                $COLUMN_EVENT_TIME_RANGE TEXT NOT NULL,
                $COLUMN_EVENT_TYPE TEXT NOT NULL,
                $COLUMN_EVENT_LOCATION TEXT,
                $COLUMN_EVENT_DATE TEXT NOT NULL DEFAULT '16',
                $COLUMN_EVENT_CREATED_AT INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createEventsSql)

        val createDeadlinesSql = """
            CREATE TABLE IF NOT EXISTS $TABLE_DEADLINES (
                $COLUMN_DEADLINE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DEADLINE_TITLE TEXT NOT NULL,
                $COLUMN_DEADLINE_DUE_DATE TEXT NOT NULL,
                $COLUMN_DEADLINE_TAG TEXT NOT NULL,
                $COLUMN_DEADLINE_COMPLETED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_DEADLINE_DATE TEXT,
                $COLUMN_DEADLINE_CREATED_AT INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createDeadlinesSql)
    }

    private fun seedStarterCalendar(db: SQLiteDatabase) {
        val starterEvents = listOf(
            CalendarEvent(
                id = 1,
                title = "Design System & Compose Sync",
                timeRange = "09:30 AM - 10:15 AM",
                type = "Meeting",
                location = "Design Room / Virtual",
                date = "16"
            ),
            CalendarEvent(
                id = 2,
                title = "Deep Work: Inbo Core Polish",
                timeRange = "11:00 AM - 12:30 PM",
                type = "Focus Time",
                location = "Do Not Disturb",
                date = "16"
            ),
            CalendarEvent(
                id = 3,
                title = "Product Architecture Review",
                timeRange = "03:00 PM - 03:45 PM",
                type = "Review",
                location = "Conference Room B",
                date = "16"
            )
        )
        val now = System.currentTimeMillis()
        starterEvents.forEachIndexed { index, event ->
            val values = ContentValues().apply {
                put(COLUMN_EVENT_TITLE, event.title)
                put(COLUMN_EVENT_TIME_RANGE, event.timeRange)
                put(COLUMN_EVENT_TYPE, event.type)
                put(COLUMN_EVENT_LOCATION, event.location)
                put(COLUMN_EVENT_DATE, event.date)
                put(COLUMN_EVENT_CREATED_AT, now + index)
            }
            db.insert(TABLE_EVENTS, null, values)
        }

        val starterDeadlines = listOf(
            CalendarDeadline(
                id = 1,
                title = "Inbo Android UI Freeze",
                dueDate = "Tomorrow, 5:00 PM",
                tag = "High Priority",
                completed = false,
                date = "17"
            ),
            CalendarDeadline(
                id = 2,
                title = "Sprint Milestone Documentation",
                dueDate = "Thu, 12:00 PM",
                tag = "Milestone",
                completed = false,
                date = "19"
            ),
            CalendarDeadline(
                id = 3,
                title = "Publish Release Candidate",
                dueDate = "Friday, 6:00 PM",
                tag = "Release",
                completed = true,
                date = "20"
            )
        )
        starterDeadlines.forEachIndexed { index, deadline ->
            val values = ContentValues().apply {
                put(COLUMN_DEADLINE_TITLE, deadline.title)
                put(COLUMN_DEADLINE_DUE_DATE, deadline.dueDate)
                put(COLUMN_DEADLINE_TAG, deadline.tag)
                put(COLUMN_DEADLINE_COMPLETED, if (deadline.completed) 1 else 0)
                put(COLUMN_DEADLINE_DATE, deadline.date)
                put(COLUMN_DEADLINE_CREATED_AT, now + index)
            }
            db.insert(TABLE_DEADLINES, null, values)
        }
    }

    private fun createSettingsTable(db: SQLiteDatabase) {
        val createSettingsSql = """
            CREATE TABLE IF NOT EXISTS $TABLE_SETTINGS (
                $COLUMN_SETTING_KEY TEXT PRIMARY KEY,
                $COLUMN_SETTING_VALUE TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createSettingsSql)
    }

    private fun seedDefaultSettings(db: SQLiteDatabase) {
        val defaults = listOf(
            KEY_DARK_MODE to "false",
            KEY_QUIET_HOURS to "true",
            KEY_MORNING_DIGEST to "true",
            KEY_TASK_REMINDERS to "true",
            KEY_HAPTIC_FEEDBACK to "true",
            KEY_AUTO_ARCHIVE to "false",
            KEY_DEFAULT_LAUNCH_SCREEN to "Inbox"
        )
        defaults.forEach { (key, value) ->
            val values = ContentValues().apply {
                put(COLUMN_SETTING_KEY, key)
                put(COLUMN_SETTING_VALUE, value)
            }
            db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun createNotificationsTable(db: SQLiteDatabase) {
        val createNotificationsSql = """
            CREATE TABLE IF NOT EXISTS $TABLE_NOTIFICATIONS (
                $COLUMN_NOTIF_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOTIF_KEY TEXT NOT NULL UNIQUE,
                $COLUMN_NOTIF_PACKAGE TEXT NOT NULL,
                $COLUMN_NOTIF_SENDER TEXT NOT NULL,
                $COLUMN_NOTIF_SNIPPET TEXT NOT NULL,
                $COLUMN_NOTIF_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_NOTIF_IS_GROUP INTEGER NOT NULL DEFAULT 0,
                $COLUMN_NOTIF_IS_SUMMARY INTEGER NOT NULL DEFAULT 0,
                $COLUMN_NOTIF_IS_DISMISSED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_NOTIF_IS_READ INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createNotificationsSql)
    }
}

/**
 * Data Access Object performing CRUD operations on the SQLite database asynchronously for Tasks.
 */
class TaskDao(
    private val dbHelper: InboDatabaseHelper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getAllTasks(): List<InboTask> = withContext(ioDispatcher) {
        val db = dbHelper.readableDatabase
        val tasks = mutableListOf<InboTask>()
        val cursor: Cursor = db.query(
            InboDatabaseHelper.TABLE_TASKS,
            null,
            null,
            null,
            null,
            null,
            "${InboDatabaseHelper.COLUMN_TASK_ID} ASC"
        )
        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_TASK_ID)
            val titleIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_TASK_TITLE)
            val completedIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_TASK_COMPLETED)
            val categoryIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_TASK_CATEGORY)
            val dueTimeIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_TASK_DUE_TIME)

            while (it.moveToNext()) {
                tasks.add(
                    InboTask(
                        id = it.getInt(idIndex),
                        title = it.getString(titleIndex),
                        completed = it.getInt(completedIndex) == 1,
                        category = it.getString(categoryIndex),
                        dueTime = if (it.isNull(dueTimeIndex)) null else it.getString(dueTimeIndex)
                    )
                )
            }
        }
        tasks
    }

    suspend fun insertTask(title: String, category: String, dueTime: String? = null): InboTask = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InboDatabaseHelper.COLUMN_TASK_TITLE, title)
            put(InboDatabaseHelper.COLUMN_TASK_COMPLETED, 0)
            put(InboDatabaseHelper.COLUMN_TASK_CATEGORY, category)
            put(InboDatabaseHelper.COLUMN_TASK_DUE_TIME, dueTime)
            put(InboDatabaseHelper.COLUMN_TASK_CREATED_AT, System.currentTimeMillis())
        }
        val generatedId = db.insertOrThrow(InboDatabaseHelper.TABLE_TASKS, null, values)
        InboTask(
            id = generatedId.toInt(),
            title = title,
            completed = false,
            category = category,
            dueTime = dueTime
        )
    }

    suspend fun updateTaskCompletion(id: Int, completed: Boolean) = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InboDatabaseHelper.COLUMN_TASK_COMPLETED, if (completed) 1 else 0)
        }
        db.update(
            InboDatabaseHelper.TABLE_TASKS,
            values,
            "${InboDatabaseHelper.COLUMN_TASK_ID} = ?",
            arrayOf(id.toString())
        )
    }

    suspend fun deleteTask(id: Int) = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        db.delete(
            InboDatabaseHelper.TABLE_TASKS,
            "${InboDatabaseHelper.COLUMN_TASK_ID} = ?",
            arrayOf(id.toString())
        )
    }

    suspend fun clearCompletedTasks() = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        db.delete(
            InboDatabaseHelper.TABLE_TASKS,
            "${InboDatabaseHelper.COLUMN_TASK_COMPLETED} = 1",
            null
        )
    }
}

/**
 * Data Access Object performing CRUD operations on the SQLite database asynchronously for Calendar.
 */
class CalendarDao(
    private val dbHelper: InboDatabaseHelper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getAllEvents(): List<CalendarEvent> = withContext(ioDispatcher) {
        val db = dbHelper.readableDatabase
        val events = mutableListOf<CalendarEvent>()
        val cursor: Cursor = db.query(
            InboDatabaseHelper.TABLE_EVENTS,
            null,
            null,
            null,
            null,
            null,
            "${InboDatabaseHelper.COLUMN_EVENT_ID} ASC"
        )
        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_EVENT_ID)
            val titleIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_EVENT_TITLE)
            val timeRangeIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_EVENT_TIME_RANGE)
            val typeIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_EVENT_TYPE)
            val locationIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_EVENT_LOCATION)
            val dateIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_EVENT_DATE)

            while (it.moveToNext()) {
                val date = it.getString(dateIndex)
                events.add(
                    CalendarEvent(
                        id = it.getInt(idIndex),
                        title = it.getString(titleIndex),
                        timeRange = it.getString(timeRangeIndex),
                        type = it.getString(typeIndex),
                        location = if (it.isNull(locationIndex)) null else it.getString(locationIndex),
                        date = date,
                        isToday = date == "16"
                    )
                )
            }
        }
        events
    }

    suspend fun insertEvent(
        title: String,
        timeRange: String,
        type: String,
        location: String? = null,
        date: String = "16"
    ): CalendarEvent = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InboDatabaseHelper.COLUMN_EVENT_TITLE, title)
            put(InboDatabaseHelper.COLUMN_EVENT_TIME_RANGE, timeRange)
            put(InboDatabaseHelper.COLUMN_EVENT_TYPE, type)
            put(InboDatabaseHelper.COLUMN_EVENT_LOCATION, location)
            put(InboDatabaseHelper.COLUMN_EVENT_DATE, date)
            put(InboDatabaseHelper.COLUMN_EVENT_CREATED_AT, System.currentTimeMillis())
        }
        val generatedId = db.insertOrThrow(InboDatabaseHelper.TABLE_EVENTS, null, values)
        CalendarEvent(
            id = generatedId.toInt(),
            title = title,
            timeRange = timeRange,
            type = type,
            location = location,
            date = date,
            isToday = date == "16"
        )
    }

    suspend fun deleteEvent(id: Int) = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        db.delete(
            InboDatabaseHelper.TABLE_EVENTS,
            "${InboDatabaseHelper.COLUMN_EVENT_ID} = ?",
            arrayOf(id.toString())
        )
    }

    suspend fun getAllDeadlines(): List<CalendarDeadline> = withContext(ioDispatcher) {
        val db = dbHelper.readableDatabase
        val deadlines = mutableListOf<CalendarDeadline>()
        val cursor: Cursor = db.query(
            InboDatabaseHelper.TABLE_DEADLINES,
            null,
            null,
            null,
            null,
            null,
            "${InboDatabaseHelper.COLUMN_DEADLINE_ID} ASC"
        )
        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_DEADLINE_ID)
            val titleIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_DEADLINE_TITLE)
            val dueDateIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_DEADLINE_DUE_DATE)
            val tagIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_DEADLINE_TAG)
            val completedIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_DEADLINE_COMPLETED)
            val dateIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_DEADLINE_DATE)

            while (it.moveToNext()) {
                deadlines.add(
                    CalendarDeadline(
                        id = it.getInt(idIndex),
                        title = it.getString(titleIndex),
                        dueDate = it.getString(dueDateIndex),
                        tag = it.getString(tagIndex),
                        completed = it.getInt(completedIndex) == 1,
                        date = if (it.isNull(dateIndex)) null else it.getString(dateIndex)
                    )
                )
            }
        }
        deadlines
    }

    suspend fun insertDeadline(
        title: String,
        dueDate: String,
        tag: String,
        date: String? = null
    ): CalendarDeadline = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InboDatabaseHelper.COLUMN_DEADLINE_TITLE, title)
            put(InboDatabaseHelper.COLUMN_DEADLINE_DUE_DATE, dueDate)
            put(InboDatabaseHelper.COLUMN_DEADLINE_TAG, tag)
            put(InboDatabaseHelper.COLUMN_DEADLINE_COMPLETED, 0)
            put(InboDatabaseHelper.COLUMN_DEADLINE_DATE, date)
            put(InboDatabaseHelper.COLUMN_DEADLINE_CREATED_AT, System.currentTimeMillis())
        }
        val generatedId = db.insertOrThrow(InboDatabaseHelper.TABLE_DEADLINES, null, values)
        CalendarDeadline(
            id = generatedId.toInt(),
            title = title,
            dueDate = dueDate,
            tag = tag,
            completed = false,
            date = date
        )
    }

    suspend fun updateDeadlineCompletion(id: Int, completed: Boolean) = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InboDatabaseHelper.COLUMN_DEADLINE_COMPLETED, if (completed) 1 else 0)
        }
        db.update(
            InboDatabaseHelper.TABLE_DEADLINES,
            values,
            "${InboDatabaseHelper.COLUMN_DEADLINE_ID} = ?",
            arrayOf(id.toString())
        )
    }

    suspend fun deleteDeadline(id: Int) = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        db.delete(
            InboDatabaseHelper.TABLE_DEADLINES,
            "${InboDatabaseHelper.COLUMN_DEADLINE_ID} = ?",
            arrayOf(id.toString())
        )
    }
}

/**
 * Data Access Object performing CRUD operations on the SQLite database asynchronously for Settings.
 */
class SettingsDao(
    private val dbHelper: InboDatabaseHelper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getSettings(): InboSettings = withContext(ioDispatcher) {
        val db = dbHelper.readableDatabase
        var darkMode = false
        var quietHours = true
        var morningDigest = true
        var taskReminders = true
        var hapticFeedback = true
        var autoArchive = false
        var defaultLaunch = "Inbox"

        val cursor = db.query(
            InboDatabaseHelper.TABLE_SETTINGS,
            null,
            null,
            null,
            null,
            null,
            null
        )
        cursor.use {
            val keyIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_SETTING_KEY)
            val valIndex = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_SETTING_VALUE)
            while (it.moveToNext()) {
                val key = it.getString(keyIndex)
                val value = it.getString(valIndex)
                when (key) {
                    InboDatabaseHelper.KEY_DARK_MODE -> darkMode = value.toBoolean()
                    InboDatabaseHelper.KEY_QUIET_HOURS -> quietHours = value.toBoolean()
                    InboDatabaseHelper.KEY_MORNING_DIGEST -> morningDigest = value.toBoolean()
                    InboDatabaseHelper.KEY_TASK_REMINDERS -> taskReminders = value.toBoolean()
                    InboDatabaseHelper.KEY_HAPTIC_FEEDBACK -> hapticFeedback = value.toBoolean()
                    InboDatabaseHelper.KEY_AUTO_ARCHIVE -> autoArchive = value.toBoolean()
                    InboDatabaseHelper.KEY_DEFAULT_LAUNCH_SCREEN -> defaultLaunch = value
                }
            }
        }
        InboSettings(
            darkModeEnabled = darkMode,
            quietHoursEnabled = quietHours,
            morningDigestEnabled = morningDigest,
            taskRemindersEnabled = taskReminders,
            hapticFeedbackEnabled = hapticFeedback,
            autoArchiveEnabled = autoArchive,
            defaultLaunchScreen = defaultLaunch
        )
    }

    suspend fun setSetting(key: String, value: String) = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InboDatabaseHelper.COLUMN_SETTING_KEY, key)
            put(InboDatabaseHelper.COLUMN_SETTING_VALUE, value)
        }
        db.insertWithOnConflict(
            InboDatabaseHelper.TABLE_SETTINGS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }
}

/**
 * Data Access Object for persisting, pruning, and querying captured notifications.
 *
 * Privacy Guarantees:
 * 1. Hard deletion: When dismissed, records are permanently erased using DELETE.
 * 2. 72-hour retention: Older notifications are automatically pruned via pruneExpiredNotifications.
 * 3. Never touches Tasks, Calendar, or Settings tables.
 */
class NotificationDao(
    private val dbHelper: InboDatabaseHelper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Retrieves active, non-expired notifications ordered newest first.
     * Automatically filters out any notification older than 72 hours.
     */
    suspend fun getAllNotifications(nowMs: Long = System.currentTimeMillis()): List<CapturedNotification> = withContext(ioDispatcher) {
        val cutoffTimestamp = nowMs - InboDatabaseHelper.RETENTION_PERIOD_MS
        val db = dbHelper.readableDatabase
        val list = mutableListOf<CapturedNotification>()
        val cursor = db.query(
            InboDatabaseHelper.TABLE_NOTIFICATIONS,
            null,
            "${InboDatabaseHelper.COLUMN_NOTIF_TIMESTAMP} >= ?",
            arrayOf(cutoffTimestamp.toString()),
            null,
            null,
            "${InboDatabaseHelper.COLUMN_NOTIF_TIMESTAMP} DESC"
        )
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_NOTIF_ID)
            val keyIdx = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_NOTIF_KEY)
            val pkgIdx = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_NOTIF_PACKAGE)
            val senderIdx = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_NOTIF_SENDER)
            val snippetIdx = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_NOTIF_SNIPPET)
            val timeIdx = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_NOTIF_TIMESTAMP)
            val groupIdx = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_NOTIF_IS_GROUP)
            val summaryIdx = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_NOTIF_IS_SUMMARY)
            val dismissedIdx = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_NOTIF_IS_DISMISSED)
            val readIdx = it.getColumnIndex(InboDatabaseHelper.COLUMN_NOTIF_IS_READ)

            while (it.moveToNext()) {
                val isRead = if (readIdx != -1) it.getInt(readIdx) == 1 else false
                list.add(
                    CapturedNotification(
                        id = it.getLong(idIdx),
                        notificationKey = it.getString(keyIdx),
                        packageName = it.getString(pkgIdx),
                        senderOrTitle = it.getString(senderIdx),
                        messageSnippet = it.getString(snippetIdx),
                        timestamp = it.getLong(timeIdx),
                        isGroup = it.getInt(groupIdx) == 1,
                        isSummary = it.getInt(summaryIdx) == 1,
                        isDismissed = it.getInt(dismissedIdx) == 1,
                        isRead = isRead
                    )
                )
            }
        }
        list
    }

    suspend fun insertOrUpdateNotification(notification: CapturedNotification): Long = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase

        // Check if a record with this unique notification_key already exists
        var existingIsRead: Int? = null
        var existingRowId: Long? = null

        val cursor = db.query(
            InboDatabaseHelper.TABLE_NOTIFICATIONS,
            arrayOf(InboDatabaseHelper.COLUMN_NOTIF_ID, InboDatabaseHelper.COLUMN_NOTIF_IS_READ),
            "${InboDatabaseHelper.COLUMN_NOTIF_KEY} = ?",
            arrayOf(notification.notificationKey),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val rowIdIdx = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_NOTIF_ID)
                val isReadIdx = it.getColumnIndexOrThrow(InboDatabaseHelper.COLUMN_NOTIF_IS_READ)
                existingRowId = it.getLong(rowIdIdx)
                existingIsRead = it.getInt(isReadIdx)
            }
        }

        // Preserve existing is_read status if already stored; otherwise default to new notification's isRead (defaults to false / unread)
        val finalIsRead = existingIsRead ?: if (notification.isRead) 1 else 0

        val values = ContentValues().apply {
            put(InboDatabaseHelper.COLUMN_NOTIF_KEY, notification.notificationKey)
            put(InboDatabaseHelper.COLUMN_NOTIF_PACKAGE, notification.packageName)
            put(InboDatabaseHelper.COLUMN_NOTIF_SENDER, notification.senderOrTitle)
            put(InboDatabaseHelper.COLUMN_NOTIF_SNIPPET, notification.messageSnippet)
            put(InboDatabaseHelper.COLUMN_NOTIF_TIMESTAMP, notification.timestamp)
            put(InboDatabaseHelper.COLUMN_NOTIF_IS_GROUP, if (notification.isGroup) 1 else 0)
            put(InboDatabaseHelper.COLUMN_NOTIF_IS_SUMMARY, if (notification.isSummary) 1 else 0)
            put(InboDatabaseHelper.COLUMN_NOTIF_IS_DISMISSED, if (notification.isDismissed) 1 else 0)
            put(InboDatabaseHelper.COLUMN_NOTIF_IS_READ, finalIsRead)
        }

        if (existingRowId != null) {
            // Update existing row directly preserving the stable primary key id and read state
            db.update(
                InboDatabaseHelper.TABLE_NOTIFICATIONS,
                values,
                "${InboDatabaseHelper.COLUMN_NOTIF_ID} = ?",
                arrayOf(existingRowId.toString())
            )
            existingRowId!!
        } else {
            // Insert new notification record
            db.insert(
                InboDatabaseHelper.TABLE_NOTIFICATIONS,
                null,
                values
            )
        }
    }

    /**
     * Marks an individual captured notification as read/unread by its database row ID or notification key.
     */
    suspend fun markNotificationAsRead(id: Long, isRead: Boolean = true): Int = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InboDatabaseHelper.COLUMN_NOTIF_IS_READ, if (isRead) 1 else 0)
        }
        db.update(
            InboDatabaseHelper.TABLE_NOTIFICATIONS,
            values,
            "${InboDatabaseHelper.COLUMN_NOTIF_ID} = ?",
            arrayOf(id.toString())
        )
    }

    suspend fun markNotificationAsReadByKey(notificationKey: String, isRead: Boolean = true): Int = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InboDatabaseHelper.COLUMN_NOTIF_IS_READ, if (isRead) 1 else 0)
        }
        db.update(
            InboDatabaseHelper.TABLE_NOTIFICATIONS,
            values,
            "${InboDatabaseHelper.COLUMN_NOTIF_KEY} = ?",
            arrayOf(notificationKey)
        )
    }

    /**
     * Marks all active captured notifications as read.
     */
    suspend fun markAllNotificationsAsRead(): Int = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InboDatabaseHelper.COLUMN_NOTIF_IS_READ, 1)
        }
        db.update(
            InboDatabaseHelper.TABLE_NOTIFICATIONS,
            values,
            null,
            null
        )
    }

    /**
     * Hard-deletes a notification permanently when dismissed in the Android notification tray.
     * Privacy requirement: Removes record entirely from database disk storage.
     */
    suspend fun hardDeleteNotification(notificationKey: String): Int = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        db.delete(
            InboDatabaseHelper.TABLE_NOTIFICATIONS,
            "${InboDatabaseHelper.COLUMN_NOTIF_KEY} = ?",
            arrayOf(notificationKey)
        )
    }

    /**
     * Automatically prunes captured notifications older than 72 hours.
     * Strictly targets TABLE_NOTIFICATIONS only. Does not touch tasks, calendar, or settings.
     * Returns the count of deleted expired records.
     */
    suspend fun pruneExpiredNotifications(nowMs: Long = System.currentTimeMillis()): Int = withContext(ioDispatcher) {
        val cutoffTimestamp = nowMs - InboDatabaseHelper.RETENTION_PERIOD_MS
        val db = dbHelper.writableDatabase
        db.delete(
            InboDatabaseHelper.TABLE_NOTIFICATIONS,
            "${InboDatabaseHelper.COLUMN_NOTIF_TIMESTAMP} < ?",
            arrayOf(cutoffTimestamp.toString())
        )
    }

    suspend fun clearAllNotifications(): Int = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase
        db.delete(InboDatabaseHelper.TABLE_NOTIFICATIONS, null, null)
    }
}
