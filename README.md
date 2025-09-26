# 書籍管理システム Backend API

書籍と著者の多対多関係を管理するRESTful APIシステムです。Kotlin、Spring Boot、jOOQ、PostgreSQLを使用して実装されています。

## 技術構成

- **言語**: Kotlin
- **フレームワーク**: Spring Boot 3.5.5
- **データベース**: PostgreSQL
- **ORM**: jOOQ 3.19.8
- **マイグレーション**: Flyway
- **ビルドツール**: Gradle (Groovy)
- **Java**: 17 (Amazon Corretto)
- **コンテナ**: Docker, Docker Compose

## プロジェクト構造

```
src/
├── main/
│   ├── kotlin/com/bookmanageapp/bookmanagementapi/
│   │   ├── config/                 # 設定クラス
│   │   │   ├── JooqConfig.kt
│   │   │   └── JooqExecuteListener.kt
│   │   ├── controller/             # REST APIコントローラー
│   │   │   ├── AuthorController.kt
│   │   │   └── BookController.kt
│   │   ├── domain/                 # ドメインオブジェクト
│   │   │   ├── Author.kt
│   │   │   ├── Book.kt
│   │   │   └── PublicationStatus.kt
│   │   ├── dto/                    # データ転送オブジェクト
│   │   │   ├── AuthorDto.kt
│   │   │   ├── BirthDateAware.kt
│   │   │   ├── BookDto.kt
│   │   │   └── ErrorResponse.kt
│   │   ├── exception/              # 例外処理
│   │   │   ├── BookManagementException.kt
│   │   │   └── GlobalExceptionHandler.kt
│   │   ├── repository/             # データアクセス層
│   │   │   ├── AuthorRepository.kt
│   │   │   ├── AuthorRepositoryImpl.kt
│   │   │   ├── BookRepository.kt
│   │   │   └── BookRepositoryImpl.kt
│   │   ├── service/                # ビジネスロジック層
│   │   │   ├── AuthorService.kt
│   │   │   └── BookService.kt
│   │   └── BookManagementApiApplication.kt
│   ├── resources/
│   │   ├── db/migration/           # Flywayマイグレーションファイル
│   │   │   ├── V1_0_0_20250920000001__create_books.sql
│   │   │   ├── V1_0_0_20250920000002__create_authors.sql
│   │   │   └── V1_0_0_20250920000003__create_book_authors.sql
│   │   └── application.properties
│   └── generated/                  # jOOQ生成コード
└── test/
    └── kotlin/                     # テストコード
```

## データベーススキーマ

### テーブル構成

- **m_books**: 書籍情報
  - id, title, price, currency_code, publication_status, lock_no, created_at, created_by, updated_at, updated_by

- **m_authors**: 著者情報
  - id, name, birth_date, lock_no, created_at, created_by, updated_at, updated_by

- **r_book_authors**: 書籍と著者の関係テーブル
  - book_id, author_id

## API エンドポイント

### 書籍API
- `POST /api/books` - 書籍の新規作成
- `PUT /api/books/{id}` - 書籍の更新
- `GET /api/books/{id}` - 書籍の詳細取得

### 著者API
- `POST /api/authors` - 著者の新規作成
- `PUT /api/authors/{id}` - 著者の更新
- `GET /api/authors/{id}/books` - 著者の書籍一覧取得

## 環境設定

### 必要な環境変数

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=bookmanageapp
DB_USER=bookmanage_dev
DB_PASSWORD=secret
```

### データベース接続設定

アプリケーションはPostgreSQLデータベースに接続します。設定は以下の優先順位で決定されます：

1. 環境変数
2. gradle.properties
3. デフォルト値

## セットアップ手順

### 1. データベースの起動

```bash
# Docker Composeを使用してPostgreSQLを起動
docker-compose up -d
```

### 2. マイグレーション実行

```bash
# Flywayでデータベーススキーマを作成
./gradlew flywayMigrate
```

### 3. jOOQコード生成

```bash
# データベーススキーマからKotlinコードを生成
./gradlew generateJooq
```

### 4. ビルド

```bash
# プロジェクトをビルド
./gradlew build
```

### 5. アプリケーション起動

```bash
# 開発サーバーを起動
./gradlew bootRun
```

## 開発用コマンド

### コード品質チェック

```bash
# Ktlint実行（コード生成ファイルは除外）
./gradlew ktlintCheck

# Ktlintフォーマット適用
./gradlew ktlintFormat
```

### テスト実行

```bash
# 全テスト実行
./gradlew test
```

## 主要な実装特徴

### アーキテクチャ

- **レイヤードアーキテクチャ**: Controller → Service → Repository の階層構造
- **依存性逆転の原則**: Repository層でインターフェースと実装を分離
- **楽観的排他制御**: lock_noによるバージョン管理

### ドメインオブジェクト

- **NewBook/Book**: 新規作成用と永続化済み用でドメインオブジェクトを分離
- **PublicationStatus**: 出版ステータスの状態遷移を管理
- **バリデーション**: DTOレベルでの入力検証

### データアクセス

- **jOOQ**: 型安全なSQL生成とKotlinコード自動生成
- **Flyway**: データベーススキーマのバージョン管理
- **HikariCP**: 高性能なコネクションプール

### 例外処理

- **GlobalExceptionHandler**: 統一的なエラーレスポンス
- **カスタム例外**: ビジネスロジック固有の例外定義

## 設定ファイル

### application.properties
- データベース接続設定
- jOOQ設定
- Flyway設定
- ログ設定

### build.gradle
- 依存関係管理
- jOOQコード生成設定
- Flyway設定
- Ktlint設定

## 本番環境での注意事項

1. **データベースパスワード**: 環境変数`DB_PASSWORD`を必ず設定
2. **Docker Compose**: 本番では`spring.docker.compose.enabled=false`のまま使用
3. **ログレベル**: 本番環境では適切なログレベルに調整
4. **接続プール**: HikariCPの設定を環境に応じて調整

## 開発者向け情報

このプロジェクトは、書籍と著者の管理を行うバックエンドAPIとして設計されています。型安全性を重視し、jOOQを使用したデータアクセス層の実装により、コンパイル時の型チェックを活用したレベル構造となっています。