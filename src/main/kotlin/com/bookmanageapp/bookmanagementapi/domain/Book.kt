package com.bookmanageapp.bookmanagementapi.domain

import java.math.BigDecimal

data class Book(
    val id: Long? = null,
    val title: String,
    val price: BigDecimal,
    val currencyCode: String,
    val publicationStatus: PublicationStatus,
    val authorIds: List<Long>,
    val lockNo: Int = 1,
) {
    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(price >= BigDecimal.ZERO) { "Price must be greater than or equal to 0" }
        require(currencyCode.isNotBlank()) { "Currency code cannot be blank" }
        require(currencyCode.length == 3) { "Currency code must be 3 characters" }
        require(authorIds.isNotEmpty()) { "Book must have at least one author" }
        require(lockNo > 0) { "Lock number must be positive" }
    }

    fun canUpdatePublicationStatus(newStatus: PublicationStatus): Boolean {
        return publicationStatus.canTransitionTo(newStatus)
    }
}
