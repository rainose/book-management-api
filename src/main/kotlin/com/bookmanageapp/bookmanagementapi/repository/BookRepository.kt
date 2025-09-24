package com.bookmanageapp.bookmanagementapi.repository

import com.bookmanageapp.bookmanagementapi.domain.Book
import com.bookmanageapp.bookmanagementapi.domain.NewBook

/**
 * 書籍情報に関するデータベース操作のインターフェース。
 *
 * @author nose yudai
 */
interface BookRepository {
    /**
     * 新しい書籍をデータベースに作成します。
     *
     * @param book 作成する書籍情報（IDなし）
     * @return 作成された書籍のID。作成に失敗した場合はnull
     */
    fun create(book: NewBook): Long?

    /**
     * 既存の書籍を更新します。
     *
     * @param book 更新する書籍情報（IDあり）
     * @return 更新された行数
     */
    fun update(book: Book): Int

    /**
     * IDを指定して書籍を検索します。
     *
     * @param id 検索する書籍のID
     * @return 見つかった書籍情報。見つからない場合はnull
     */
    fun findById(id: Long): Book?

    /**
     * 指定された著者IDに関連するすべての書籍を検索します。
     *
     * @param authorId 検索する著者のID
     * @return 見つかった書籍のリスト
     */
    fun findByAuthorId(authorId: Long): List<Book>
}
