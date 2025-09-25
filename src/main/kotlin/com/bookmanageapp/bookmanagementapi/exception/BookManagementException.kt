package com.bookmanageapp.bookmanagementapi.exception

/**
 * このアプリケーションにおけるカスタム例外の基底クラス。
 *
 * @param message 例外メッセージ
 * @param cause 原因となった例外
 * @author nose yudai
 */
sealed class BookManagementException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * 要求されたリソースが見つからない場合にスローされる例外。
 *
 * @param message 例外メッセージ
 * @author nose yudai
 */
class NotFoundException(
    message: String,
) : BookManagementException(message)

/**
 * リクエストが無効である場合にスローされる例外。
 *
 * @param message 例外メッセージ
 * @author nose yudai
 */
class InvalidRequestException(
    message: String,
) : BookManagementException(message)

/**
 * 楽観的ロックに失敗した場合にスローされる例外。
 *
 * @param message 例外メッセージ
 * @author nose yudai
 */
class OptimisticLockException(
    message: String = "リソースが他のユーザーによって変更されました。画面を更新して再度お試しください。",
) : BookManagementException(message)
