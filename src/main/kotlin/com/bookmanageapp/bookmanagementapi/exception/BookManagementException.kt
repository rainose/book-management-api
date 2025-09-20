package com.bookmanageapp.bookmanagementapi.exception

sealed class BookManagementException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class BookNotFoundException(
    bookId: Long,
) : BookManagementException("Book not found with ID: $bookId")

class AuthorNotFoundException(
    authorId: Long,
) : BookManagementException("Author not found with ID: $authorId")

class AuthorsNotFoundException(
    authorIds: List<Long>,
) : BookManagementException("Authors not found with IDs: ${authorIds.joinToString(", ")}")

class InvalidPublicationStatusTransitionException(
    from: String,
    to: String,
) : BookManagementException("Invalid publication status transition from $from to $to")

class ValidationException(
    message: String,
) : BookManagementException(message)

class DatabaseException(
    message: String,
    cause: Throwable? = null,
) : BookManagementException(message, cause)
