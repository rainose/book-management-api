package com.bookmanageapp.bookmanagementapi.util

import com.bookmanageapp.bookmanagementapi.exception.ValidationException
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class ValidationUtilsTest : BehaviorSpec({

    // region validateAuthorName
    given("validateAuthorName") {
        `when`("有効な名前の場合") {
            then("エラーが発生しないこと") {
                shouldNotThrow<ValidationException> {
                    ValidationUtils.validateAuthorName("Natsume Soseki")
                }
            }
        }

        `when`("名前が空の場合") {
            then("ValidationExceptionが発生すること") {
                val exception = shouldThrow<ValidationException> {
                    ValidationUtils.validateAuthorName("")
                }
                exception.message shouldBe "Author name cannot be blank"
            }
        }

        `when`("名前が空白の場合") {
            then("ValidationExceptionが発生すること") {
                val exception = shouldThrow<ValidationException> {
                    ValidationUtils.validateAuthorName("   ")
                }
                exception.message shouldBe "Author name cannot be blank"
            }
        }

        `when`("名前が100文字を超える場合") {
            then("ValidationExceptionが発生すること") {
                val longName = "a".repeat(101)
                val exception = shouldThrow<ValidationException> {
                    ValidationUtils.validateAuthorName(longName)
                }
                exception.message shouldBe "Author name must not exceed 100 characters"
            }
        }
    }
    // endregion

    // region validateBirthDate
    given("validateBirthDate") {
        `when`("過去の日付の場合") {
            then("エラーが発生しないこと") {
                shouldNotThrow<ValidationException> {
                    ValidationUtils.validateBirthDate(LocalDate.now().minusDays(1))
                }
            }
        }

        `when`("未来の日付の場合") {
            then("ValidationExceptionが発生すること") {
                val exception = shouldThrow<ValidationException> {
                    ValidationUtils.validateBirthDate(LocalDate.now().plusDays(1))
                }
                exception.message shouldBe "Birth date must be in the past"
            }
        }

        `when`("今日の日付の場合") {
            then("ValidationExceptionが発生すること") {
                val exception = shouldThrow<ValidationException> {
                    ValidationUtils.validateBirthDate(LocalDate.now())
                }
                exception.message shouldBe "Birth date must be in the past"
            }
        }
    }
    // endregion
})