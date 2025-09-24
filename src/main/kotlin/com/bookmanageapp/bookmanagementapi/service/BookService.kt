package com.bookmanageapp.bookmanagementapi.service

import com.bookmanageapp.bookmanagementapi.domain.Book
import com.bookmanageapp.bookmanagementapi.domain.NewBook
import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import com.bookmanageapp.bookmanagementapi.dto.AuthorSummaryResponse
import com.bookmanageapp.bookmanagementapi.dto.BookResponse
import com.bookmanageapp.bookmanagementapi.dto.CreateBookRequest
import com.bookmanageapp.bookmanagementapi.dto.PagedResponse
import com.bookmanageapp.bookmanagementapi.dto.PaginationInfo
import com.bookmanageapp.bookmanagementapi.exception.AuthorsNotFoundException
import com.bookmanageapp.bookmanagementapi.exception.BookNotFoundException
import com.bookmanageapp.bookmanagementapi.repository.AuthorRepository
import com.bookmanageapp.bookmanagementapi.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {
    fun createBook(request: CreateBookRequest): Long {
        // 著者の存在確認
        if (!isAuthorsExist(request.authorIds)) {
            throw AuthorsNotFoundException(request.authorIds)
        }

        // publicationStatusを文字列からenumに変換
        val publicationStatus = PublicationStatus.fromCode(request.publicationStatus)

        val book =
            NewBook(
                title = request.title.trim(),
                price = request.price,
                currencyCode = request.currencyCode,
                publicationStatus = publicationStatus,
                authorIds = request.authorIds,
            )

        val bookId = bookRepository.create(book)
        return requireNotNull(bookId)
    }

    @Transactional(readOnly = true)
    fun getBook(id: Long): Book {
        return bookRepository.findById(id)
            ?: throw BookNotFoundException(id)
    }

    @Transactional(readOnly = true)
    fun getAllBooks(): List<Book> {
        return bookRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getAllBooksWithPagination(
        page: Int,
        size: Int,
    ): PagedResponse<Book> {
        val (books, totalCount) = bookRepository.findAllWithPagination(page, size)
        val paginationInfo = PaginationInfo.fromPageNumber(page, size, totalCount)

        return PagedResponse(
            content = books,
            pagination = paginationInfo,
        )
    }

    @Transactional(readOnly = true)
    fun getAllBooksWithAuthors(
        page: Int,
        size: Int,
    ): PagedResponse<BookResponse> {
        val (books, totalCount) = bookRepository.findAllWithPagination(page, size)

        // 全ての著者IDを収集
        val allAuthorIds = books.flatMap { it.authorIds }.distinct()

        // 著者情報を一括取得
        val authors = authorRepository.findByIds(allAuthorIds)
        val authorMap = authors.associateBy { it.id }

        // BookResponseに変換
        val bookResponses =
            books.map { book ->
                val bookAuthors =
                    book.authorIds.mapNotNull { authorId ->
                        authorMap[authorId]?.let { author ->
                            AuthorSummaryResponse(
                                id = author.id,
                                name = author.name,
                                birthDate = author.birthDate,
                            )
                        }
                    }

                BookResponse(
                    id = requireNotNull(book.id),
                    title = book.title,
                    price = book.price,
                    currencyCode = book.currencyCode,
                    publicationStatus = book.publicationStatus,
                    authors = bookAuthors,
                )
            }

        val paginationInfo = PaginationInfo.fromPageNumber(page, size, totalCount)

        return PagedResponse(
            content = bookResponses,
            pagination = paginationInfo,
        )
    }

    private fun isAuthorsExist(authorIds: List<Long>): Boolean {
        val existingCount = authorRepository.findByIds(authorIds).size
        return existingCount == authorIds.size
    }

    // TODO: UpdateBookRequestのpublicationStatus型をStringに修正後に実装

    /*
    fun updateBook(
        id: Long,
        request: UpdateBookRequest,
    ) {
        if (!bookRepository.existsById(id)) {
            throw BookNotFoundException(id)
        }

        // 著者の存在確認
        validateAuthorsExist(request.authorIds)

        // publicationStatusを文字列からenumに変換
        val publicationStatus = PublicationStatus.fromCode(request.publicationStatus)

        val updatedBook = Book(
            id = id,
            title = request.title.trim(),
            price = request.price,
            currencyCode = request.currencyCode,
            publicationStatus = publicationStatus,
            authorIds = request.authorIds,
        )

        val updatedRows = bookRepository.update(updatedBook)
        if (updatedRows == 0) {
            throw OptimisticLockException()
        }
    }
     */
}
