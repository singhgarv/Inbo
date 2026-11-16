package com.inbo.app.intelligence

import org.junit.Assert.*
import org.junit.Test

class LocalIntelligenceEngineUnitTest {

    private val engine = RuleBasedIntelligenceEngine()

    // 1. All 8 Categories

    @Test
    fun testAnnouncementCategory() {
        val message = "Attention all students: Notice regarding winter break will be issued tomorrow."
        val result = engine.analyze("School Admin", message)

        assertEquals(NotificationCategory.ANNOUNCEMENT, result.category)
        assertTrue("Confidence should be >= SUGGESTED", result.confidence >= ConfidenceThresholds.SUGGESTED)
        assertEquals("Tomorrow", result.dateText)
        assertNotNull(result.actionItem)
    }

    @Test
    fun testHomeworkCategory() {
        val message = "Complete homework: Solve questions 1 to 10 from Mathematics textbook exercises."
        val result = engine.analyze("Math Teacher", message)

        assertEquals(NotificationCategory.HOMEWORK, result.category)
        assertTrue(result.confidence >= ConfidenceThresholds.SUGGESTED)
        assertEquals("Mathematics", result.subjectOrTopic)
        assertEquals("Solve exercises", result.actionItem)
    }

    @Test
    fun testExamCategory() {
        val message = "Physics midterm examination will take place on Monday in Hall A at 10:00 AM."
        val result = engine.analyze("Department Head", message)

        assertEquals(NotificationCategory.TEST_EXAM, result.category)
        assertTrue(result.confidence >= ConfidenceThresholds.RECOGNIZED)
        assertEquals("Physics", result.subjectOrTopic)
        assertEquals("Monday", result.dateText)
        assertEquals("10:00 AM", result.timeText)
        assertNotNull(result.venue)
    }

    @Test
    fun testDeadlineCategory() {
        val message = "Hard deadline: Project submission closes strictly by Friday at 5:00 PM."
        val result = engine.analyze("Professor", message)

        assertEquals(NotificationCategory.DEADLINE, result.category)
        assertTrue(result.confidence >= ConfidenceThresholds.RECOGNIZED)
        assertEquals("Friday", result.dateText)
        assertEquals("5:00 PM", result.timeText)
        assertEquals("Submit before deadline", result.actionItem)
    }

    @Test
    fun testScheduleChangeCategory() {
        val message = "Chemistry lecture timing changed: rescheduled to tomorrow at 2:00 PM in Room 204."
        val result = engine.analyze("Coordinator", message)

        assertEquals(NotificationCategory.SCHEDULE_CHANGE, result.category)
        assertTrue(result.confidence >= ConfidenceThresholds.RECOGNIZED)
        assertEquals("Chemistry", result.subjectOrTopic)
        assertEquals("Tomorrow", result.dateText)
        assertEquals("2:00 PM", result.timeText)
    }

    @Test
    fun testEventCategory() {
        val message = "Join us for an AI & Machine Learning workshop this Friday on Zoom at 4:00 PM."
        val result = engine.analyze("Tech Club", message)

        assertEquals(NotificationCategory.EVENT, result.category)
        assertTrue(result.confidence >= ConfidenceThresholds.SUGGESTED)
        assertEquals("Zoom", result.venue)
        assertEquals("Friday", result.dateText)
        assertEquals("4:00 PM", result.timeText)
    }

    @Test
    fun testCancellationCategory() {
        val message = "Friday's physics test has been cancelled due to maintenance."
        val result = engine.analyze("Academic Office", message)

        // Must classify as CANCELLATION, not TEST_EXAM!
        assertEquals(NotificationCategory.CANCELLATION, result.category)
        assertTrue(result.confidence >= ConfidenceThresholds.RECOGNIZED)
        assertEquals("Physics", result.subjectOrTopic)
        assertEquals("Friday", result.dateText)
        assertEquals("No action required (Session cancelled)", result.actionItem)
    }

    @Test
    fun testStudyMaterialCategory() {
        val message = "Sharing the lecture notes and formula sheet pdf for Algorithms Chapter 3."
        val result = engine.analyze("Teaching Assistant", message)

        assertEquals(NotificationCategory.STUDY_MATERIAL, result.category)
        assertTrue(result.confidence >= ConfidenceThresholds.SUGGESTED)
        assertEquals("Algorithms", result.subjectOrTopic)
    }

    // 2. Unknown & Noise Handling

    @Test
    fun testUnknownCategoryForGeneralChatter() {
        val message = "Hey are you coming for dinner tonight at the pizza place?"
        val result = engine.analyze("Alex", message)

        assertEquals(NotificationCategory.UNKNOWN, result.category)
        assertTrue("Confidence should be below SUGGESTED", result.confidence < ConfidenceThresholds.SUGGESTED)
        assertFalse(result.isClassified)
    }

    @Test
    fun testEmptyOrTrivialMessage() {
        val message = "Ok"
        val result = engine.analyze("Bob", message)

        assertEquals(NotificationCategory.UNKNOWN, result.category)
        assertFalse(result.isClassified)
    }

    // 3. Negation & Cancellation Edge Cases

    @Test
    fun testCancellationNegationDoesNotTriggerUpcomingEvent() {
        val message = "Attention: Today's seminar has been called off."
        val result = engine.analyze("Events Desk", message)

        assertEquals(NotificationCategory.CANCELLATION, result.category)
        assertNotEquals(NotificationCategory.EVENT, result.category)
    }

    // 4. Missing Information & Zero Hallucination

    @Test
    fun testMissingDatesAndTimesAreNotHallucinated() {
        val message = "Please check the attached pdf study material for Biology."
        val result = engine.analyze("Bio Prof", message)

        assertEquals(NotificationCategory.STUDY_MATERIAL, result.category)
        assertNull("Date must not be hallucinated when not present", result.dateText)
        assertNull("Time must not be hallucinated when not present", result.timeText)
        assertNull("Venue must not be hallucinated when not present", result.venue)
        assertEquals("Biology", result.subjectOrTopic)
    }

    // 5. Multiple Category Matches & Disambiguation

    @Test
    fun testDeadlineDisambiguationOverGeneralHomework() {
        val message = "Strict deadline: submit assignment by Friday at 11:59 PM."
        val result = engine.analyze("Course Admin", message)

        // Deadline has higher priority when strict cutoff keywords are present
        assertEquals(NotificationCategory.DEADLINE, result.category)
        assertEquals("Friday", result.dateText)
        assertEquals("11:59 PM", result.timeText)
    }

    // 6. Confidence Thresholds

    @Test
    fun testConfidenceThresholdEvaluation() {
        val strongMessage = "Mathematics final exam scheduled for Monday in Hall B at 9:00 AM."
        val strongResult = engine.analyze("Dean", strongMessage)
        assertTrue(strongResult.isRecognized)
        assertTrue(strongResult.isClassified)

        val weakMessage = "Some slides attached."
        val weakResult = engine.analyze("Friend", weakMessage)
        assertTrue(weakResult.confidence >= ConfidenceThresholds.SUGGESTED)
        assertTrue(weakResult.isClassified)
    }
}
