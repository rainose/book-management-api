package com.bookmanageapp.bookmanagementapi.util

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ValidPublicationStatusCodeValidatorTest {
    @InjectMockKs
    private lateinit var validator: ValidPublicationStatusCodeValidator

    @MockK
    private lateinit var context: ConstraintValidatorContext

    @Test
    fun `有効な出版ステータスコードの場合_trueが返される`() {
        // Arrange
        val validCode = "00"

        // Act
        val result = validator.isValid(validCode, context)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `別の有効な出版ステータスコードの場合_trueが返される`() {
        // Arrange
        val validCode = "01"

        // Act
        val result = validator.isValid(validCode, context)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `無効な出版ステータスコードの場合_falseが返される`() {
        // Arrange
        val invalidCode = "02"

        // Act
        val result = validator.isValid(invalidCode, context)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `nullの場合_trueが返される`() {
        // Arrange
        val nullCode = null

        // Act
        val result = validator.isValid(nullCode, context)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `空文字の場合_falseが返される`() {
        // Arrange
        val emptyCode = ""

        // Act
        val result = validator.isValid(emptyCode, context)

        // Assert
        assertFalse(result)
    }
}
