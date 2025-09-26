# バリデーション実装詳細

書籍管理システムのバリデーション実装詳細とカスタムバリデーターの仕様です。

**📋 基本的なAPIエンドポイントとビジネスルールについては [API_SPECIFICATION.md](API_SPECIFICATION.md) を参照してください。**

## 目次

- [カスタムバリデーションアノテーション](#カスタムバリデーションアノテーション)
- [バリデーション実装詳細](#バリデーション実装詳細)
- [実装ベースのバリデーションメッセージ](#実装ベースのバリデーションメッセージ)
- [バリデーション処理フロー](#バリデーション処理フロー)

## カスタムバリデーションアノテーション

### @ValidPublicationStatusCode

出版状況コードの妥当性を検証するカスタムアノテーション。

**実装場所**: `com.bookmanageapp.bookmanagementapi.util.ValidPublicationStatusCode`

```kotlin
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPublicationStatusCodeValidator::class])
annotation class ValidPublicationStatusCode(
    val message: String = "出版状況の値が不正です",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
```

**検証ロジック**:
- 有効値: "00"（未出版）、"01"（出版済み）
- デフォルトエラーメッセージ: "出版状況の値が不正です"

### @ValidBirthDate

生年月日がクライアントタイムゾーンで過去日付であることを検証するカスタムアノテーション。

**実装場所**: `com.bookmanageapp.bookmanagementapi.util.ValidBirthDate`

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidBirthDateValidator::class])
annotation class ValidBirthDate(
    val message: String = "生年月日はクライアントのタイムゾーンにおける今日以前の日付である必要があります",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
```

**検証ロジック**:
- `BirthDateAware`インターフェースを実装したクラスに適用
- クライアントのタイムゾーンを考慮した現在日時で判定
- 生年月日は現在日時より過去である必要がある
- デフォルトエラーメッセージ: "生年月日はクライアントのタイムゾーンにおける今日以前の日付である必要があります"

## 実装ベースのバリデーションメッセージ

### 書籍関連メッセージ（BookDto.kt）

**CreateBookRequest / UpdateBookRequest**:
```kotlin
// タイトル
@field:NotBlank(message = "タイトルは必須です")
@field:Size(max = 255, message = "タイトルは255文字以内で入力してください")
val title: String

// 価格
@field:NotNull(message = "価格は必須です")
@field:DecimalMin(value = "0.0", inclusive = true, message = "価格は0以上で入力してください")
@field:Digits(integer = 10, fraction = 2, message = "価格は整数部10桁、小数部2桁以内で入力してください")
val price: BigDecimal

// 通貨コード
@field:NotBlank(message = "通貨コードは必須です")
@field:Size(min = 3, max = 3, message = "通貨コードは3文字で入力してください")
val currencyCode: String

// 出版状況
@field:NotBlank(message = "出版ステータスは必須です")  // CreateBookRequest
@field:NotNull(message = "出版ステータスは必須です")   // UpdateBookRequest
@field:ValidPublicationStatusCode  // デフォルト: "出版状況の値が不正です"
val publicationStatus: String

// 著者IDs
@field:NotEmpty(message = "著者IDは必須です")
@field:Size(min = 1, message = "著者は1人以上指定してください")
val authorIds: List<@Positive(message = "著者IDは正の数でなければなりません") Long>

// ロックナンバー（更新時のみ）
@field:NotNull(message = "ロックナンバーは必須です")
val lockNo: Int?
```

### 著者関連メッセージ（AuthorDto.kt）

**CreateAuthorRequest / UpdateAuthorRequest**:
```kotlin
// 名前
@field:NotBlank(message = "名前は必須です")
@field:Size(max = 255, message = "名前は255文字以内で入力してください")
val name: String

// 生年月日
@field:NotNull(message = "生年月日は必須です")
@JsonFormat(pattern = "yyyy-MM-dd")
val birthDate: LocalDate?

// クライアントタイムゾーン
@field:NotBlank(message = "クライアントのタイムゾーンは必須です")
val clientTimeZone: String

// ロックナンバー（更新時のみ）
@field:NotNull(message = "ロックナンバーは必須です")
val lockNo: Int?
```

### カスタムバリデーションメッセージ

```kotlin
// ValidPublicationStatusCode.kt
val message: String = "出版状況の値が不正です"

// ValidBirthDate.kt
val message: String = "生年月日はクライアントのタイムゾーンにおける今日以前の日付である必要があります"
```

## バリデーション実装詳細

### エラーレスポンス構造（ErrorResponse.kt）

**標準エラーレスポンス**:
```kotlin
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: List<String>? = null
)
```

**バリデーションエラーレスポンス**:
```kotlin
data class ValidationErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val validationErrors: List<FieldError>
)

data class FieldError(
    val field: String,
    val rejectedValue: Any?,
    val message: String
)
```

### バリデーション処理フロー

1. **コントローラー層**：`@Valid`アノテーションによりリクエストデータを検証
2. **バリデーションエラー**：`MethodArgumentNotValidException`が発生
3. **GlobalExceptionHandler**：`ValidationErrorResponse`として統一的なエラーレスポンスを返却

**GlobalExceptionHandler.kt の実装**:
```kotlin
@ExceptionHandler(MethodArgumentNotValidException::class)
fun handleMethodArgumentNotValidException(
    ex: MethodArgumentNotValidException,
    request: WebRequest
): ResponseEntity<ValidationErrorResponse> {
    val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
        FieldError(
            field = fieldError.field,
            rejectedValue = fieldError.rejectedValue,
            message = fieldError.defaultMessage ?: "不正な値です"
        )
    }

    val errorResponse = ValidationErrorResponse(
        status = HttpStatus.BAD_REQUEST.value(),
        error = "バリデーションエラー",
        message = "リクエストのバリデーションに失敗しました",
        path = getPath(request),
        validationErrors = fieldErrors
    )
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
}
```

### データ型別制約まとめ

| 項目 | データ型 | 制約 | 実装場所 |
|------|----------|------|----------|
| ID | Long | 正の数（`@Positive`） | BookDto, AuthorDto |
| タイトル・著者名 | String | 1-255文字、必須（`@NotBlank`, `@Size`） | BookDto, AuthorDto |
| 価格 | BigDecimal | 0以上、整数部10桁・小数部2桁（`@DecimalMin`, `@Digits`） | BookDto |
| 通貨コード | String | 3文字固定、必須（`@NotBlank`, `@Size`） | BookDto |
| 出版状況 | String | "00" または "01"、必須（`@ValidPublicationStatusCode`） | BookDto |
| 生年月日 | LocalDate | yyyy-MM-dd形式、過去日付、必須（`@ValidBirthDate`） | AuthorDto |
| タイムゾーン | String | 有効なタイムゾーンID、必須（`@NotBlank`） | AuthorDto |
| ロック番号 | Int? | 必須（`@NotNull`、更新時のみ） | BookDto, AuthorDto |