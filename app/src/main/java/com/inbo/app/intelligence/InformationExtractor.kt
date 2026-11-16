package com.inbo.app.intelligence

import java.util.regex.Pattern

/**
 * Robust regex-based extraction of temporal data, venues, subjects, and action items.
 * Strictly avoids hallucinating any values that are not in the message.
 */
object InformationExtractor {

    // Time pattern: matches "10:00 AM", "5:00 PM", "2:00pm", "11:59 PM"
    private val TIME_PATTERN = Pattern.compile(
        "\\b([0-1]?[0-9]|2[0-3])(?::([0-5][0-9]))?\\s*(am|pm|AM|PM)\\b(?:\\s*(?:-|to)\\s*([0-1]?[0-9]|2[0-3])(?::([0-5][0-9]))?\\s*(am|pm|AM|PM)\\b)?",
        Pattern.CASE_INSENSITIVE
    )

    // Standard date patterns:
    // e.g. "tomorrow", "today", "tonight", "yesterday", "next Monday", "this Friday"
    // e.g. "15th Nov", "Nov 15", "15 November 2026", "25/11/2026", "2026-11-25"
    private val RELATIVE_DATE_PATTERN = Pattern.compile(
        "\\b(?:by|on|before|due)?\\s*(today|tomorrow|tonight|next\\s+(?:monday|tuesday|wednesday|thursday|friday|saturday|sunday)|this\\s+(?:monday|tuesday|wednesday|thursday|friday|saturday|sunday)|monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val EXPLICIT_DATE_PATTERN = Pattern.compile(
        "\\b(\\d{1,2}(?:st|nd|rd|th)?\\s+(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|june?|july?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)(?:\\s+\\d{2,4})?|(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|june?|july?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)\\s+\\d{1,2}(?:st|nd|rd|th)?(?:,\\s*\\d{2,4})?|\\d{1,2}[/-]\\d{1,2}(?:[/-]\\d{2,4})?)\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Venue pattern: e.g. "in Room 302", "at Hall A", "in Audi 2", "Lab 4", "Library", "Zoom", "Google Meet"
    private val VENUE_PATTERN = Pattern.compile(
        "\\b(?:in|at|venue:?|location:?)\\s+([A-Z0-9][A-Za-z0-9\\s\\-]{2,25}(?:room|hall|audi|auditorium|lab|laboratory|library|seminar hall|center|centre|ground|zoom|meet|teams)?)\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Common academic & professional subject candidates
    private val COMMON_SUBJECTS = listOf(
        "Mathematics", "Maths", "Math", "Physics", "Chemistry", "Biology",
        "Computer Science", "CS", "Data Structures", "Algorithms", "Software Engineering",
        "English", "History", "Economics", "Accounts", "Commerce", "Business Studies",
        "Circuits", "Electronics", "Mechanics", "AI", "Machine Learning"
    )

    /**
     * Extracts date string if present; returns null if missing or ambiguous.
     */
    fun extractDate(text: String): String? {
        val relMatcher = RELATIVE_DATE_PATTERN.matcher(text)
        if (relMatcher.find()) {
            val raw = relMatcher.group(1)?.trim() ?: return null
            // Strip leading "this " or "next " for clean display (e.g. "this Friday" -> "Friday")
            val clean = raw.replace(Regex("^(?:this|next)\\s+", RegexOption.IGNORE_CASE), "")
            return clean.replaceFirstChar { it.uppercase() }
        }
        val expMatcher = EXPLICIT_DATE_PATTERN.matcher(text)
        if (expMatcher.find()) {
            return expMatcher.group(1)
        }
        return null
    }

    /**
     * Extracts time string if present; returns null if missing.
     */
    fun extractTime(text: String): String? {
        val matcher = TIME_PATTERN.matcher(text)
        if (matcher.find()) {
            return matcher.group(0)?.trim()?.replaceFirstChar { it.uppercase() }
        }
        return null
    }

    /**
     * Extracts venue or online meeting link if present.
     */
    fun extractVenue(text: String): String? {
        val lower = text.lowercase()
        if (lower.contains("zoom")) return "Zoom"
        if (lower.contains("google meet") || lower.contains("gmeet")) return "Google Meet"
        if (lower.contains("ms teams") || lower.contains("microsoft teams")) return "Microsoft Teams"

        val matcher = VENUE_PATTERN.matcher(text)
        if (matcher.find()) {
            val candidate = matcher.group(1)?.trim()
            if (candidate != null && candidate.length > 2 && !candidate.equals("the", ignoreCase = true)) {
                return candidate
            }
        }
        return null
    }

    /**
     * Extracts subject or topic from sender or message content.
     */
    fun extractSubject(sender: String, message: String): String? {
        val combined = "$sender $message"
        for (subj in COMMON_SUBJECTS) {
            val pattern = Pattern.compile("\\b${Pattern.quote(subj)}\\b", Pattern.CASE_INSENSITIVE)
            if (pattern.matcher(combined).find()) {
                return subj
            }
        }
        return null
    }

    /**
     * Extracts actionable items (e.g. "Submit by 5pm", "Complete Chapter 4", "Prepare for quiz").
     */
    fun extractActionItem(category: NotificationCategory, message: String): String? {
        val lower = message.lowercase()
        return when (category) {
            NotificationCategory.HOMEWORK -> {
                when {
                    lower.contains("submit") -> "Complete & submit assignment"
                    lower.contains("solve") -> "Solve exercises"
                    lower.contains("read") -> "Read assigned material"
                    else -> "Complete assigned homework"
                }
            }
            NotificationCategory.DEADLINE -> "Submit before deadline"
            NotificationCategory.TEST_EXAM -> "Prepare for examination / test"
            NotificationCategory.SCHEDULE_CHANGE -> "Note rescheduled timing"
            NotificationCategory.CANCELLATION -> "No action required (Session cancelled)"
            NotificationCategory.EVENT -> "Attend scheduled event / session"
            NotificationCategory.STUDY_MATERIAL -> "Review provided study materials"
            NotificationCategory.ANNOUNCEMENT -> "Review announcement details"
            NotificationCategory.UNKNOWN -> null
        }
    }
}
