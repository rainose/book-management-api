package com.bookmanageapp.bookmanagementapi.dto

import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import com.bookmanageapp.bookmanagementapi.util.ValidPublicationStatusCode
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

/**
 * 書籍作成リクエストのデータ転送オブジェクト(DTO)。
 *
 * @property title 書籍のタイトル
 * @property price 価格
 * @property currencyCode 通貨コード
 * @property publicationStatus 出版ステータスコード
 * @property authorIds 著者IDのリスト
 * @author nose yudai
 */
data class CreateBookRequest(
    @field:NotBlank(message = "タイトルは必須です")
    @field:Size(max = 255, message = "タイトルは255文字以内で入力してください")
    val title: String,
    @field:NotNull(message = "価格は必須です")
    @field:DecimalMin(value = "0.0", inclusive = true, message = "価格は0以上で入力してください")
    @field:Digits(integer = 10, fraction = 2, message = "価格は整数部10桁、小数部2桁以内で入力してください")
    val price: BigDecimal,
    @field:NotBlank(message = "通貨コードは必須です")
    @field:Size(min = 3, max = 3, message = "通貨コードは3文字で入力してください")
    val currencyCode: String,
    @field:NotBlank(message = "出版ステータスは必須です")
    @field:ValidPublicationStatusCode
    val publicationStatus: String,
    @field:NotEmpty(message = "著者IDは必須です")
    @field:Size(min = 1, message = "著者は1人以上指定してください")
    val authorIds: List<
        @Positive(message = "著者IDは正の数でなければなりません")
        Long,
        >,
)

/**
 * 書籍更新リクエストのデータ転送オブジェクト(DTO)。
 *
 * @property title 書籍のタイトル
 * @property price 価格
 * @property currencyCode 通貨コード
 * @property publicationStatus 出版ステータス
 * @property authorIds 著者IDのリスト
 * @property lockNo 楽観的ロックのためのバージョン番号
 * @author nose yudai
 */
data class UpdateBookRequest(
    @field:NotBlank(message = "タイトルは必須です")
    @field:Size(max = 255, message = "タイトルは255文字以内で入力してください")
    val title: String,
    @field:NotNull(message = "価格は必須です")
    @field:DecimalMin(value = "0.0", inclusive = true, message = "価格は0以上で入力してください")
    @field:Digits(integer = 10, fraction = 2, message = "価格は整数部10桁、小数部2桁以内で入力してください")
    val price: BigDecimal,
    @field:NotBlank(message = "通貨コードは必須です")
    @field:Size(min = 3, max = 3, message = "通貨コードは3文字で入力してください")
    val currencyCode: String,
    @field:NotNull(message = "出版ステータスは必須です")
    @field:ValidPublicationStatusCode
    val publicationStatus: String,
    @field:NotEmpty(message = "著者IDは必須です")
    @field:Size(min = 1, message = "著者は1人以上指定してください")
    val authorIds: List<
        @Positive(message = "著者IDは正の数でなければなりません")
        Long,
        >,
    @field:NotNull(message = "ロックナンバーは必須です")
    val lockNo: Int?,
)

/**
 * 書籍のサマリー情報のレスポンス用データ転送オブジェクト(DTO)。
 *
 * @property id 書籍ID
 * @property title 書籍のタイトル
 * @property price 価格
 * @property currencyCode 通貨コード
 * @property publicationStatus 出版ステータス
 * @author nose yudai
 */
data class BookSummaryResponse(
    val id: Long,
    val title: String,
    val price: BigDecimal,
    val currencyCode: String,
    val publicationStatus: PublicationStatus,
)
