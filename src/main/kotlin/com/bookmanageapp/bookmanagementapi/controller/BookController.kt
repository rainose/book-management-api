package com.bookmanageapp.bookmanagementapi.controller

import com.bookmanageapp.bookmanagementapi.dto.CreateBookRequest
import com.bookmanageapp.bookmanagementapi.dto.UpdateBookRequest
import com.bookmanageapp.bookmanagementapi.service.BookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 書籍情報に関するAPIエンドポイントを提供するコントローラー。
 *
 * @property bookService 書籍情報サービス
 * @author nose yudai
 */
@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService,
) {
    /**
     * 新しい書籍を作成します。
     *
     * @param request 書籍作成リクエスト
     * @return 作成された書籍のIDを含むレスポンス (ステータスコード 201)
     */
    @PostMapping
    fun createBook(
        @Valid @RequestBody request: CreateBookRequest,
    ): ResponseEntity<Map<String, Long>> {
        val bookId = bookService.createBook(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("id" to bookId))
    }

    /**
     * 既存の書籍を更新します。
     *
     * @param id 更新する書籍のID
     * @param request 書籍更新リクエスト
     * @return ステータスコード204 (No Content)
     */
    @PutMapping("/{id}")
    fun updateBook(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateBookRequest,
    ): ResponseEntity<Void> {
        bookService.updateBook(id, request)
        return ResponseEntity.noContent().build()
    }
}
