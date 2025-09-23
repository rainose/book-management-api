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
                    .set(M_BOOKS.PUBLICATION_STATUS, book.publicationStatus.name)
                    .set(M_BOOKS.LOCK_NO, book.lockNo)
                    .set(M_BOOKS.CREATED_AT, now)
                    .set(M_BOOKS.CREATED_BY, "api_executer")
                    .set(M_BOOKS.UPDATED_AT, now)
                    .set(M_BOOKS.UPDATED_BY, "api_executer")
                    .returningResult(M_BOOKS.ID)
                    .fetchOne()
                    ?.value1()

            if (bookId != null) {
                // 書籍と著者の関連をt_book_authorsテーブルに挿入
                book.authorIds.forEach { authorId ->
                    transactionDsl
                        .insertInto(T_BOOK_AUTHORS)
                        .set(T_BOOK_AUTHORS.BOOK_ID, bookId)
                        .set(T_BOOK_AUTHORS.AUTHOR_ID, authorId)
                        .execute()
                }
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
            publicationStatus = PublicationStatus.valueOf(bookRecord.publicationStatus),
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

            // まず書籍情報を更新
            val updatedRows =
                transactionDsl
                    .update(M_BOOKS)
                    .set(M_BOOKS.TITLE, book.title)
                    .set(M_BOOKS.PRICE, book.price)
                    .set(M_BOOKS.CURRENCY_CODE, book.currencyCode)
                    .set(M_BOOKS.PUBLICATION_STATUS, book.publicationStatus.name)
                    .set(M_BOOKS.LOCK_NO, book.lockNo + 1)
                    .set(M_BOOKS.UPDATED_AT, now)
                    .set(M_BOOKS.UPDATED_BY, "api_executer")
                    .where(
                        M_BOOKS.ID.eq(book.id)
                            .and(M_BOOKS.LOCK_NO.eq(book.lockNo)),
                    )
                    .execute()

            if (updatedRows > 0) {
                // 既存の著者関連を削除
                transactionDsl
                    .deleteFrom(T_BOOK_AUTHORS)
                    .where(T_BOOK_AUTHORS.BOOK_ID.eq(book.id))
                    .execute()

                // 新しい著者関連を挿入
                book.authorIds.forEach { authorId ->
                    transactionDsl
                        .insertInto(T_BOOK_AUTHORS)
                        .set(T_BOOK_AUTHORS.BOOK_ID, book.id)
                        .set(T_BOOK_AUTHORS.AUTHOR_ID, authorId)
                        .execute()
                }
            }

            updatedRows
        }
    }

    override fun findAll(): List<Book> {
        val books =
            dslContext
                .selectFrom(M_BOOKS)
                .orderBy(M_BOOKS.ID.asc())
                .fetch()
                .map { record ->
                    val authorIds =
                        dslContext
                            .select(T_BOOK_AUTHORS.AUTHOR_ID)
                            .from(T_BOOK_AUTHORS)
                            .where(T_BOOK_AUTHORS.BOOK_ID.eq(record.id))
                            .fetch()
                            .mapNotNull { it.value1() }

                    Book(
                        id = requireNotNull(record.id),
                        title = record.title,
                        price = record.price,
                        currencyCode = record.currencyCode,
                        publicationStatus = PublicationStatus.valueOf(record.publicationStatus),
                        authorIds = authorIds,
                        lockNo = requireNotNull(record.lockNo),
                    )
                }

        return books
    }

    override fun findAllWithPagination(
        page: Int,
        size: Int,
    ): Pair<List<Book>, Long> {
        val totalCount =
            dslContext
                .selectCount()
                .from(M_BOOKS)
                .fetchOne(0, Long::class.java) ?: 0L

        val books =
            dslContext
                .selectFrom(M_BOOKS)
                .orderBy(M_BOOKS.ID.asc())
                .limit(size)
                .offset((page - 1) * size)
                .fetch()
                .map { record ->
                    val authorIds =
                        dslContext
                            .select(T_BOOK_AUTHORS.AUTHOR_ID)
                            .from(T_BOOK_AUTHORS)
                            .where(T_BOOK_AUTHORS.BOOK_ID.eq(record.id))
                            .fetch()
                            .mapNotNull { it.value1() }

                    Book(
                        id = requireNotNull(record.id),
                        title = record.title,
                        price = record.price,
                        currencyCode = record.currencyCode,
                        publicationStatus = PublicationStatus.valueOf(record.publicationStatus),
                        authorIds = authorIds,
                        lockNo = requireNotNull(record.lockNo),
                    )
                }

        return Pair(books, totalCount)
    }
}
