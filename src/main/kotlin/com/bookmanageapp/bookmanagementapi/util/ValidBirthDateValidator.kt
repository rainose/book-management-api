package com.bookmanageapp.bookmanagementapi.util

import com.bookmanageapp.bookmanagementapi.dto.BirthDateAware
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.DateTimeException
import java.time.LocalDate
import java.time.ZoneId

class ValidBirthDateValidator : ConstraintValidator<ValidBirthDate, BirthDateAware> {
    override fun isValid(
        request: BirthDateAware?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (request == null || request.clientTimeZone.isBlank()) {
            // Let other @NotNull/@NotBlank handle nulls/blanks
            return true
        }

        val birthDate = request.birthDate
        val clientTimeZoneId =
            try {
                ZoneId.of(request.clientTimeZone)
            } catch (e: DateTimeException) {
                // Invalid time zone ID provided
                context?.disableDefaultConstraintViolation()
                context?.buildConstraintViolationWithTemplate("無効なタイムゾーンIDが指定されました")
                    ?.addPropertyNode("clientTimeZone")
                    ?.addConstraintViolation()
                return false
            }

        val clientLocalDate = LocalDate.now(clientTimeZoneId)

        // birthDate <= clientLocalDate
        if (birthDate.isAfter(clientLocalDate)) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(
                context?.defaultConstraintMessageTemplate
                    ?: "生年月日はクライアントのタイムゾーンにおける今日以前の日付である必要があります",
            )
                ?.addPropertyNode("birthDate")
                ?.addConstraintViolation()
            return false
        }

        return true
    }
}
