package com.bookmanageapp.bookmanagementapi.exception

import com.bookmanageapp.bookmanagementapi.dto.ErrorResponse
import com.bookmanageapp.bookmanagementapi.dto.FieldError
import com.bookmanageapp.bookmanagementapi.dto.ValidationErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.sql.SQLException

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(
        ex: NotFoundException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = "見つかりません",
                message = ex.message ?: "リソースが見つかりません",
                path = getPath(request),
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequestException(
        ex: InvalidRequestException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "不正なリクエスト",
                message = ex.message ?: "不正なリクエストです",
                path = getPath(request),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: WebRequest,
    ): ResponseEntity<ValidationErrorResponse> {
        val fieldErrors =
            ex.bindingResult.fieldErrors.map { fieldError ->
                FieldError(
                    field = fieldError.field,
                    rejectedValue = fieldError.rejectedValue,
                    message = fieldError.defaultMessage ?: "不正な値です",
                )
            }

        val errorResponse =
            ValidationErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "バリデーションエラー",
                message = "リクエストのバリデーションに失敗しました",
                path = getPath(request),
                validationErrors = fieldErrors,
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(OptimisticLockException::class)
    fun handleOptimisticLockException(
        ex: OptimisticLockException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                status = HttpStatus.CONFLICT.value(),
                error = "競合",
                message = ex.message ?: "リソースが他のユーザーによって変更されました",
                path = getPath(request),
            )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(DatabaseException::class)
    fun handleDatabaseException(
        ex: DatabaseException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "データベースエラー",
                message = ex.message ?: "データベース操作に失敗しました",
                path = getPath(request),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(SQLException::class)
    fun handleSQLException(
        ex: SQLException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "データベースエラー",
                message = "データベース操作に失敗しました",
                path = getPath(request),
                details = listOf("SQL State: ${ex.sqlState}", "Error Code: ${ex.errorCode}"),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "サーバー内部エラー",
                message = "予期せぬエラーが発生しました",
                path = getPath(request),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    private fun getPath(request: WebRequest): String {
        return request.getDescription(false).removePrefix("uri=")
    }
}
