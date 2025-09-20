package com.bookmanageapp.bookmanagementapi.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: List<String>? = null,
) {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    fun getFormattedTimestamp(): LocalDateTime = timestamp
}

data class ValidationErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val validationErrors: List<FieldError>,
) {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    fun getFormattedTimestamp(): LocalDateTime = timestamp
}

data class FieldError(
    val field: String,
    val rejectedValue: Any?,
    val message: String,
)
