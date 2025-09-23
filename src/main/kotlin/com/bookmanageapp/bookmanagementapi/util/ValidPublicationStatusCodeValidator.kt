package com.bookmanageapp.bookmanagementapi.util

import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ValidPublicationStatusCodeValidator : ConstraintValidator<ValidPublicationStatusCode, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value == null) {
            // nullの場合は@NotNullに適切なエラーメッセージ表示を委ね、このvalidatorはパスする
            return true
        }

        return PublicationStatus.entries.any { it.code == value }
    }
}
