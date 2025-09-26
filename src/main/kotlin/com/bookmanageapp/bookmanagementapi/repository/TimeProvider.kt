package com.bookmanageapp.bookmanagementapi.repository

import java.time.LocalDate

/**
 * 現在日時を取得するためのプロバイダーインターフェース。
 *
 * データベースから取得した時刻を基準とすることで、
 * 分散環境やテスト環境での時刻の一貫性を保証します。
 *
 * @author nose yudai
 */
interface TimeProvider {
    /**
     * 指定されたタイムゾーンにおける現在の日付を取得します。
     *
     * @param clientTimeZone クライアントのタイムゾーンID（例: "Asia/Tokyo"）
     * @return 現在の日付
     */
    fun getCurrentDate(clientTimeZone: String): LocalDate
}
