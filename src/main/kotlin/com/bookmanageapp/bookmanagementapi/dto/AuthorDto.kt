package com.bookmanageapp.bookmanagementapi.dto

import com.bookmanageapp.bookmanagementapi.util.ValidBirthDate
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

/**
 * 著者作成リクエストのデータ転送オブジェクト(DTO)。
 *
 * @property name 著者名
 * @property birthDate 生年月日
 * @property clientTimeZone リクエスト元のクライアントのタイムゾーン
 * @author nose yudai
 */
@ValidBirthDate
data class CreateAuthorRequest(
    @field:NotBlank(message = "名前は必須です")
    @field:Size(max = 255, message = "名前は255文字以内で入力してください")
    val name: String,
    @field:NotNull(message = "生年月日は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd")
    override val birthDate: LocalDate?,
    @field:NotBlank(message = "クライアントのタイムゾーンは必須です")
    override val clientTimeZone: String,
) : BirthDateAware

/**
 * 著者更新リクエストのデータ転送オブジェクト(DTO)。
 *
 * @property name 著者名
 * @property birthDate 生年月日
 * @property lockNo 楽観的ロックのためのバージョン番号
 * @property clientTimeZone リクエスト元のクライアントのタイムゾーン
 * @author nose yudai
 */
@ValidBirthDate
data class UpdateAuthorRequest(
    @field:NotBlank(message = "名前は必須です")
    @field:Size(max = 255, message = "名前は255文字以内で入力してください")
    val name: String,
    @field:NotNull(message = "生年月日は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd")
    override val birthDate: LocalDate?,
    @field:NotNull(message = "ロックナンバーは必須です")
    val lockNo: Int?,
    @field:NotBlank(message = "クライアントのタイムゾーンは必須です")
    override val clientTimeZone: String,
) : BirthDateAware

/**
 * 著者情報のレスポンス用データ転送オブジェクト(DTO)。
 *
 * @property id 著者ID
 * @property name 著者名
 * @property birthDate 生年月日
 * @author nose yudai
 */
data class AuthorResponse(
    val id: Long?,
    val name: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate,
)

/**
 * 著者とその書籍リストのレスポンス用データ転送オブジェクト(DTO)。
 *
 * @property author 著者情報
 * @property books 書籍のサマリーリスト
 * @author nose yudai
 */
data class AuthorBooksResponse(
    val author: AuthorResponse,
    val books: List<BookResponse>,
)
