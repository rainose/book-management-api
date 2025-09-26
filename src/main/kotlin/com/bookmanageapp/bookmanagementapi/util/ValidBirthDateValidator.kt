package com.bookmanageapp.bookmanagementapi.util

import com.bookmanageapp.bookmanagementapi.dto.BirthDateAware
import com.bookmanageapp.bookmanagementapi.repository.TimeProvider
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.beans.factory.annotation.Autowired
import java.time.DateTimeException
import java.time.ZoneId

/**
 * [ValidBirthDate]アノテーションに対応するバリデーションロジックを実装したクラス。
 *
 * @author nose yudai
 */
class ValidBirthDateValidator : ConstraintValidator<ValidBirthDate, BirthDateAware> {
    @Autowired
    private lateinit var timeProvider: TimeProvider

    /**
     * 指定されたリクエストオブジェクトの生年月日が有効かどうかを検証します。
     *
     * 生年月日は、リクエストで指定されたクライアントのタイムゾーンにおける現在の日付以前である必要があります。
     *
     * @param request 検証対象の[BirthDateAware]オブジェクト
     * @param context バリデーションコンテキスト
     * @return 生年月日が有効な場合はtrue、そうでない場合はfalse
     */
    override fun isValid(
        request: BirthDateAware?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (request == null || request.clientTimeZone.isBlank()) {
            // nullの場合は@NotNullに適切なエラーメッセージ表示を委ね、このvalidatorはパスする
            return true
        }

        try {
            ZoneId.of(request.clientTimeZone)
        } catch (e: DateTimeException) {
            // Invalid time zone ID provided
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate("無効なタイムゾーンIDが指定されました")
                ?.addPropertyNode("clientTimeZone")
                ?.addConstraintViolation()
            return false
        }

        val clientLocalDate = timeProvider.getCurrentDate(request.clientTimeZone)

        // 日付チェック
        if (request.birthDate.isAfter(clientLocalDate)) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(
                context.defaultConstraintMessageTemplate
                    ?: "生年月日はクライアントのタイムゾーンにおける今日以前の日付である必要があります",
            )
                ?.addPropertyNode("birthDate")
                ?.addConstraintViolation()
            return false
        }

        return true
    }
}
