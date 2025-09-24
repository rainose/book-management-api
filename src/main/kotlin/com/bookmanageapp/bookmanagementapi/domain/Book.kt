package com.bookmanageapp.bookmanagementapi.domain

import java.math.BigDecimal

/**
 * 新規作成用の書籍ドメインオブジェクト。IDは含まない。
 *
 * @property title 書籍のタイトル
 * @property price 価格
 * @property currencyCode 通貨コード
 * @property publicationStatus 出版ステータス
 * @property authorIds 著者IDのリスト
 * @property lockNo 楽観的ロックのためのバージョン番号
 * @author nose yudai
 */
data class NewBook(
    val title: String,
    val price: BigDecimal,
    val currencyCode: String,
    val publicationStatus: PublicationStatus,
    val authorIds: List<Long>,
    val lockNo: Int = 1,
)

/**
 * 永続化済みの書籍ドメインオブジェクト。IDを含む。
 *
 * @property id 書籍ID
 * @property title 書籍のタイトル
 * @property price 価格
 * @property currencyCode 通貨コード
 * @property publicationStatus 出版ステータス
 * @property authorIds 著者IDのリスト
 * @property lockNo 楽観的ロックのためのバージョン番号
 * @author nose yudai
 */
data class Book(
    val id: Long? = null,
    val title: String,
    val price: BigDecimal,
    val currencyCode: String,
    val publicationStatus: PublicationStatus,
    val authorIds: List<Long>,
    val lockNo: Int = 1,
) {
    /**
     * 指定された新しい出版ステータスに更新可能かどうかを判定します。
     *
     * @param newStatus 新しい出版ステータス
     * @return 更新可能な場合はtrue、そうでない場合はfalse
     */
    fun canUpdatePublicationStatus(newStatus: PublicationStatus): Boolean {
        return publicationStatus.canTransitionTo(newStatus)
    }
}
