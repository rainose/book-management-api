package com.bookmanageapp.bookmanagementapi.util

import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import com.bookmanageapp.bookmanagementapi.exception.InvalidPublicationStatusTransitionException
import com.bookmanageapp.bookmanagementapi.exception.ValidationException
import java.math.BigDecimal
import java.time.LocalDate

object ValidationUtils {
    fun validatePublicationStatusTransition(
        currentStatus: PublicationStatus,
        newStatus: PublicationStatus,
    ) {
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw InvalidPublicationStatusTransitionException(
                currentStatus.name,
                newStatus.name,
            )
        }
    }

    fun validatePrice(price: BigDecimal) {
        if (price < BigDecimal.ZERO) {
            throw ValidationException("Price must be greater than or equal to 0")
        }
    }

    fun validateTitle(title: String) {
        if (title.isBlank()) {
            throw ValidationException("Title cannot be blank")
        }
        if (title.length > 255) {
            throw ValidationException("Title must not exceed 255 characters")
        }
    }

    fun validateAuthorName(name: String) {
        if (name.isBlank()) {
            throw ValidationException("Author name cannot be blank")
        }
        if (name.length > 100) {
            throw ValidationException("Author name must not exceed 100 characters")
        }
    }

    fun validateBirthDate(birthDate: LocalDate) {
        if (!birthDate.isBefore(LocalDate.now())) {
            throw ValidationException("Birth date must be in the past")
        }
    }

    fun validateAuthorIds(authorIds: List<Long>) {
        if (authorIds.isEmpty()) {
            throw ValidationException("Book must have at least one author")
        }
        if (authorIds.any { it <= 0 }) {
            throw ValidationException("All author IDs must be positive")
        }
    }

    fun validateId(
        id: Long,
        entityName: String,
    ) {
        if (id <= 0) {
            throw ValidationException("$entityName ID must be positive")
        }
    }

    fun validateCurrencyCode(currencyCode: String) {
        if (currencyCode.isBlank()) {
            throw ValidationException("Currency code cannot be blank")
        }
        if (currencyCode.length != 3) {
            throw ValidationException("Currency code must be exactly 3 characters")
        }
        if (!currencyCode.all { it.isUpperCase() && it.isLetter() }) {
            throw ValidationException("Currency code must contain only uppercase letters")
        }
    }

    fun validateLockNo(lockNo: Int) {
        if (lockNo <= 0) {
            throw ValidationException("Lock number must be positive")
        }
    }
}
