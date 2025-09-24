package com.bookmanageapp.bookmanagementapi.service

import com.bookmanageapp.bookmanagementapi.domain.Book
import com.bookmanageapp.bookmanagementapi.domain.NewBook
import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import com.bookmanageapp.bookmanagementapi.dto.CreateBookRequest
import com.bookmanageapp.bookmanagementapi.dto.UpdateBookRequest
import com.bookmanageapp.bookmanagementapi.exception.InvalidRequestException
import com.bookmanageapp.bookmanagementapi.exception.NotFoundException
import com.bookmanageapp.bookmanagementapi.exception.OptimisticLockException
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
        // 重複を除去
        val uniqueAuthorIds = request.authorIds.distinct()

        // 著者の存在確認
        if (!isAuthorsExist(uniqueAuthorIds)) {
            throw NotFoundException("指定されたIDの著者が見つかりません: ${uniqueAuthorIds.joinToString(", ")}")
        }

        // publicationStatusを文字列からenumに変換
        val publicationStatus = PublicationStatus.fromCode(request.publicationStatus)

        val book =
            NewBook(
                title = request.title.trim(),
                price = request.price,
                currencyCode = request.currencyCode,
                publicationStatus = publicationStatus,
                authorIds = uniqueAuthorIds,
            )

        val bookId = bookRepository.create(book)
        return requireNotNull(bookId)
    }

    private fun isAuthorsExist(authorIds: List<Long>): Boolean {
        // 重複が既に除去されていることを前提とする
        val existingCount = authorRepository.countByIds(authorIds)
        return existingCount == authorIds.size
    }

    fun updateBook(
        id: Long,
        request: UpdateBookRequest,
    ) {
        val currentBook = bookRepository.findById(id) ?: throw NotFoundException("指定されたIDの書籍が見つかりません: $id")

        if (!currentBook.publicationStatus.canTransitionTo(request.publicationStatus)) {
            throw InvalidRequestException(
                "出版状況を出版済みから未出版に変更することはできません",
            )
        }

        // 重複を除去
        val uniqueAuthorIds = request.authorIds.distinct()

        // 著者の存在確認
        if (!isAuthorsExist(uniqueAuthorIds)) {
            throw NotFoundException("指定されたIDの著者が見つかりません: ${uniqueAuthorIds.joinToString(", ")}")
        }

        val updatedBook =
            Book(
                id = id,
                title = request.title.trim(),
                price = request.price,
                currencyCode = request.currencyCode,
                publicationStatus = request.publicationStatus,
                authorIds = uniqueAuthorIds,
                lockNo = request.lockNo,
            )

        val updatedRows = bookRepository.update(updatedBook)
        if (updatedRows == 0) {
            throw OptimisticLockException()
        }
    }
}
