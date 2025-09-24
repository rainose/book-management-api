package com.bookmanageapp.bookmanagementapi.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

/**
 * APIエラーレスポンスの汎用的なデータ転送オブジェクト(DTO)。
 *
 * @property timestamp エラーが発生したタイムスタンプ
 * @property status HTTPステータスコード
 * @property error エラーの概要
 * @property message エラーメッセージ
 * @property path リクエストされたパス
 * @property details エラーの詳細情報（オプション）
 * @author nose yudai
 */
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: List<String>? = null,
)

/**
 * バリデーションエラーレスポンスのデータ転送オブジェクト(DTO)。
 *
 * @property timestamp エラーが発生したタイムスタンプ
 * @property status HTTPステータスコード
 * @property error エラーの概要
 * @property message エラーメッセージ
 * @property path リクエストされたパス
 * @property validationErrors バリデーションエラーの詳細リスト
 * @author nose yudai
 */
data class ValidationErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val validationErrors: List<FieldError>,
)

/**
 * 個々のフィールドのバリデーションエラー情報を保持するデータ転送オブジェクト(DTO)。
 *
 * @property field エラーが発生したフィールド名
 * @property rejectedValue 検証に失敗した値
 * @property message エラーメッセージ
 * @author nose yudai
 */
data class FieldError(
    val field: String,
    val rejectedValue: Any?,
    val message: String,
)
