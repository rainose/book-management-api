package com.bookmanageapp.bookmanagementapi.repository

import com.bookmanageapp.bookmanagementapi.domain.Book
import com.bookmanageapp.bookmanagementapi.domain.NewBook
import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import com.bookmanageapp.jooq.tables.MBooks.Companion.M_BOOKS
import com.bookmanageapp.jooq.tables.TBookAuthors.Companion.T_BOOK_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class BookRepositoryImpl(
    private val dslContext: DSLContext,
) : BookRepository {
    override fun create(book: NewBook): Long? {
        val now = LocalDateTime.now()

        return dslContext.transactionResult { configuration ->
            val transactionDsl = configuration.dsl()

            // まず書籍をm_booksテーブルに挿入
            val bookId =
                transactionDsl
                    .insertInto(M_BOOKS)
                    .set(M_BOOKS.TITLE, book.title)
                    .set(M_BOOKS.PRICE, book.price)
                    .set(M_BOOKS.CURRENCY_CODE, book.currencyCode)
                    .set(M_BOOKS.PUBLICATION_STATUS, book.publicationStatus.code)
                    .set(M_BOOKS.LOCK_NO, book.lockNo)
                    .set(M_BOOKS.CREATED_AT, now)
                    .set(M_BOOKS.CREATED_BY, "api_executer")
                    .set(M_BOOKS.UPDATED_AT, now)
                    .set(M_BOOKS.UPDATED_BY, "api_executer")
                    .returningResult(M_BOOKS.ID)
                    .fetchOne()
                    ?.value1()

            if (bookId != null && book.authorIds.isNotEmpty()) {
                // 書籍と著者の関連をt_book_authorsテーブルに一括挿入
                val insertQueries =
                    book.authorIds.map { authorId ->
                        transactionDsl.insertInto(T_BOOK_AUTHORS)
                            .set(T_BOOK_AUTHORS.BOOK_ID, bookId)
                            .set(T_BOOK_AUTHORS.AUTHOR_ID, authorId)
                    }
                transactionDsl.batch(insertQueries).execute()
            }

            bookId
        }
    }

    override fun findById(id: Long): Book? {
        val bookRecord =
            dslContext
                .selectFrom(M_BOOKS)
                .where(M_BOOKS.ID.eq(id))
                .fetchOne()
                ?: return null

        val authorIds =
            dslContext
                .select(T_BOOK_AUTHORS.AUTHOR_ID)
                .from(T_BOOK_AUTHORS)
                .where(T_BOOK_AUTHORS.BOOK_ID.eq(id))
                .fetch()
                .mapNotNull { it.value1() }

        return Book(
            id = requireNotNull(bookRecord.id),
            title = bookRecord.title,
            price = bookRecord.price,
            currencyCode = bookRecord.currencyCode,
            publicationStatus = PublicationStatus.fromCode(bookRecord.publicationStatus),
            authorIds = authorIds,
            lockNo = requireNotNull(bookRecord.lockNo),
        )
    }

    override fun existsById(id: Long): Boolean {
        return (
            dslContext
                .selectCount()
                .from(M_BOOKS)
                .where(M_BOOKS.ID.eq(id))
                .fetchOne(0, Int::class.java) ?: 0
        ) > 0
    }

    override fun update(book: Book): Int {
        val now = LocalDateTime.now()

        return dslContext.transactionResult { configuration ->
            val transactionDsl = configuration.dsl()
            val bookId = requireNotNull(book.id) { "Book ID must not be null for update operation" }

            // 楽観的ロック付き更新
            val updatedRows =
                transactionDsl
                    .update(M_BOOKS)
                    .set(M_BOOKS.TITLE, book.title)
                    .set(M_BOOKS.PRICE, book.price)
                    .set(M_BOOKS.CURRENCY_CODE, book.currencyCode)
                    .set(M_BOOKS.PUBLICATION_STATUS, book.publicationStatus.code)
                    .set(M_BOOKS.LOCK_NO, book.lockNo + 1)
                    .set(M_BOOKS.UPDATED_AT, now)
                    .set(M_BOOKS.UPDATED_BY, "api_executer")
                    .where(
                        M_BOOKS.ID.eq(bookId)
                            .and(M_BOOKS.LOCK_NO.eq(book.lockNo)),
                    )
                    .execute()

            // 更新が成功した場合のみ著者関連を更新
            if (updatedRows > 0) {
                // 既存の著者関連を削除
                transactionDsl
                    .deleteFrom(T_BOOK_AUTHORS)
                    .where(T_BOOK_AUTHORS.BOOK_ID.eq(bookId))
                    .execute()

                // 新しい著者関連を一括挿入
                if (book.authorIds.isNotEmpty()) {
                    val insertQueries =
                        book.authorIds.map { authorId ->
                            transactionDsl.insertInto(T_BOOK_AUTHORS)
                                .set(T_BOOK_AUTHORS.BOOK_ID, bookId)
                                .set(T_BOOK_AUTHORS.AUTHOR_ID, authorId)
                        }
                    transactionDsl.batch(insertQueries).execute()
                }
            }

            updatedRows
        }
    }

    private fun findBooksWithPagination(
        limit: Int,
        offset: Int,
    ): Pair<List<Book>, Long> {
        // 1. 書籍総数を取得
        val totalCount =
            dslContext
                .selectCount()
                .from(M_BOOKS)
                .fetchOne(0, Long::class.java) ?: 0L

        // offsetが範囲外の場合、不要なクエリをスキップ
        if (offset >= totalCount) {
            return Pair(emptyList(), totalCount)
        }

        // 2. ページング対象の書籍を取得
        val bookRecords =
            dslContext
                .selectFrom(M_BOOKS)
                .orderBy(M_BOOKS.ID.asc())
                .limit(limit)
                .offset(offset)
                .fetch()

        if (bookRecords.isEmpty()) {
            return Pair(emptyList(), totalCount)
        }

        // 3. 書籍IDリストを抽出
        val bookIds = bookRecords.mapNotNull { it.id }

        // 4. 一度のクエリで対象書籍の著者情報を取得
        val bookAuthorMap =
            dslContext
                .select(T_BOOK_AUTHORS.BOOK_ID, T_BOOK_AUTHORS.AUTHOR_ID)
                .from(T_BOOK_AUTHORS)
                .where(T_BOOK_AUTHORS.BOOK_ID.`in`(bookIds))
                .fetch()
                .groupBy { it.value1() }
                .mapValues { entry -> entry.value.mapNotNull { it.value2() } }

        // 5. 書籍オブジェクトを構築
        val books =
            bookRecords.map { record ->
                val bookId = requireNotNull(record.id)
                val authorIds = bookAuthorMap[bookId] ?: emptyList()

                Book(
                    id = bookId,
                    title = record.title,
                    price = record.price,
                    currencyCode = record.currencyCode,
                    publicationStatus = PublicationStatus.fromCode(record.publicationStatus),
                    authorIds = authorIds,
                    lockNo = requireNotNull(record.lockNo),
                )
            }

        return Pair(books, totalCount)
    }

    override fun findAllWithPagination(
        page: Int,
        size: Int,
    ): Pair<List<Book>, Long> {
        val offset = (page - 1) * size
        return findBooksWithPagination(size, offset)
    }

    override fun findByAuthorId(authorId: Long): List<Book> {
        // 1. 指定された著者が書いた書籍IDを取得
        val bookIds =
            dslContext
                .select(T_BOOK_AUTHORS.BOOK_ID)
                .from(T_BOOK_AUTHORS)
                .where(T_BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
                .fetch()
                .mapNotNull { it.value1() }

        if (bookIds.isEmpty()) {
            return emptyList()
        }

        // 2. 書籍情報を取得
        val bookRecords =
            dslContext
                .selectFrom(M_BOOKS)
                .where(M_BOOKS.ID.`in`(bookIds))
                .orderBy(M_BOOKS.ID.asc())
                .fetch()

        // 3. 一度のクエリで対象書籍の全著者情報を取得
        val bookAuthorMap =
            dslContext
                .select(T_BOOK_AUTHORS.BOOK_ID, T_BOOK_AUTHORS.AUTHOR_ID)
                .from(T_BOOK_AUTHORS)
                .where(T_BOOK_AUTHORS.BOOK_ID.`in`(bookIds))
                .fetch()
                .groupBy { it.value1() }
                .mapValues { entry -> entry.value.mapNotNull { it.value2() } }

        // 4. 書籍オブジェクトを構築
        return bookRecords.map { record ->
            val bookId = requireNotNull(record.id)
            val allAuthorIds = bookAuthorMap[bookId] ?: emptyList()

            Book(
                id = bookId,
                title = record.title,
                price = record.price,
                currencyCode = record.currencyCode,
                publicationStatus = PublicationStatus.fromCode(record.publicationStatus),
                authorIds = allAuthorIds,
                lockNo = requireNotNull(record.lockNo),
            )
        }
    }
}
