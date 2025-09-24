package com.bookmanageapp.bookmanagementapi.util

import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import com.bookmanageapp.bookmanagementapi.exception.InvalidRequestException
import java.math.BigDecimal
import java.time.LocalDate

object ValidationUtils {
    fun validatePublicationStatusTransition(
        currentStatus: PublicationStatus,
        newStatus: PublicationStatus,
    ) {
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw InvalidRequestException("Invalid publication status transition from ${currentStatus.name} to ${newStatus.name}")
        }
    }

    fun validatePrice(price: BigDecimal) {
        if (price < BigDecimal.ZERO) {
            throw InvalidRequestException("Price must be greater than or equal to 0")
        }
    }

    fun validateTitle(title: String) {
        if (title.isBlank()) {
            throw InvalidRequestException("Title cannot be blank")
        }
        if (title.length > 255) {
            throw InvalidRequestException("Title must not exceed 255 characters")
        }
    }

    fun validateAuthorName(name: String) {
        if (name.isBlank()) {
            throw InvalidRequestException("Author name cannot be blank")
        }
        if (name.length > 100) {
            throw InvalidRequestException("Author name must not exceed 100 characters")
        }
    }

    fun validateBirthDate(birthDate: LocalDate) {
        if (!birthDate.isBefore(LocalDate.now())) {
            throw InvalidRequestException("Birth date must be in the past")
        }
    }

    fun validateAuthorIds(authorIds: List<Long>) {
        if (authorIds.isEmpty()) {
            throw InvalidRequestException("Book must have at least one author")
        }
        if (authorIds.any { it <= 0 }) {
            throw InvalidRequestException("All author IDs must be positive")
        }
    }

    fun validateId(
        id: Long,
        entityName: String,
    ) {
        if (id <= 0) {
            throw InvalidRequestException("$entityName ID must be positive")
        }
    }

    fun validateCurrencyCode(currencyCode: String) {
        if (currencyCode.isBlank()) {
            throw InvalidRequestException("Currency code cannot be blank")
        }
        if (currencyCode.length != 3) {
            throw InvalidRequestException("Currency code must be exactly 3 characters")
        }
        if (!currencyCode.all { it.isUpperCase() && it.isLetter() }) {
            throw InvalidRequestException("Currency code must contain only uppercase letters")
        }
    }

    fun validateLockNo(lockNo: Int) {
        if (lockNo <= 0) {
            throw InvalidRequestException("Lock number must be positive")
        }
    }
}
