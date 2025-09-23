package com.bookmanageapp.bookmanagementapi.service

import com.bookmanageapp.bookmanagementapi.domain.Book
import com.bookmanageapp.bookmanagementapi.domain.NewBook
import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import com.bookmanageapp.bookmanagementapi.dto.CreateBookRequest
import com.bookmanageapp.bookmanagementapi.dto.PagedResponse
import com.bookmanageapp.bookmanagementapi.dto.PaginationInfo
import com.bookmanageapp.bookmanagementapi.exception.AuthorsNotFoundException
import com.bookmanageapp.bookmanagementapi.exception.BookNotFoundException
import com.bookmanageapp.bookmanagementapi.exception.ValidationException
import com.bookmanageapp.bookmanagementapi.repository.AuthorRepository
import com.bookmanageapp.bookmanagementapi.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil

@Service
@Transactional
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
    private val authorService: AuthorService,
) {
    fun createBook(request: CreateBookRequest): Long {
        // 事前の件数チェック
        if (request.authorIds.isEmpty()) {
            throw ValidationException("著者を1人以上選択してください")
        }

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
        val totalPages = if (totalCount > 0) ceil(totalCount.toDouble() / size).toInt() else 0

        val paginationInfo =
            PaginationInfo(
                currentPage = page,
                pageSize = size,
                totalElements = totalCount,
                totalPages = totalPages,
                hasNext = page < totalPages,
                hasPrevious = page > 1,
            )

        return PagedResponse(
            content = books,
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
