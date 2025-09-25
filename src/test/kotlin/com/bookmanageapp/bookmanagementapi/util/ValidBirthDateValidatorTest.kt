package com.bookmanageapp.bookmanagementapi.util

import com.bookmanageapp.bookmanagementapi.dto.BirthDateAware
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
class ValidBirthDateValidatorTest {
    @InjectMockKs
    private lateinit var validator: ValidBirthDateValidator

    @MockK
    private lateinit var context: ConstraintValidatorContext

    @MockK
    private lateinit var violationBuilder: ConstraintValidatorContext.ConstraintViolationBuilder

    @MockK
    private lateinit var propertyNodeBuilder: ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext

    @BeforeEach
    fun setUp() {
        // MockKのセットアップ
        every { context.buildConstraintViolationWithTemplate(any()) } returns violationBuilder
        every { violationBuilder.addPropertyNode(any()) } returns propertyNodeBuilder
        every { propertyNodeBuilder.addConstraintViolation() } returns context
        justRun { context.disableDefaultConstraintViolation() }
    }

    private data class TestBirthDateAware(
        override val birthDate: LocalDate,
        override val clientTimeZone: String,
    ) : BirthDateAware

    @Test
    fun `生年月日が今日の場合_trueが返される`() {
        // Arrange
        val clientTimeZone = "Asia/Tokyo"
        val today = LocalDate.now(ZoneId.of(clientTimeZone))
        val request = TestBirthDateAware(birthDate = today, clientTimeZone = clientTimeZone)

        // Act
        val result = validator.isValid(request, context)

        // Assert
        assertTrue(result)
        verify(exactly = 0) { context.buildConstraintViolationWithTemplate(any()) }
    }

    @Test
    fun `生年月日が過去の場合_trueが返される`() {
        // Arrange
        val clientTimeZone = "Asia/Tokyo"
        val yesterday = LocalDate.now(ZoneId.of(clientTimeZone)).minusDays(1)
        val request = TestBirthDateAware(birthDate = yesterday, clientTimeZone = clientTimeZone)

        // Act
        val result = validator.isValid(request, context)

        // Assert
        assertTrue(result)
        verify(exactly = 0) { context.buildConstraintViolationWithTemplate(any()) }
    }

    @Test
    fun `生年月日が未来の場合_falseが返されエラーメッセージが設定される`() {
        // Arrange
        val clientTimeZone = "Asia/Tokyo"
        val tomorrow = LocalDate.now(ZoneId.of(clientTimeZone)).plusDays(1)
        val request = TestBirthDateAware(birthDate = tomorrow, clientTimeZone = clientTimeZone)
        every { context.defaultConstraintMessageTemplate } returns "some error message"

        // Act
        val result = validator.isValid(request, context)

        // Assert
        assertFalse(result)
        verify { context.disableDefaultConstraintViolation() }
        verify { context.buildConstraintViolationWithTemplate("some error message") }
        verify { violationBuilder.addPropertyNode("birthDate") }
        verify { propertyNodeBuilder.addConstraintViolation() }
    }

    @Test
    fun `タイムゾーンが無効な場合_falseが返されエラーメッセージが設定される`() {
        // Arrange
        val request = TestBirthDateAware(birthDate = LocalDate.now(), clientTimeZone = "Invalid/Zone")

        // Act
        val result = validator.isValid(request, context)

        // Assert
        assertFalse(result)
        verify { context.disableDefaultConstraintViolation() }
        verify { context.buildConstraintViolationWithTemplate("無効なタイムゾーンIDが指定されました") }
        verify { violationBuilder.addPropertyNode("clientTimeZone") }
        verify { propertyNodeBuilder.addConstraintViolation() }
    }

    @Test
    fun `リクエストがnullの場合_trueが返される`() {
        // Arrange
        val request = null

        // Act
        val result = validator.isValid(request, context)

        // Assert
        assertTrue(result)
        verify(exactly = 0) { context.buildConstraintViolationWithTemplate(any()) }
    }

    @Test
    fun `タイムゾーンが空文字の場合_trueが返される`() {
        // Arrange
        val request = TestBirthDateAware(birthDate = LocalDate.now(), clientTimeZone = "")

        // Act
        val result = validator.isValid(request, context)

        // Assert
        assertTrue(result)
        verify(exactly = 0) { context.buildConstraintViolationWithTemplate(any()) }
    }
}
