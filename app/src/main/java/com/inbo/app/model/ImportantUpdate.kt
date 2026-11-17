
package com.inbo.app.model

data class ImportantUpdate(
    val id: Long = 0,
    val stableKey: String,
    val sourceNotificationKey: String,
    val senderOrTitle: String,
    val category: String,
    val confidence: Int,
    val summary: String,
    val actionItem: String? = null,
    val dateText: String? = null,
    val timeText: String? = null,
    val venue: String? = null,
    val details: String? = null,
    val status: String = "ACTIVE",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
