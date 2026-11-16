package com.inbo.app.intelligence

/**
 * 8 predefined intelligence categories plus UNKNOWN.
 */
enum class NotificationCategory(val displayName: String, val badgeLabel: String) {
    ANNOUNCEMENT("Announcement", "Announcement"),
    HOMEWORK("Homework & Assignments", "Homework"),
    TEST_EXAM("Tests & Exams", "Exam / Test"),
    DEADLINE("Deadlines & Submissions", "Deadline"),
    SCHEDULE_CHANGE("Schedule Change", "Rescheduled"),
    EVENT("Events & Seminars", "Event"),
    CANCELLATION("Cancellation", "Cancelled"),
    STUDY_MATERIAL("Study Material & Notes", "Material"),
    UNKNOWN("General Message", "General")
}

/**
 * Threshold configuration for classification confidence.
 * Easily tunable without code restructuring.
 */
object ConfidenceThresholds {
    const val RECOGNIZED = 85
    const val SUGGESTED = 60
}

/**
 * Structured information extracted from a captured notification.
 * Retains null for missing fields to avoid hallucinating dates/times/venues.
 */
data class ExtractedInformation(
    val category: NotificationCategory,
    val confidence: Int, // 0 to 100
    val subjectOrTopic: String? = null,
    val actionItem: String? = null,
    val dateText: String? = null,
    val timeText: String? = null,
    val venue: String? = null,
    val details: String? = null,
    val matchedSignals: List<String> = emptyList()
) {
    val isRecognized: Boolean
        get() = confidence >= ConfidenceThresholds.RECOGNIZED

    val isSuggested: Boolean
        get() = confidence in ConfidenceThresholds.SUGGESTED until ConfidenceThresholds.RECOGNIZED

    val isClassified: Boolean
        get() = confidence >= ConfidenceThresholds.SUGGESTED && category != NotificationCategory.UNKNOWN
}

/**
 * Pluggable intelligence engine interface.
 * Allows rule-based engines today and on-device ML / TFLite models in the future
 * without changing consumers or UI.
 */
interface LocalIntelligenceEngine {
    fun analyze(senderOrTitle: String, messageText: String): ExtractedInformation
}
