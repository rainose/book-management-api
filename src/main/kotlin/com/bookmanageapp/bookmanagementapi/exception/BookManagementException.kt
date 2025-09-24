package com.bookmanageapp.bookmanagementapi.exception

sealed class BookManagementException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class NotFoundException(
    message: String,
) : BookManagementException(message)

class InvalidRequestException(
    message: String,
) : BookManagementException(message)

class DatabaseException(
    message: String,
    cause: Throwable? = null,
) : BookManagementException(message, cause)

class OptimisticLockException(
    message: String = "リソースが他のユーザーによって変更されました。画面を更新して再度お試しください。",
) : BookManagementException(message)