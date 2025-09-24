package com.bookmanageapp.bookmanagementapi.domain

import java.time.LocalDate

/**
 * 新規作成用の著者ドメインオブジェクト。IDは含まない。
 *
 * @property name 著者名
 * @property birthDate 生年月日
 * @property lockNo 楽観的ロックのためのバージョン番号
 * @author nose yudai
 */
data class NewAuthor(
    val name: String,
    val birthDate: LocalDate,
    val lockNo: Int = 1,
)

/**
 * 永続化済みの著者ドメインオブジェクト。IDを含む。
 *
 * @property id 著者ID
 * @property name 著者名
 * @property birthDate 生年月日
 * @property lockNo 楽観的ロックのためのバージョン番号
 * @author nose yudai
 */
data class Author(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
    val lockNo: Int = 1,
)
