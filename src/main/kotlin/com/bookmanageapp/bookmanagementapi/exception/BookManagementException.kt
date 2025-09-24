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
    message: String = "The resource has been modified by another user. Please refresh and try again.",
) : BookManagementException(message)