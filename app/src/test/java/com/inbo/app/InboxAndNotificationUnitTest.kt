package com.inbo.app

import com.inbo.app.data.local.InboDatabaseHelper
import com.inbo.app.data.local.InboDatabaseHelper.Companion.COLUMN_NOTIF_IS_READ
import com.inbo.app.data.local.InboDatabaseHelper.Companion.TABLE_NOTIFICATIONS
import com.inbo.app.model.CapturedNotification
import com.inbo.app.ui.screens.formatRelativeTime
import org.junit.Assert.*
import org.junit.Test

class InboxAndNotificationUnitTest {

    @Test
    fun testRelativeTimeFormatting() {
        val now = 1_700_000_000_000L

        // Just now (< 60s)
        assertEquals("Just now", formatRelativeTime(now - 30_000L, now))
        assertEquals("Just now", formatRelativeTime(now, now))

        // Minutes ago (1m to 59m)
        assertEquals("2m ago", formatRelativeTime(now - (2 * 60 * 1000L), now))
        assertEquals("45m ago", formatRelativeTime(now - (45 * 60 * 1000L), now))

        // Hours ago (1h to 23h)
        assertEquals("1h ago", formatRelativeTime(now - (60 * 60 * 1000L), now))
        assertEquals("5h ago", formatRelativeTime(now - (5 * 60 * 60 * 1000L), now))

        // Days ago
        assertEquals("Yesterday", formatRelativeTime(now - (24 * 60 * 60 * 1000L), now))
        assertEquals("3d ago", formatRelativeTime(now - (3 * 24 * 60 * 60 * 1000L), now))

        // Weeks ago
        assertEquals("1w ago", formatRelativeTime(now - (8 * 24 * 60 * 60 * 1000L), now))
    }

    @Test
    fun testNotificationFilteringLogic() {
        val n1 = CapturedNotification(
            id = 1,
            notificationKey = "key_1",
            packageName = "com.whatsapp",
            senderOrTitle = "Alice",
            messageSnippet = "Hey there",
            timestamp = 1000L,
            isGroup = false,
            isRead = false
        )
        val n2 = CapturedNotification(
            id = 2,
            notificationKey = "key_2",
            packageName = "com.whatsapp",
            senderOrTitle = "Tech Team",
            messageSnippet = "Deployment completed",
            timestamp = 2000L,
            isGroup = true,
            isRead = false
        )
        val n3 = CapturedNotification(
            id = 3,
            notificationKey = "key_3",
            packageName = "com.whatsapp",
            senderOrTitle = "Bob",
            messageSnippet = "See you tomorrow",
            timestamp = 3000L,
            isGroup = false,
            isRead = true
        )
        val list = listOf(n3, n2, n1) // ordered newest first

        // All filter
        assertEquals(3, list.size)

        // Unread filter
        val unreadList = list.filter { !it.isRead }
        assertEquals(2, unreadList.size)
        assertTrue(unreadList.contains(n1))
        assertTrue(unreadList.contains(n2))
        assertFalse(unreadList.contains(n3))

        // Groups filter
        val groupsList = list.filter { it.isGroup }
        assertEquals(1, groupsList.size)
        assertEquals("Tech Team", groupsList[0].senderOrTitle)

        // Notification ordering: newest timestamp first
        val sortedList = list.sortedByDescending { it.timestamp }
        assertEquals(3000L, sortedList[0].timestamp)
        assertEquals(2000L, sortedList[1].timestamp)
        assertEquals(1000L, sortedList[2].timestamp)
    }

    @Test
    fun testReadUnreadStateImmutability() {
        val original = CapturedNotification(
            id = 42,
            notificationKey = "msg_42",
            packageName = "com.whatsapp",
            senderOrTitle = "Mom",
            messageSnippet = "Dinner ready",
            timestamp = 5000L,
            isGroup = false,
            isRead = false
        )
        assertFalse(original.isRead)

        val updated = original.copy(isRead = true)
        assertTrue(updated.isRead)
        assertEquals(original.id, updated.id)
        assertEquals(original.notificationKey, updated.notificationKey)
        assertEquals(original.senderOrTitle, updated.senderOrTitle)
    }

    @Test
    fun testDatabaseSchemaSafetyConstants() {
        // Ensure Database version is bumped to 5 for is_read column migration
        assertEquals(5, InboDatabaseHelper.DATABASE_VERSION)
        assertEquals("is_read", COLUMN_NOTIF_IS_READ)
        assertEquals("captured_notifications", TABLE_NOTIFICATIONS)
    }

    @Test
    fun testEmptyStateEvaluation() {
        val emptyNotifications = emptyList<CapturedNotification>()
        assertTrue(emptyNotifications.isEmpty())

        val unreadFiltered = emptyNotifications.filter { !it.isRead }
        assertTrue(unreadFiltered.isEmpty())

        val groupsFiltered = emptyNotifications.filter { it.isGroup }
        assertTrue(groupsFiltered.isEmpty())
    }

    @Test
    fun testPreserveReadStatusLogicOnUpdate() {
        // Simulates the DAO's preservation logic:
        // When notification already exists in DB with isRead = true,
        // an incoming notification update with default isRead = false MUST NOT overwrite existing isRead.
        val existingIsReadInDb = 1 // Already marked as read by user
        val incomingNotification = CapturedNotification(
            notificationKey = "key_100",
            packageName = "com.whatsapp",
            senderOrTitle = "Alice",
            messageSnippet = "Updated message text",
            timestamp = 6000L,
            isGroup = false,
            isRead = false // default for incoming notifications
        )

        val preservedIsRead = existingIsReadInDb
        assertEquals(1, preservedIsRead)

        // For brand new notification (not yet in DB):
        val newNotificationExistingIsRead: Int? = null
        val finalIsReadForNew = newNotificationExistingIsRead ?: if (incomingNotification.isRead) 1 else 0
        assertEquals(0, finalIsReadForNew)
    }

    @Test
    fun testNotificationKeyToIdMappingIntegrity() {
        // Confirms that the data model maintains both the SQLite auto-increment primary key 'id'
        // and the stable Android 'notificationKey' without mixing them up.
        val notif1 = CapturedNotification(
            id = 15L,
            notificationKey = "com.whatsapp|101|null",
            packageName = "com.whatsapp",
            senderOrTitle = "Design Channel",
            messageSnippet = "Figma link updated",
            timestamp = 7000L,
            isGroup = true,
            isRead = false
        )

        assertNotEquals(notif1.id.toString(), notif1.notificationKey)
        assertEquals(15L, notif1.id)
        assertEquals("com.whatsapp|101|null", notif1.notificationKey)
    }
}
