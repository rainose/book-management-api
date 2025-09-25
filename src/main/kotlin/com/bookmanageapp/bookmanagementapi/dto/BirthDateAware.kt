package com.bookmanageapp.bookmanagementapi.dto

import java.time.LocalDate

/**
 * 生年月日とクライアントのタイムゾーンを持つオブジェクトを表すインターフェース。
 *
 * このインターフェースは、生年月日のバリデーションを共通化するために使用されます。
 *
 * @property birthDate 生年月日
 * @property clientTimeZone クライアントのタイムゾーンID (例: "Asia/Tokyo")
 * @author nose yudai
 */
interface BirthDateAware {
    val birthDate: LocalDate?
    val clientTimeZone: String
}
