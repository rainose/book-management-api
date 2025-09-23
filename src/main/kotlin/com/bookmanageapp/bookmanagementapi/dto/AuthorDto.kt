package com.bookmanageapp.bookmanagementapi.dto

import com.bookmanageapp.bookmanagementapi.util.ValidBirthDate
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

@ValidBirthDate
data class CreateAuthorRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    @field:NotNull(message = "Birth date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate,
    @field:NotBlank(message = "クライアントのタイムゾーンは必須です")
    val clientTimeZone: String,
)

@ValidBirthDate
data class UpdateAuthorRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    @field:NotNull(message = "Birth date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate,
    @field:NotNull(message = "Lock number is required")
    val lockNo: Int,
    @field:NotBlank(message = "クライアントのタイムゾーンは必須です")
    val clientTimeZone: String,
)

data class AuthorResponse(
    val id: Long?,
    val name: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate,
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
