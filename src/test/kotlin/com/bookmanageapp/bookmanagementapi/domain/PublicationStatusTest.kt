package com.bookmanageapp.bookmanagementapi.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PublicationStatusTest {
    @Test
    fun `未出版から出版済みへの遷移は許可される`() {
        // Arrange
        val fromStatus = PublicationStatus.UNPUBLISHED
        val toStatus = PublicationStatus.PUBLISHED

        // Act
        val result = fromStatus.canTransitionTo(toStatus)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `未出版から未出版への遷移は許可される`() {
        // Arrange
        val fromStatus = PublicationStatus.UNPUBLISHED
        val toStatus = PublicationStatus.UNPUBLISHED

        // Act
        val result = fromStatus.canTransitionTo(toStatus)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `出版済みから出版済みへの遷移は許可される`() {
        // Arrange
        val fromStatus = PublicationStatus.PUBLISHED
        val toStatus = PublicationStatus.PUBLISHED

        // Act
        val result = fromStatus.canTransitionTo(toStatus)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `出版済みから未出版への遷移は許可されない`() {
        // Arrange
        val fromStatus = PublicationStatus.PUBLISHED
        val toStatus = PublicationStatus.UNPUBLISHED

        // Act
        val result = fromStatus.canTransitionTo(toStatus)

        // Assert
        assertFalse(result)
    }
}
