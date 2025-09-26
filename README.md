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
  - id: 書籍ID（自動採番）
  - title: 書籍タイトル
  - price: 価格（0以上）
  - currency_code: 通貨コード（ISO 4217形式）
  - publication_status: 出版状態（UP:未出版, PB:出版済）
  - lock_no: 楽観的排他制御用バージョン番号
  - created_at: 作成日時
  - created_by: 作成者
  - updated_at: 更新日時
  - updated_by: 更新者

- **m_authors**: 著者情報
  - id: 著者ID（自動採番）
  - name: 著者名
  - birth_date: 生年月日（過去の日付のみ）
  - lock_no: 楽観的排他制御用バージョン番号
  - created_at: 作成日時
  - created_by: 作成者
  - updated_at: 更新日時
  - updated_by: 更新者

- **t_book_authors**: 書籍と著者の関係テーブル（多対多関係）
  - book_id: 書籍ID（m_booksテーブル参照）
  - author_id: 著者ID（m_authorsテーブル参照）

## API エンドポイント

### 書籍API
- `POST /api/books` - 書籍の新規作成
- `PUT /api/books/{id}` - 書籍の更新

### 著者API
- `POST /api/authors` - 著者の新規作成
- `PUT /api/authors/{id}` - 著者の更新
- `GET /api/authors/{id}/books` - 著者の書籍一覧取得

詳細な仕様については [API仕様書](DOCS/API_SPECIFICATION.md) を参照してください。

## バリデーションルール

### 主要なバリデーション
- **書籍**：タイトル（必須、255文字以内）、価格（0以上）、通貨コード（3文字）、出版状況（"00"/"01"）
- **著者**：名前（必須、255文字以内）、生年月日（過去日付）、タイムゾーン（有効なID）
- **楽観的排他制御**：更新時はlockNoが必須

詳細なバリデーションルールについては [バリデーションルール詳細](DOCS/VALIDATION_RULES.md) を参照してください。

## ビジネスルール

### 主要ルール
- **出版状況遷移**：未出版（"00"）→出版済み（"01"）は可能、逆は不可
- **著者関連**：書籍には最低1人の著者が必要、指定された著者IDは存在する必要がある
- **楽観的排他制御**：更新時はlockNoによる競合チェック
- **日付制約**：著者の生年月日はクライアントタイムゾーンで過去日付のみ

## エラーハンドリング

### HTTPステータスコード
- **400** : バリデーションエラー、ビジネスルール違反
- **404** : リソース未存在
- **409** : 楽観的排他制御による競合
- **500** : データベースエラー、システムエラー

詳細なエラーレスポンス形式については [API仕様書](DOCS/API_SPECIFICATION.md#エラーハンドリング仕様) を参照してください。

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

## ドキュメント

- [API仕様書](DOCS/API_SPECIFICATION.md) - REST APIの詳細仕様
- [バリデーションルール詳細](DOCS/VALIDATION_RULES.md) - 入力検証の詳細ルール

## 主要な実装特徴

### アーキテクチャ

- **レイヤードアーキテクチャ**: Controller → Service → Repository の階層構造
- **依存性逆転の原則**: Repository層でインターフェースと実装を分離
- **楽観的排他制御**: lock_noによるバージョン管理

### ドメインオブジェクト

- **NewBook/Book, NewAuthor/Author**: 新規作成用と永続化済み用でドメインオブジェクトを分離
- **PublicationStatus**: 出版ステータスの状態遷移を管理

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

## 実プロジェクトでの考慮事項

### セキュリティ・認証
- **認証・認可**: JWT認証またはOAuth 2.0とSpring Securityによるロールベースアクセス制御の実装
- **Cookie・セッション管理**: HTTPOnly、Secure、SameSite属性によるセキュアなCookie設定
- **Webセキュリティ**: CORS、CSP、HSTSなどのWeb標準セキュリティ対策

### データ品質・整合性
- **重複チェック**: 書籍タイトルや著者名の重複防止機能とユニーク制約の設定
- **データ整合性**: トランザクション管理とデッドロック対策の実装

### ユーザビリティ・保守性
- **国際化対応**: エラーメッセージをプロパティファイルで管理し、Accept-Languageヘッダーによる多言語対応
- **API設計**: OpenAPI仕様書の自動生成とAPIバージョニング戦略

### パフォーマンス・運用
- **パフォーマンス**: データベースインデックス、ページネーション、キャッシュ戦略の実装
- **監視・運用**: Spring Boot Actuatorによるメトリクス取得とヘルスチェック機能
- **テスト・品質**: 統合テスト環境の自動化とコードカバレッジ測定

## 開発ツール・AI支援

このプロジェクトの開発では以下のAIツールを使用：

- **実装**: Claude Code - コード作成、リファクタリング、デバッグ
- **調査・ドキュメント**: Gemini CLI - 技術調査、要件分析、ドキュメント作成
- **コードレビュー**: Gemini Code Assistant - コード品質チェック、セキュリティ監査

## 参考ガイドライン

### API設計・実装方針
- [Future Architect Web APIガイドライン](https://future-architect.github.io/arch-guidelines/documents/forWebAPI/web_api_guidelines.html)
- [Fintan Web APIガイドライン](https://fintan.jp/page/317/)

### Kotlin開発方針
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Gradle Best Practices for Kotlin](https://kotlinlang.org/docs/gradle-best-practices.html)
