package com.bookmanageapp.bookmanagementapi.domain

import java.math.BigDecimal

// 新規作成用（IDなし）
data class NewBook(
    val title: String,
    val price: BigDecimal,
    val currencyCode: String,
    val publicationStatus: PublicationStatus,
    val authorIds: List<Long>,
    val lockNo: Int = 1,
)

// 永続化済み（IDあり）
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
