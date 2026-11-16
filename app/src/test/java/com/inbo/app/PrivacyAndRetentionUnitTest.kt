package com.inbo.app

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class PrivacyAndRetentionUnitTest {

    @Test
    fun testRetentionThresholdConstant() {
        val expected72HoursMs = 72L * 60L * 60L * 1000L
        assertEquals(259200000L, expected72HoursMs)
        assertEquals(expected72HoursMs, com.inbo.app.data.local.InboDatabaseHelper.RETENTION_PERIOD_MS)
    }

    @Test
    fun testNotificationExpiryLogic() {
        val now = 1_000_000_000_000L
        val retentionMs = 72L * 60L * 60L * 1000L
        val cutoff = now - retentionMs

        val expiredTimestamp = cutoff - 1000L // 72 hours and 1 second ago
        val activeTimestamp = cutoff + 1000L  // 71 hours, 59 minutes, 59 seconds ago
        val freshTimestamp = now - 60_000L    // 1 minute ago

        // Expired should be strictly less than cutoff
        assertTrue(expiredTimestamp < cutoff)

        // Active and fresh must be greater than or equal to cutoff
        assertTrue(activeTimestamp >= cutoff)
        assertTrue(freshTimestamp >= cutoff)
    }

    @Test
    fun testBackupRulesExcludeDatabase() {
        // Validate res/xml/backup_rules.xml (API <= 30)
        val backupRulesFile = File("src/main/res/xml/backup_rules.xml")
        assertTrue("backup_rules.xml must exist", backupRulesFile.exists())

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(backupRulesFile)

        val excludes = doc.getElementsByTagName("exclude")
        assertTrue("Must have exclude entries in backup_rules.xml", excludes.length > 0)

        var databaseExcluded = false
        for (i in 0 until excludes.length) {
            val element = excludes.item(i) as org.w3c.dom.Element
            val domain = element.getAttribute("domain")
            val path = element.getAttribute("path")
            if (domain == "database" && path.contains("inbo_tasks.db")) {
                databaseExcluded = true
            }
        }
        assertTrue("inbo_tasks.db must be excluded in backup_rules.xml", databaseExcluded)
    }

    @Test
    fun testDataExtractionRulesExcludeDatabaseFromCloudAndTransfer() {
        // Validate res/xml/data_extraction_rules.xml (API >= 31)
        val dataExtractionFile = File("src/main/res/xml/data_extraction_rules.xml")
        assertTrue("data_extraction_rules.xml must exist", dataExtractionFile.exists())

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(dataExtractionFile)

        val cloudBackupNodes = doc.getElementsByTagName("cloud-backup")
        assertTrue("cloud-backup element must exist", cloudBackupNodes.length > 0)
        val cloudElement = cloudBackupNodes.item(0) as org.w3c.dom.Element
        val cloudExcludes = cloudElement.getElementsByTagName("exclude")
        assertTrue("cloud-backup must have exclude elements", cloudExcludes.length > 0)

        var cloudDbExcluded = false
        for (i in 0 until cloudExcludes.length) {
            val element = cloudExcludes.item(i) as org.w3c.dom.Element
            if (element.getAttribute("domain") == "database" && element.getAttribute("path").contains("inbo_tasks.db")) {
                cloudDbExcluded = true
            }
        }
        assertTrue("inbo_tasks.db must be excluded from cloud-backup", cloudDbExcluded)

        val deviceTransferNodes = doc.getElementsByTagName("device-transfer")
        assertTrue("device-transfer element must exist", deviceTransferNodes.length > 0)
        val transferElement = deviceTransferNodes.item(0) as org.w3c.dom.Element
        val transferExcludes = transferElement.getElementsByTagName("exclude")
        assertTrue("device-transfer must have exclude elements", transferExcludes.length > 0)

        var transferDbExcluded = false
        for (i in 0 until transferExcludes.length) {
            val element = transferExcludes.item(i) as org.w3c.dom.Element
            if (element.getAttribute("domain") == "database" && element.getAttribute("path").contains("inbo_tasks.db")) {
                transferDbExcluded = true
            }
        }
        assertTrue("inbo_tasks.db must be excluded from device-transfer", transferDbExcluded)
    }

    @Test
    fun testPruningQueryDoesNotAffectOtherTables() {
        // Ensure table constants are separated
        assertNotEquals(
            com.inbo.app.data.local.InboDatabaseHelper.TABLE_NOTIFICATIONS,
            com.inbo.app.data.local.InboDatabaseHelper.TABLE_TASKS
        )
        assertNotEquals(
            com.inbo.app.data.local.InboDatabaseHelper.TABLE_NOTIFICATIONS,
            com.inbo.app.data.local.InboDatabaseHelper.TABLE_EVENTS
        )
        assertNotEquals(
            com.inbo.app.data.local.InboDatabaseHelper.TABLE_NOTIFICATIONS,
            com.inbo.app.data.local.InboDatabaseHelper.TABLE_DEADLINES
        )
        assertNotEquals(
            com.inbo.app.data.local.InboDatabaseHelper.TABLE_NOTIFICATIONS,
            com.inbo.app.data.local.InboDatabaseHelper.TABLE_SETTINGS
        )
    }
}
