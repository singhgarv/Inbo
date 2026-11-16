package com.inbo.app.intelligence

import java.util.regex.Pattern

/**
 * Completely offline, rule-based Local Intelligence Engine for Inbo.
 *
 * Design & Architecture:
 * 1. Zero Cloud / Zero API: Runs 100% locally on Android device without internet access or API keys.
 * 2. Multi-Signal Scoring: Uses keyword dictionaries, phrase matching, regex, and context modifiers.
 * 3. Negation & Cancellation Resolution: Prevents "cancelled test" from triggering TEST_EXAM.
 * 4. Extensible: Implements LocalIntelligenceEngine so future on-device ML/TFLite models can plug in.
 */
class RuleBasedIntelligenceEngine : LocalIntelligenceEngine {

    companion object {
        // Negation & cancellation tokens (High priority override)
        private val CANCELLATION_SIGNALS = listOf(
            "cancelled", "canceled", "called off", "call off", "postponed indefinitely",
            "will not be held", "won't be held", "no class", "no lecture", "no meeting",
            "class suspended", "suspended", "holiday declared", "is cancelled", "has been cancelled"
        )

        // Reschedule signals
        private val SCHEDULE_CHANGE_SIGNALS = listOf(
            "rescheduled", "re-scheduled", "shifted to", "moved to", "new timing",
            "time changed", "timing changed", "delay", "delayed by", "preponed",
            "will now be held at", "new schedule", "updated timetable", "revised schedule"
        )

        // Test & Exam signals
        private val TEST_EXAM_SIGNALS = listOf(
            "exam", "examination", "test", "quiz", "midterm", "mid-term", "final exam",
            "viva", "assessment", "practical exam", "unit test", "mock test", "grading"
        )

        // Homework & Assignment signals
        private val HOMEWORK_SIGNALS = listOf(
            "homework", "assignment", "problem set", "worksheet", "exercise",
            "solve questions", "lab report", "write-up", "task for tomorrow", "read chapter"
        )

        // Deadline & Submission signals
        private val DEADLINE_SIGNALS = listOf(
            "deadline", "due date", "due by", "last date", "submit before",
            "submission closes", "cutoff", "cut-off", "strictly by", "hard deadline"
        )

        // Study Material signals
        private val STUDY_MATERIAL_SIGNALS = listOf(
            "notes", "slides", "pdf", "lecture notes", "study material", "attached document",
            "ppt", "presentation", "formula sheet", "reference book", "drive link", "recording"
        )

        // Event & Seminar signals
        private val EVENT_SIGNALS = listOf(
            "webinar", "seminar", "workshop", "guest lecture", "hackathon", "conference",
            "meetup", "annual fest", "orientation", "symposium", "ceremony", "celebration"
        )

        // Announcement signals
        private val ANNOUNCEMENT_SIGNALS = listOf(
            "announcement", "notice", "attention all", "important update", "circular",
            "please note", "fyi", "kindly note", "heads up", "to all students", "dear all"
        )
    }

    override fun analyze(senderOrTitle: String, messageText: String): ExtractedInformation {
        val trimmedMsg = messageText.trim()
        val combinedText = "$senderOrTitle $trimmedMsg".trim()
        val lowerText = combinedText.lowercase()

        // Empty or meaningless check
        if (trimmedMsg.length < 5) {
            return ExtractedInformation(
                category = NotificationCategory.UNKNOWN,
                confidence = 20,
                details = trimmedMsg
            )
        }

        // Extracted entities
        val date = InformationExtractor.extractDate(trimmedMsg)
        val time = InformationExtractor.extractTime(trimmedMsg)
        val venue = InformationExtractor.extractVenue(trimmedMsg)
        val subject = InformationExtractor.extractSubject(senderOrTitle, trimmedMsg)

        // Check for Cancellation / Negation first (handles e.g. "Physics test cancelled")
        val isCancellation = hasMatch(lowerText, CANCELLATION_SIGNALS)
        if (isCancellation) {
            val score = calculateScore(
                baseConfidence = 85,
                hasTemporal = date != null || time != null,
                hasSubject = subject != null
            )
            return ExtractedInformation(
                category = NotificationCategory.CANCELLATION,
                confidence = score,
                subjectOrTopic = subject,
                actionItem = "No action required (Session cancelled)",
                dateText = date,
                timeText = time,
                venue = venue,
                details = trimmedMsg,
                matchedSignals = listOf("cancellation")
            )
        }

        // Check for Schedule Change / Rescheduling
        val isScheduleChange = hasMatch(lowerText, SCHEDULE_CHANGE_SIGNALS)
        if (isScheduleChange && (time != null || date != null)) {
            val score = calculateScore(
                baseConfidence = 88,
                hasTemporal = true,
                hasSubject = subject != null
            )
            return ExtractedInformation(
                category = NotificationCategory.SCHEDULE_CHANGE,
                confidence = score,
                subjectOrTopic = subject,
                actionItem = InformationExtractor.extractActionItem(NotificationCategory.SCHEDULE_CHANGE, trimmedMsg),
                dateText = date,
                timeText = time,
                venue = venue,
                details = trimmedMsg,
                matchedSignals = listOf("schedule_change")
            )
        }

        // Multi-category match evaluations with scoring weights
        val categoryScores = mutableMapOf<NotificationCategory, Int>()
        val matchedSignals = mutableMapOf<NotificationCategory, MutableList<String>>()

        fun evaluateCategory(
            category: NotificationCategory,
            signals: List<String>,
            baseScore: Int
        ) {
            var matchCount = 0
            val foundSignals = mutableListOf<String>()
            for (signal in signals) {
                if (containsWordOrPhrase(lowerText, signal)) {
                    matchCount++
                    foundSignals.add(signal)
                }
            }
            if (matchCount > 0) {
                var score = baseScore + (matchCount - 1) * 6
                if (date != null) score += 6
                if (time != null) score += 4
                if (subject != null) score += 5
                categoryScores[category] = score.coerceIn(0, 100)
                matchedSignals[category] = foundSignals
            }
        }

        evaluateCategory(NotificationCategory.TEST_EXAM, TEST_EXAM_SIGNALS, 80)
        evaluateCategory(NotificationCategory.DEADLINE, DEADLINE_SIGNALS, 80)
        evaluateCategory(NotificationCategory.HOMEWORK, HOMEWORK_SIGNALS, 78)
        evaluateCategory(NotificationCategory.STUDY_MATERIAL, STUDY_MATERIAL_SIGNALS, 78)
        evaluateCategory(NotificationCategory.EVENT, EVENT_SIGNALS, 76)
        evaluateCategory(NotificationCategory.ANNOUNCEMENT, ANNOUNCEMENT_SIGNALS, 72)

        // Disambiguation between DEADLINE and HOMEWORK:
        // "Submit homework by 5pm" -> DEADLINE takes priority if explicit deadline keywords appear
        if (categoryScores.containsKey(NotificationCategory.DEADLINE) && categoryScores.containsKey(NotificationCategory.HOMEWORK)) {
            if (lowerText.contains("deadline") || lowerText.contains("due date") || lowerText.contains("due by")) {
                categoryScores[NotificationCategory.DEADLINE] = (categoryScores[NotificationCategory.DEADLINE] ?: 80) + 10
            }
        }

        // Pick highest scoring category
        val bestEntry = categoryScores.maxByOrNull { it.value }

        if (bestEntry != null && bestEntry.value >= ConfidenceThresholds.SUGGESTED) {
            val cat = bestEntry.key
            val finalScore = bestEntry.value
            return ExtractedInformation(
                category = cat,
                confidence = finalScore,
                subjectOrTopic = subject,
                actionItem = InformationExtractor.extractActionItem(cat, trimmedMsg),
                dateText = date,
                timeText = time,
                venue = venue,
                details = trimmedMsg,
                matchedSignals = matchedSignals[cat] ?: emptyList()
            )
        }

        // Fallback: If no category matches reliably, mark as UNKNOWN
        return ExtractedInformation(
            category = NotificationCategory.UNKNOWN,
            confidence = 35,
            subjectOrTopic = subject,
            dateText = date,
            timeText = time,
            venue = venue,
            details = trimmedMsg
        )
    }

    private fun hasMatch(text: String, signals: List<String>): Boolean {
        return signals.any { containsWordOrPhrase(text, it) }
    }

    private fun containsWordOrPhrase(text: String, phrase: String): Boolean {
        return if (phrase.contains(" ")) {
            text.contains(phrase)
        } else {
            val pattern = Pattern.compile("\\b${Pattern.quote(phrase)}\\b", Pattern.CASE_INSENSITIVE)
            pattern.matcher(text).find()
        }
    }

    private fun calculateScore(baseConfidence: Int, hasTemporal: Boolean, hasSubject: Boolean): Int {
        var score = baseConfidence
        if (hasTemporal) score += 8
        if (hasSubject) score += 5
        return score.coerceIn(0, 100)
    }
}
