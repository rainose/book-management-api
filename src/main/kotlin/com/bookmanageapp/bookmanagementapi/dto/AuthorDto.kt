package com.bookmanageapp.bookmanagementapi.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime

data class CreateAuthorRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String,
    @field:NotNull(message = "Birth date is required")
    @field:Past(message = "Birth date must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate,
)

data class UpdateAuthorRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String,
    @field:NotNull(message = "Birth date is required")
    @field:Past(message = "Birth date must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate,
)

data class AuthorResponse(
    val id: Long,
    val name: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val updatedAt: LocalDateTime,
)

data class AuthorSummaryResponse(
    val id: Long,
    val name: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate,
)

data class AuthorBooksResponse(
    val author: AuthorResponse,
    val books: List<BookSummaryResponse>,
)
