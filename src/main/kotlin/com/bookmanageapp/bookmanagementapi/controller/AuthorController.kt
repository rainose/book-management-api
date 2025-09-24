package com.bookmanageapp.bookmanagementapi.controller

import com.bookmanageapp.bookmanagementapi.dto.AuthorBooksResponse
import com.bookmanageapp.bookmanagementapi.dto.CreateAuthorRequest
import com.bookmanageapp.bookmanagementapi.dto.UpdateAuthorRequest
import com.bookmanageapp.bookmanagementapi.service.AuthorService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 著者情報に関するAPIエンドポイントを提供するコントローラー。
 *
 * @property authorService 著者情報サービス
 * @author nose yudai
 */
@RestController
@RequestMapping("/api/authors")
class AuthorController(
    private val authorService: AuthorService,
) {
    /**
     * 新しい著者を作成します。
     *
     * @param request 著者作成リクエスト
     * @return 作成された著者のIDを含むレスポンス (ステータスコード 201)
     */
    @PostMapping
    fun createAuthor(
        @Valid @RequestBody request: CreateAuthorRequest,
    ): ResponseEntity<Map<String, Long>> {
        val authorId = authorService.createAuthor(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("id" to authorId))
    }

    /**
     * 既存の著者を更新します。
     *
     * @param id 更新する著者のID
     * @param request 著者更新リクエスト
     * @return ステータスコード204 (No Content)
     */
    @PutMapping("/{id}")
    fun updateAuthor(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateAuthorRequest,
    ): ResponseEntity<Void> {
        authorService.updateAuthor(id, request)
        return ResponseEntity.noContent().build()
    }

    /**
     * 指定された著者が執筆した書籍のリストを取得します。
     *
     * @param id 著者のID
     * @return 著者とその書籍リストを含むレスポンス (ステータスコード 200)
     */
    @GetMapping("/{id}/books")
    fun getAuthorBooks(
        @PathVariable id: Long,
    ): ResponseEntity<AuthorBooksResponse> {
        val response = authorService.getAuthorBooks(id)
        return ResponseEntity.ok(response)
    }
}
