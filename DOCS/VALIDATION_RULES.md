# バリデーションルール詳細

書籍管理システムのバリデーションルール詳細仕様です。

## 目次

- [書籍作成・更新リクエスト](#書籍作成・更新リクエスト)
- [著者作成・更新リクエスト](#著者作成・更新リクエスト)
- [バリデーションエラーレスポンス例](#バリデーションエラーレスポンス例)

## 書籍作成・更新リクエスト

### 共通フィールド

#### title（タイトル）
- **必須項目**（`@NotBlank`）
- **最大文字数**：255文字（`@Size(max = 255)`）
- **エラーメッセージ**：
  - 空白の場合：「タイトルは必須です」
  - 文字数超過：「タイトルは255文字以内で入力してください」

#### price（価格）
- **必須項目**（`@NotNull`）
- **最小値**：0以上（`@DecimalMin(value = "0.0", inclusive = true)`）
- **桁数制限**：整数部10桁、小数部2桁（`@Digits(integer = 10, fraction = 2)`）
- **エラーメッセージ**：
  - null の場合：「価格は必須です」
  - 負の値：「価格は0以上で入力してください」
  - 桁数超過：「価格は整数部10桁、小数部2桁以内で入力してください」

#### currencyCode（通貨コード）
- **必須項目**（`@NotBlank`）
- **文字数**：3文字固定（`@Size(min = 3, max = 3)`）
- **エラーメッセージ**：
  - 空白の場合：「通貨コードは必須です」
  - 文字数不正：「通貨コードは3文字で入力してください」

#### publicationStatus（出版状況）
- **必須項目**（`@NotBlank` または `@NotNull`）
- **カスタムバリデーション**（`@ValidPublicationStatusCode`）
- **有効値**：「00」（未出版）、「01」（出版済み）
- **エラーメッセージ**：
  - 空白・null の場合：「出版状況は必須です」
  - 無効値：「出版状況の値が不正です」

#### authorIds（著者IDリスト）
- **必須項目**（`@NotEmpty`）
- **最小要素数**：1以上（`@Size(min = 1)`）
- **各要素**：正の数（`@Positive`）
- **エラーメッセージ**：
  - 空リスト：「著者IDは必須です」 / 「著者は1人以上指定してください」
  - 不正な値：「著者IDは正の数でなければなりません」

### 更新固有フィールド

#### lockNo（楽観的ロック番号）
- **必須項目**（`@NotNull`）
- **エラーメッセージ**：「ロックナンバーは必須です」

## 著者作成・更新リクエスト

### 共通フィールド

#### name（著者名）
- **必須項目**（`@NotBlank`）
- **最大文字数**：255文字（`@Size(max = 255)`）
- **エラーメッセージ**：
  - 空白の場合：「名前は必須です」
  - 文字数超過：「名前は255文字以内で入力してください」

#### birthDate（生年月日）
- **必須項目**（`@NotNull`）
- **フォーマット**：yyyy-MM-dd（`@JsonFormat(pattern = "yyyy-MM-dd")`）
- **カスタムバリデーション**（`@ValidBirthDate`）
- **制約**：クライアントタイムゾーンで現在時刻より過去である必要がある
- **エラーメッセージ**：
  - null の場合：「生年月日は必須です」
  - 未来日付：「生年月日は過去の日付である必要があります」

#### clientTimeZone（クライアントタイムゾーン）
- **必須項目**（`@NotBlank`）
- **有効なタイムゾーンID**（例：Asia/Tokyo, UTC, America/New_York）
- **エラーメッセージ**：「クライアントのタイムゾーンは必須です」

### 更新固有フィールド

#### lockNo（楽観的ロック番号）
- **必須項目**（`@NotNull`）
- **エラーメッセージ**：「ロックナンバーは必須です」

## バリデーションエラーレスポンス例

### 複数フィールドエラー
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "バリデーションエラーが発生しました",
  "timestamp": "2025-09-26T12:00:00",
  "details": {
    "title": ["タイトルは必須です"],
    "price": ["価格は0以上で入力してください"],
    "authorIds": ["著者は1人以上指定してください"]
  }
}
```

### 書籍作成時のバリデーションエラー例
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "バリデーションエラーが発生しました",
  "timestamp": "2025-09-26T12:00:00",
  "details": {
    "title": ["タイトルは255文字以内で入力してください"],
    "price": ["価格は整数部10桁、小数部2桁以内で入力してください"],
    "currencyCode": ["通貨コードは3文字で入力してください"],
    "publicationStatus": ["出版状況の値が不正です"],
    "authorIds": ["著者IDは正の数でなければなりません"]
  }
}
```

### 著者作成時のバリデーションエラー例
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "バリデーションエラーが発生しました",
  "timestamp": "2025-09-26T12:00:00",
  "details": {
    "name": ["名前は必須です"],
    "birthDate": ["生年月日は過去の日付である必要があります"],
    "clientTimeZone": ["クライアントのタイムゾーンは必須です"]
  }
}
```

### 更新時のバリデーションエラー例
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "バリデーションエラーが発生しました",
  "timestamp": "2025-09-26T12:00:00",
  "details": {
    "lockNo": ["ロックナンバーは必須です"],
    "title": ["タイトルは必須です"]
  }
}
```

## バリデーション実装詳細

### カスタムバリデーションアノテーション

#### @ValidPublicationStatusCode
```kotlin
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PublicationStatusCodeValidator::class])
annotation class ValidPublicationStatusCode(
    val message: String = "出版状況の値が不正です",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
```

#### @ValidBirthDate
```kotlin
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [BirthDateValidator::class])
annotation class ValidBirthDate(
    val message: String = "生年月日は過去の日付である必要があります",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
```

### バリデーション処理フロー

1. **コントローラー層**：`@Valid`アノテーションによりリクエストデータを検証
2. **バリデーションエラー**：`MethodArgumentNotValidException`が発生
3. **GlobalExceptionHandler**：統一的なエラーレスポンスを返却

### データ型別制約まとめ

| 項目 | データ型 | 制約 |
|------|----------|------|
| ID | Long | 正の数 |
| タイトル・著者名 | String | 1-255文字、必須 |
| 価格 | BigDecimal | 0以上、整数部10桁・小数部2桁 |
| 通貨コード | String | 3文字固定、必須 |
| 出版状況 | String | "00" または "01"、必須 |
| 生年月日 | LocalDate | yyyy-MM-dd形式、過去日付、必須 |
| タイムゾーン | String | 有効なタイムゾーンID、必須 |
| ロック番号 | Long | 必須（更新時のみ） |