package com.bookmanageapp.bookmanagementapi.service

import com.bookmanageapp.bookmanagementapi.domain.Author
import com.bookmanageapp.bookmanagementapi.domain.NewAuthor
import com.bookmanageapp.bookmanagementapi.dto.AuthorBooksResponse
import com.bookmanageapp.bookmanagementapi.dto.AuthorResponse
import com.bookmanageapp.bookmanagementapi.dto.BookResponse
import com.bookmanageapp.bookmanagementapi.dto.CreateAuthorRequest
import com.bookmanageapp.bookmanagementapi.dto.UpdateAuthorRequest
import com.bookmanageapp.bookmanagementapi.exception.NotFoundException
import com.bookmanageapp.bookmanagementapi.exception.OptimisticLockException
import com.bookmanageapp.bookmanagementapi.repository.AuthorRepository
import com.bookmanageapp.bookmanagementapi.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 著者情報に関するビジネスロジックを提供するサービスクラス。
 *
 * @property authorRepository 著者リポジトリ
 * @property bookRepository 書籍リポジトリ
 * @author nose yudai
 */
@Service
@Transactional
class AuthorService(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository,
) {
    /**
     * 新しい著者を作成します。
     *
     * @param request 著者作成リクエスト
     * @return 作成された著者のID
     */
    fun createAuthor(request: CreateAuthorRequest): Long {
        val author =
            NewAuthor(
                name = request.name.trim(),
                birthDate = requireNotNull(request.birthDate),
            )
        return authorRepository.create(author)
    }

    /**
     * 既存の著者を更新します。
     *
     * @param id 更新する著者のID
     * @param request 著者更新リクエスト
     * @throws NotFoundException 指定されたIDの著者が存在しない場合
     * @throws OptimisticLockException 楽観的ロックに失敗した場合
     */
    fun updateAuthor(
        id: Long,
        request: UpdateAuthorRequest,
    ) {
        if (!authorRepository.existsById(id)) {
            throw NotFoundException("指定されたIDの著者が見つかりません")
        }

        val updatedAuthor =
            Author(
                id = id,
                name = request.name.trim(),
                birthDate = requireNotNull(request.birthDate),
                lockNo = requireNotNull(request.lockNo),
            )

        val updatedRows = authorRepository.update(updatedAuthor)
        if (updatedRows == 0) {
            throw OptimisticLockException()
        }
    }

    /**
     * 指定された著者の情報と、その著者が執筆した書籍のリストを取得します。
     *
     * @param authorId 著者のID
     * @return 著者情報と書籍リストを含むレスポンスオブジェクト
     * @throws NotFoundException 指定されたIDの著者が存在しない場合
     */
    @Transactional(readOnly = true)
    fun getAuthorBooks(authorId: Long): AuthorBooksResponse {
        // 著者の存在確認
        val author =
            authorRepository.findById(authorId)
                ?: throw NotFoundException("指定されたIDの著者が見つかりません")

        // 著者が書いた書籍を取得
        val books = bookRepository.findByAuthorId(authorId)

        // レスポンス用のDTOに変換
        val authorResponse =
            AuthorResponse(
                id = requireNotNull(author.id),
                name = author.name,
                birthDate = author.birthDate,
            )

        val bookResponses =
            books.map { book ->
                BookResponse(
                    id = book.id,
                    title = book.title,
                    price = book.price,
                    currencyCode = book.currencyCode,
                    publicationStatus = book.publicationStatus,
                )
            }

        return AuthorBooksResponse(
            author = authorResponse,
            books = bookResponses,
        )
    }
}
