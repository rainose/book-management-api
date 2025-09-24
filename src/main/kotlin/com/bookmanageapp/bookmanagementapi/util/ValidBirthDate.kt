package com.bookmanageapp.bookmanagementapi.util

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * 生年月日がクライアントのタイムゾーンにおける今日以前の日付であることを検証するカスタムアノテーション。
 *
 * このアノテーションは、[BirthDateAware]インターフェースを実装したクラスに適用されます。
 *
 * @property message バリデーションエラー時のメッセージ
 * @property groups バリデーションが属するグループ
 * @property payload バリデーションに関連するペイロード
 * @author nose yudai
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidBirthDateValidator::class])
annotation class ValidBirthDate(
    val message: String = "生年月日はクライアントのタイムゾーンにおける今日以前の日付である必要があります",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
