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
    fun canUpdatePublicationStatus(newStatus: PublicationStatus): Boolean {
        return publicationStatus.canTransitionTo(newStatus)
    }
}
