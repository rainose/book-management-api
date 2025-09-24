package com.bookmanageapp.bookmanagementapi.dto

import com.bookmanageapp.bookmanagementapi.util.ValidBirthDate
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

@ValidBirthDate
data class CreateAuthorRequest(
    @field:NotBlank(message = "名前は必須です")
    @field:Size(max = 255, message = "名前は255文字以内で入力してください")
    val name: String,
    @field:NotNull(message = "生年月日は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd")
    override val birthDate: LocalDate,
    @field:NotBlank(message = "クライアントのタイムゾーンは必須です")
    override val clientTimeZone: String,
) : BirthDateAware

@ValidBirthDate
data class UpdateAuthorRequest(
    @field:NotBlank(message = "名前は必須です")
    @field:Size(max = 255, message = "名前は255文字以内で入力してください")
    val name: String,
    @field:NotNull(message = "生年月日は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd")
    override val birthDate: LocalDate,
    @field:NotNull(message = "ロックナンバーは必須です")
    val lockNo: Int,
    @field:NotBlank(message = "クライアントのタイムゾーンは必須です")
    override val clientTimeZone: String,
) : BirthDateAware

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
