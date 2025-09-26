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

/**
 * 書籍情報に関するビジネスロジックを提供するサービスクラス。
 *
 * @property bookRepository 書籍リポジトリ
 * @property authorRepository 著者リポジトリ
 * @author nose yudai
 */
@Service
@Transactional
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {
    /**
     * 新しい書籍を作成します。
     *
     * @param request 書籍作成リクエスト
     * @return 作成された書籍のID
     * @throws NotFoundException 指定された著者IDが存在しない場合
     */
    fun createBook(request: CreateBookRequest): Long {
        // クライアント側のミスで重複したIDが送られてきてもエラーにせず内部的に重複を除去
        val uniqueAuthorIds = request.authorIds.distinct()

        // 著者の存在確認
        if (!isAuthorsExist(uniqueAuthorIds)) {
            throw NotFoundException("指定されたIDの著者が見つかりません")
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

        return bookRepository.create(book)
    }

    /**
     * 指定された著者IDリストの著者がすべて存在するかどうかを確認します。
     *
     * @param authorIds 確認する著者IDのリスト
     * @return すべての著者が存在する場合はtrue、そうでない場合はfalse
     */
    private fun isAuthorsExist(authorIds: List<Long>): Boolean {
        // 重複が既に除去されていることを前提とする
        val existingCount = authorRepository.countByIds(authorIds)
        return existingCount == authorIds.size
    }

    /**
     * 既存の書籍を更新します。
     *
     * @param id 更新する書籍のID
     * @param request 書籍更新リクエスト
     * @throws NotFoundException 指定されたIDの書籍または著者が存在しない場合
     * @throws InvalidRequestException 出版ステータスの遷移が不正な場合
     * @throws OptimisticLockException 楽観的ロックに失敗した場合
     */
    fun updateBook(
        id: Long,
        request: UpdateBookRequest,
    ) {
        val currentBook = bookRepository.findById(id) ?: throw NotFoundException("指定されたIDの書籍が見つかりません")

        val updatePublicationStatus: PublicationStatus = PublicationStatus.fromCode(request.publicationStatus)
        if (!currentBook.canUpdatePublicationStatus(updatePublicationStatus)) {
            throw InvalidRequestException(
                "出版状況を出版済みから未出版に変更することはできません",
            )
        }

        // 重複を除去
        val uniqueAuthorIds = request.authorIds.distinct()

        // 著者の存在確認
        if (!isAuthorsExist(uniqueAuthorIds)) {
            throw NotFoundException("指定されたIDの著者が見つかりません")
        }

        val updatedBook =
            Book(
                id = id,
                title = request.title.trim(),
                price = request.price,
                currencyCode = request.currencyCode,
                publicationStatus = updatePublicationStatus,
                authorIds = uniqueAuthorIds,
                lockNo = requireNotNull(request.lockNo),
            )

        val updatedRows = bookRepository.update(updatedBook)
        if (updatedRows == 0) {
            throw OptimisticLockException()
        }
    }
}
