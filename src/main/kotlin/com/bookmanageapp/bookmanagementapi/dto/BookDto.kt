package com.bookmanageapp.bookmanagementapi.dto

import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateBookRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String,
    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    @field:Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    val price: BigDecimal,
    @field:NotNull(message = "Publication status is required")
    val publicationStatus: PublicationStatus,
    @field:NotEmpty(message = "Author IDs are required")
    @field:Size(min = 1, message = "Book must have at least one author")
    val authorIds: List<
        @Positive(message = "Author ID must be positive")
        Long,
        >,
)

data class UpdateBookRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String,
    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    @field:Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    val price: BigDecimal,
    @field:NotNull(message = "Publication status is required")
    val publicationStatus: PublicationStatus,
    @field:NotEmpty(message = "Author IDs are required")
    @field:Size(min = 1, message = "Book must have at least one author")
    val authorIds: List<
        @Positive(message = "Author ID must be positive")
        Long,
        >,
)

data class BookResponse(
    val id: Long,
    val title: String,
    val price: BigDecimal,
    val publicationStatus: PublicationStatus,
    val authors: List<AuthorSummaryResponse>,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val updatedAt: LocalDateTime,
)

data class BookSummaryResponse(
    val id: Long,
    val title: String,
    val price: BigDecimal,
    val publicationStatus: PublicationStatus,
)
