package com.bookmanageapp.bookmanagementapi.domain

/**
 * 書籍の出版ステータスを表すEnum。
 *
 * @property code データベースに格納されるステータスコード
 * @author nose yudai
 */
enum class PublicationStatus(val code: String) {
    /** 未出版 */
    UNPUBLISHED("00"),

    /** 出版済み */
    PUBLISHED("01"), ;

    /**
     * 現在のステータスから新しいステータスへの遷移が可能かどうかを判定します。
     *
     * - `UNPUBLISHED`からは`PUBLISHED`または`UNPUBLISHED`への遷移が可能です。
     * - `PUBLISHED`からは`PUBLISHED`への遷移のみ可能です。
     *
     * @param newStatus 遷移先の新しいステータス
     * @return 遷移可能な場合はtrue、そうでない場合はfalse
     */
    fun canTransitionTo(newStatus: PublicationStatus): Boolean {
        return when (this) {
            UNPUBLISHED -> newStatus == PUBLISHED || newStatus == UNPUBLISHED
            PUBLISHED -> newStatus == PUBLISHED
        }
    }

    companion object {
        /**
         * 指定されたコード文字列から対応する[PublicationStatus]を取得します。
         *
         * @param code ステータスコード
         * @return 対応する[PublicationStatus]
         * @throws IllegalArgumentException 指定されたコードが見つからない場合
         */
        fun fromCode(code: String): PublicationStatus {
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("'$code' に対応する出版ステータスは存在しません。")
        }
    }
}
