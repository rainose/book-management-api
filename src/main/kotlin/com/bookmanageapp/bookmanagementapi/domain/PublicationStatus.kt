package com.bookmanageapp.bookmanagementapi.domain

enum class PublicationStatus(val code: String) {
    UNPUBLISHED("00"),
    PUBLISHED("01"), ;

    fun canTransitionTo(newStatus: PublicationStatus): Boolean {
        return when (this) {
            UNPUBLISHED -> newStatus == PUBLISHED || newStatus == UNPUBLISHED
            PUBLISHED -> newStatus == PUBLISHED
        }
    }

    companion object {
        fun fromString(value: String): PublicationStatus {
            return values().find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException()
        }

        fun fromCode(code: String): PublicationStatus {
            return values().find { it.code == code }
                ?: throw IllegalArgumentException()
        }
    }
}
