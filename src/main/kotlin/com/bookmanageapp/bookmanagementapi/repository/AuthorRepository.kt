package com.bookmanageapp.bookmanagementapi.repository

import com.bookmanageapp.bookmanagementapi.domain.Author
import com.bookmanageapp.bookmanagementapi.domain.NewAuthor

/**
 * 著者情報に関するデータベース操作のインターフェース。
 *
 * @author nose yudai
 */
interface AuthorRepository {
    /**
     * 新しい著者をデータベースに作成します。
     *
     * @param author 作成する著者情報（IDなし）
     * @return 作成された著者のID。作成に失敗した場合はnull
     */
    fun create(author: NewAuthor): Long?

    /**
     * 既存の著者を更新します。
     *
     * @param author 更新する著者情報（IDあり）
     * @return 更新された行数
     */
    fun update(author: Author): Int

    /**
     * IDを指定して著者を検索します。
     *
     * @param id 検索する著者のID
     * @return 見つかった著者情報。見つからない場合はnull
     */
    fun findById(id: Long): Author?

    /**
     * 指定されたIDの著者が存在するかどうかを確認します。
     *
     * @param id 確認する著者のID
     * @return 存在する場合はtrue、そうでない場合はfalse
     */
    fun existsById(id: Long): Boolean

    /**
     * 指定されたIDリストに存在する著者の数をカウントします。
     *
     * @param ids カウント対象の著者IDリスト
     * @return 存在する著者の数
     */
    fun countByIds(ids: List<Long>): Int
}
