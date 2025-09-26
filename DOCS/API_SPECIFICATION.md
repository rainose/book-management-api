# API 仕様書

書籍管理システムのREST API詳細仕様です。

## 目次

- [API エンドポイント詳細](#api-エンドポイント詳細)
- [ビジネスルール詳細](#ビジネスルール詳細)
- [エラーハンドリング仕様](#エラーハンドリング仕様)

**📋 バリデーション実装詳細については [VALIDATION_RULES.md](VALIDATION_RULES.md) を参照してください。**

## API エンドポイント詳細

### 書籍API

#### POST /api/books - 書籍の新規作成

**リクエスト:**
```json
{
  "title": "Spring Boot実践ガイド",
  "price": 3200,
  "currencyCode": "JPY",
  "publicationStatus": "00",
  "authorIds": [1, 2]
}
```

**レスポンス (201 Created):**
```json
{
  "id": 1
}
```

#### PUT /api/books/{id} - 書籍の更新

**リクエスト:**
```json
{
  "title": "Spring Boot実践ガイド 第2版",
  "price": 3500,
  "currencyCode": "JPY",
  "publicationStatus": "01",
  "authorIds": [1, 2, 3],
  "lockNo": 1
}
```

**レスポンス (204 No Content):**
レスポンスボディなし

### 著者API

#### POST /api/authors - 著者の新規作成

**リクエスト:**
```json
{
  "name": "田中太郎",
  "birthDate": "1980-01-15",
  "clientTimeZone": "Asia/Tokyo"
}
```

**レスポンス (201 Created):**
```json
{
  "id": 1
}
```

#### PUT /api/authors/{id} - 著者の更新

**リクエスト:**
```json
{
  "name": "田中次郎",
  "birthDate": "1980-01-15",
  "lockNo": 1,
  "clientTimeZone": "Asia/Tokyo"
}
```

**レスポンス (204 No Content):**
レスポンスボディなし

#### GET /api/authors/{id}/books - 著者の書籍一覧取得

**レスポンス (200 OK):**
```json
{
  "author": {
    "id": 1,
    "name": "田中太郎",
    "birthDate": "1980-01-15"
  },
  "books": [
    {
      "id": 1,
      "title": "Spring Boot実践ガイド",
      "price": 3200,
      "currencyCode": "JPY",
      "publicationStatus": "01"
    }
  ]
}
```

## ビジネスルール詳細

### 出版状況遷移ルール

出版状況は以下の遷移ルールに従います：

- **"00"（未出版 - UNPUBLISHED）**
  - → "00"：同じ状況のまま（変更なし）
  - → "01"：出版可能

- **"01"（出版済み - PUBLISHED）**
  - → "01"：同じ状況のまま（変更なし）
  - → "00"：**不可**（一度出版された書籍は未出版に戻せない）

#### 違反例とエラー
```json
{
  "timestamp": "2025-09-26T12:00:00",
  "status": 400,
  "error": "不正なリクエスト",
  "message": "出版状況を出版済みから未出版に変更することはできません",
  "path": "/api/books/1"
}
```

### 著者関連のビジネスルール

#### 書籍と著者の関連
- **書籍には最低1人の著者が必要**
  - 新規作成時：authorIds は空リスト不可
  - 更新時：既存の著者をすべて削除することは不可

#### 著者存在チェック
- **指定された著者IDは必ず存在する必要がある**
  - データベース内に存在しない著者IDが含まれる場合はエラー
  - 複数の著者IDのうち一つでも存在しない場合は全体がエラー

#### 著者存在エラー例
```json
{
  "timestamp": "2025-09-26T12:00:00",
  "status": 404,
  "error": "見つかりません",
  "message": "指定された著者が見つかりません",
  "path": "/api/books"
}
```

### 楽観的排他制御

#### ロックナンバー（lockNo）の仕組み
- **更新時は現在のlockNoが必要**
- データ更新の際にlockNoを確認し、別のユーザーが先に更新していないかチェック
- 更新後はlockNoが自動的にインクリメントされる

#### 楽観的排他制御エラー例
```json
{
  "timestamp": "2025-09-26T12:00:00",
  "status": 409,
  "error": "競合",
  "message": "リソースが他のユーザーによって変更されました。画面を更新して再度お試しください。",
  "path": "/api/books/1"
}
```

### 日付・時刻関連ルール

#### 生年月日制約
- **著者の生年月日は今日以前の日付が有効**
- クライアントのタイムゾーンを考慮した現在時刻で判定
- 同日（今日）は有効（今日を含む過去日付が対象）

#### タイムゾーン処理
- クライアントから送信されるタイムゾーンを使用して日付判定
- 有効なタイムゾーンID（例：Asia/Tokyo, UTC, America/New_York）が必要

### 通貨コード制約

#### 対応通貨
- ISO 4217準拠の3文字通貨コード（例：JPY, USD, EUR）
- 大文字小文字を区別する
- 現在の実装では特定の通貨コードに制限はないが、3文字固定

### データ形式制約

#### 数値制限
- **価格（price）**：整数部10桁、小数部2桁まで（9,999,999,999.99が上限）
- **ID値**：正の整数のみ

#### 文字列制限
- **タイトル・著者名**：最大255文字
- **通貨コード**：3文字固定

## エラーハンドリング仕様

### エラーレスポンス形式

#### 標準エラーレスポンス
```json
{
  "timestamp": "2025-09-26T12:00:00",
  "status": 400,
  "error": "不正なリクエスト",
  "message": "出版状況を出版済みから未出版に変更することはできません",
  "path": "/api/books/1"
}
```

#### バリデーションエラーレスポンス
```json
{
  "timestamp": "2025-09-26T12:00:00",
  "status": 400,
  "error": "バリデーションエラー",
  "message": "リクエストのバリデーションに失敗しました",
  "path": "/api/books",
  "validation_errors": [
    {
      "field": "title",
      "rejectedValue": "",
      "message": "タイトルは必須です"
    },
    {
      "field": "price",
      "rejectedValue": -100,
      "message": "価格は0以上で入力してください"
    }
  ]
}
```

### HTTPステータスコードとエラータイプ

| ステータスコード | エラータイプ | 説明 | 例外クラス |
|---|---|---|---|
| 400 Bad Request | 不正なリクエスト | ビジネスルール違反、無効な遷移 | `InvalidRequestException` |
| 400 Bad Request | バリデーションエラー | 入力値の検証失敗 | `MethodArgumentNotValidException` |
| 404 Not Found | 見つかりません | 指定されたリソースが存在しない | `NotFoundException` |
| 409 Conflict | 競合 | 楽観的排他制御によるデータ競合 | `OptimisticLockException` |
| 500 Internal Server Error | データベースエラー | SQL実行時のエラー | `SQLException` |
| 500 Internal Server Error | サーバー内部エラー | 予期せぬシステムエラー | `Exception` |

### 具体的なエラーシナリオ

#### 1. リソース未存在エラー (404)
```json
{
  "timestamp": "2025-09-26T12:00:00",
  "status": 404,
  "error": "見つかりません",
  "message": "指定された著者が見つかりません",
  "path": "/api/authors/999/books"
}
```

#### 2. 出版状況遷移エラー (400)
```json
{
  "timestamp": "2025-09-26T12:00:00",
  "status": 400,
  "error": "不正なリクエスト",
  "message": "出版状況を出版済みから未出版に変更することはできません",
  "path": "/api/books/1"
}
```

#### 3. 楽観的排他制御エラー (409)
```json
{
  "timestamp": "2025-09-26T12:00:00",
  "status": 409,
  "error": "競合",
  "message": "リソースが他のユーザーによって変更されました。画面を更新して再度お試しください。",
  "path": "/api/books/1"
}
```

#### 4. データベースエラー (500)
```json
{
  "timestamp": "2025-09-26T12:00:00",
  "status": 500,
  "error": "データベースエラー",
  "message": "データベース操作に失敗しました",
  "path": "/api/books",
  "details": [
    "SQL State: 23505",
    "Error Code: 0"
  ]
}
```

#### 5. 予期せぬエラー (500)
```json
{
  "timestamp": "2025-09-26T12:00:00",
  "status": 500,
  "error": "サーバー内部エラー",
  "message": "予期せぬエラーが発生しました",
  "path": "/api/books"
}
```