package com.bookmanageapp.bookmanagementapi.domain

enum class PublicationStatus {
    UNPUBLISHED,
    PUBLISHED,
    ;

    fun canTransitionTo(newStatus: PublicationStatus): Boolean {
        return when (this) {
            UNPUBLISHED -> newStatus == PUBLISHED || newStatus == UNPUBLISHED
            PUBLISHED -> newStatus == PUBLISHED
        }
    }

    companion object {
        fun fromString(value: String): PublicationStatus {
            return values().find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid publication status: $value")
        }
    }
}
