package com.bookmanageapp.bookmanagementapi.util

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

        return value == "00" || value == "01"
    }
}
