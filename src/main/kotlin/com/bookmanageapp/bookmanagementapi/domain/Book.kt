package com.bookmanageapp.bookmanagementapi.domain

import java.math.BigDecimal
import java.time.LocalDateTime

data class Book(
    val id: Long? = null,
    val title: String,
    val price: BigDecimal,
    val publicationStatus: PublicationStatus,
    val authorIds: List<Long>,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(price >= BigDecimal.ZERO) { "Price must be greater than or equal to 0" }
        require(authorIds.isNotEmpty()) { "Book must have at least one author" }
    }

    fun canUpdatePublicationStatus(newStatus: PublicationStatus): Boolean {
        return publicationStatus.canTransitionTo(newStatus)
    }
}
