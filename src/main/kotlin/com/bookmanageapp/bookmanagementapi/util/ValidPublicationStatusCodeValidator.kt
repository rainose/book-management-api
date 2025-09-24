package com.bookmanageapp.bookmanagementapi.util

import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * [ValidPublicationStatusCode]アノテーションに対応するバリデーションロジックを実装したクラス。
 *
 * @author nose yudai
 */
class ValidPublicationStatusCodeValidator : ConstraintValidator<ValidPublicationStatusCode, String> {
    /**
     * 指定された出版ステータスコードが有効かどうかを検証します。
     *
     * @param value 検証対象のステータスコード文字列
     * @param context バリデーションコンテキスト
     * @return ステータスコードが有効な場合はtrue、そうでない場合はfalse
     */
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value == null) {
            // nullの場合は@NotNullに適切なエラーメッセージ表示を委ね、このvalidatorはパスする
            return true
        }

        return PublicationStatus.entries.any { it.code == value }
    }
}
