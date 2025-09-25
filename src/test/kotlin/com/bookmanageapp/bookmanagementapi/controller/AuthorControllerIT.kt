package com.bookmanageapp.bookmanagementapi.controller

import com.bookmanageapp.bookmanagementapi.domain.NewAuthor
import com.bookmanageapp.bookmanagementapi.domain.NewBook
import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import com.bookmanageapp.bookmanagementapi.dto.CreateAuthorRequest
import com.bookmanageapp.bookmanagementapi.dto.UpdateAuthorRequest
import com.bookmanageapp.bookmanagementapi.repository.AuthorRepository
import com.bookmanageapp.bookmanagementapi.repository.BookRepository
import com.bookmanageapp.jooq.tables.MAuthors.Companion.M_AUTHORS
import com.bookmanageapp.jooq.tables.MBooks.Companion.M_BOOKS
import com.bookmanageapp.jooq.tables.TBookAuthors.Companion.T_BOOK_AUTHORS
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.greaterThan
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test-testcontainers")
@Transactional
class AuthorControllerIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var authorRepository: AuthorRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var dslContext: DSLContext

    companion object {
        @Container
        @JvmStatic
        val postgreSQLContainer =
            PostgreSQLContainer("postgres:15")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgreSQLContainer::getUsername)
            registry.add("spring.datasource.password", postgreSQLContainer::getPassword)
        }
    }

    @BeforeEach
    fun setUp() {
        // テスト用のセットアップは各テストメソッドの前に実行される
    }

    private fun createTestAuthor(
        name: String,
        birthDate: LocalDate,
    ): Long {
        val authorId = authorRepository.create(NewAuthor(name = name, birthDate = birthDate))
        return requireNotNull(authorId)
    }

    private fun createTestBook(
        title: String,
        price: BigDecimal,
        currencyCode: String,
        publicationStatus: PublicationStatus,
        authorIds: List<Long>,
    ): Long {
        val newBook =
            NewBook(
                title = title,
                price = price,
                currencyCode = currencyCode,
                publicationStatus = publicationStatus,
                authorIds = authorIds,
            )
        val bookId = bookRepository.create(newBook)
        return requireNotNull(bookId)
    }

    private fun findAuthorById(id: Long) =
        dslContext
            .select(M_AUTHORS.asterisk())
            .from(M_AUTHORS)
            .where(M_AUTHORS.ID.eq(id))
            .fetchOne()

    private fun findBooksByAuthorId(authorId: Long) =
        dslContext
            .select(M_BOOKS.asterisk())
            .from(M_BOOKS)
            .join(T_BOOK_AUTHORS).on(M_BOOKS.ID.eq(T_BOOK_AUTHORS.BOOK_ID))
            .where(T_BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetch()

    @Nested
    inner class CreateAuthorTests {
        @Test
        fun `正常なリクエストの場合_201ステータスと作成されたIDが返される`() {
            // 1. 準備 (Arrange)
            val request =
                CreateAuthorRequest(
                    name = "テスト著者",
                    birthDate = LocalDate.of(1980, 1, 1),
                    clientTimeZone = "Asia/Tokyo",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(greaterThan(0)))

            // 4. DB検証 - 登録データを確認
            val createdAuthor =
                dslContext
                    .selectFrom(M_AUTHORS)
                    .where(M_AUTHORS.NAME.eq(request.name))
                    .fetchOne()
            assertThat(createdAuthor).isNotNull
            assertThat(createdAuthor!!.get(M_AUTHORS.NAME)).isEqualTo(request.name)
            assertThat(createdAuthor.get(M_AUTHORS.BIRTH_DATE)).isEqualTo(request.birthDate)
        }

        @Test
        fun `名前が空文字の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（名前が空）
            val invalidRequest =
                CreateAuthorRequest(
                    // 空の名前（バリデーションエラー）
                    name = "",
                    birthDate = LocalDate.of(1980, 1, 1),
                    clientTimeZone = "Asia/Tokyo",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'name')].message").value("名前は必須です"))
        }

        @Test
        fun `名前が256文字以上の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（名前が256文字）
            val longName = "a".repeat(256) // 256文字の名前（バリデーションエラー）
            val invalidRequest =
                CreateAuthorRequest(
                    name = longName,
                    birthDate = LocalDate.of(1980, 1, 1),
                    clientTimeZone = "Asia/Tokyo",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'name')].message").value("名前は255文字以内で入力してください"))
        }

        @Test
        fun `生年月日がnullの場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（生年月日がnull）
            val invalidJson =
                """
                {
                    "name": "テスト著者",
                    "birthDate": null,
                    "clientTimeZone": "Asia/Tokyo"
                }
                """.trimIndent()

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'birthDate')].message").value("生年月日は必須です"))
        }

        @Test
        fun `生年月日が未来日付の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（生年月日が未来）
            val futureDate = LocalDate.now().plusDays(1) // 未来の日付（バリデーションエラー）
            val invalidRequest =
                CreateAuthorRequest(
                    name = "テスト著者",
                    birthDate = futureDate,
                    clientTimeZone = "Asia/Tokyo",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'birthDate')].message").exists())
        }

        @Test
        fun `クライアントタイムゾーンが空の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（クライアントタイムゾーンが空）
            val invalidRequest =
                CreateAuthorRequest(
                    name = "テスト著者",
                    birthDate = LocalDate.of(1980, 1, 1),
                    // 空のクライアントタイムゾーン（バリデーションエラー）
                    clientTimeZone = "",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'clientTimeZone')].message").value("クライアントのタイムゾーンは必須です"))
        }
    }

    @Nested
    inner class UpdateAuthorTests {
        @Test
        fun `正常なリクエストの場合_204ステータスが返される`() {
            // 1. 準備 (Arrange)
            // テスト用の著者を事前に作成
            val createdAuthorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 更新リクエストを準備
            val updateRequest =
                UpdateAuthorRequest(
                    name = "更新されたテスト著者",
                    birthDate = LocalDate.of(1985, 5, 15),
                    lockNo = 1,
                    clientTimeZone = "Asia/Tokyo",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/authors/{id}", createdAuthorId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            )
                .andExpect(status().isNoContent)

            // 4. DB検証 - 更新データを確認
            val updatedAuthor = findAuthorById(createdAuthorId)
            assertThat(updatedAuthor).isNotNull
            assertThat(updatedAuthor!!.get(M_AUTHORS.NAME)).isEqualTo(updateRequest.name)
            assertThat(updatedAuthor.get(M_AUTHORS.BIRTH_DATE)).isEqualTo(updateRequest.birthDate)
        }

        @Test
        fun `名前が空文字の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange)
            val createdAuthorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 無効なリクエスト（名前が空）
            val invalidRequest =
                UpdateAuthorRequest(
                    // 空の名前（バリデーションエラー）
                    name = "",
                    birthDate = LocalDate.of(1985, 5, 15),
                    lockNo = 1,
                    clientTimeZone = "Asia/Tokyo",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/authors/{id}", createdAuthorId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'name')].message").value("名前は必須です"))
        }

        @Test
        fun `名前が256文字以上の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange)
            val createdAuthorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 無効なリクエスト（名前が256文字）
            val longName = "a".repeat(256) // 256文字の名前（バリデーションエラー）
            val invalidRequest =
                UpdateAuthorRequest(
                    name = longName,
                    birthDate = LocalDate.of(1985, 5, 15),
                    lockNo = 1,
                    clientTimeZone = "Asia/Tokyo",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/authors/{id}", createdAuthorId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'name')].message").value("名前は255文字以内で入力してください"))
        }

        @Test
        fun `生年月日がnullの場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange)
            val createdAuthorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 無効なリクエスト（生年月日がnull）
            val invalidJson =
                """
                {
                    "name": "更新されたテスト著者",
                    "birthDate": null,
                    "lockNo": 1,
                    "clientTimeZone": "Asia/Tokyo"
                }
                """.trimIndent()

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/authors/{id}", createdAuthorId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'birthDate')].message").value("生年月日は必須です"))
        }

        @Test
        fun `生年月日が未来日付の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange)
            val createdAuthorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 無効なリクエスト（生年月日が未来）
            val futureDate = LocalDate.now().plusDays(1) // 未来の日付（バリデーションエラー）
            val invalidRequest =
                UpdateAuthorRequest(
                    name = "更新されたテスト著者",
                    birthDate = futureDate,
                    lockNo = 1,
                    clientTimeZone = "Asia/Tokyo",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/authors/{id}", createdAuthorId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'birthDate')].message").exists())
        }

        @Test
        fun `クライアントタイムゾーンが空の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange)
            val createdAuthorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 無効なリクエスト（クライアントタイムゾーンが空）
            val invalidRequest =
                UpdateAuthorRequest(
                    name = "更新されたテスト著者",
                    birthDate = LocalDate.of(1985, 5, 15),
                    lockNo = 1,
                    // 空のクライアントタイムゾーン（バリデーションエラー）
                    clientTimeZone = "",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/authors/{id}", createdAuthorId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'clientTimeZone')].message").value("クライアントのタイムゾーンは必須です"))
        }

        @Test
        fun `存在しない著者IDを指定した場合_404ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange)
            val updateRequest =
                UpdateAuthorRequest(
                    name = "更新されたテスト著者",
                    birthDate = LocalDate.of(1985, 5, 15),
                    lockNo = 1,
                    clientTimeZone = "Asia/Tokyo",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                // 存在しない著者ID
                put("/api/authors/{id}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            )
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").exists())
        }

        @Test
        fun `lockNoが省略された場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange)
            val createdAuthorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // lockNoフィールドを省略したJSONリクエスト
            val invalidJson =
                """
                {
                    "name": "更新されたテスト著者",
                    "birthDate": "1985-05-15",
                    "clientTimeZone": "Asia/Tokyo"
                }
                """.trimIndent()

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/authors/{id}", createdAuthorId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'lockNo')].message").value("ロックナンバーは必須です"))
        }

        @Test
        fun `間違ったlockNoで更新_409ステータスとOptimisticLockExceptionが返される`() {
            // 1. 準備 (Arrange)
            val createdAuthorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 間違ったlockNoで更新リクエスト
            val updateRequest =
                UpdateAuthorRequest(
                    name = "更新されたテスト著者",
                    birthDate = LocalDate.of(1985, 5, 15),
                    // 間違ったlockNo（実際は1のはず）
                    lockNo = 999,
                    clientTimeZone = "Asia/Tokyo",
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/authors/{id}", createdAuthorId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            )
                .andExpect(status().isConflict)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("競合"))
                .andExpect(jsonPath("$.message").exists())
        }
    }

    @Nested
    inner class GetAuthorBooksTests {
        @Test
        fun `既存著者の書籍一覧取得で200とデータが返される`() {
            // 1. 準備 (Arrange)
            // テスト用の著者を作成
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // テスト用の書籍を複数作成
            val book1Id =
                createTestBook(
                    title = "テスト書籍1",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.PUBLISHED,
                    authorIds = listOf(authorId),
                )
            val book2Id =
                createTestBook(
                    title = "テスト書籍2",
                    price = BigDecimal("2000.00"),
                    currencyCode = "USD",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(authorId),
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                get("/api/authors/{id}/books", authorId),
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.author.id").value(authorId))
                .andExpect(jsonPath("$.author.name").value("テスト著者"))
                .andExpect(jsonPath("$.author.birthDate").value("1980-01-01"))
                .andExpect(jsonPath("$.books").exists())
                .andExpect(jsonPath("$.books").isArray())
                .andExpect(jsonPath("$.books.length()").value(2))
                .andExpect(jsonPath("$.books[?(@.title == 'テスト書籍1')].price").value(1500.00))
                .andExpect(jsonPath("$.books[?(@.title == 'テスト書籍1')].currencyCode").value("JPY"))
                .andExpect(jsonPath("$.books[?(@.title == 'テスト書籍1')].publicationStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.books[?(@.title == 'テスト書籍2')].price").value(2000.00))
                .andExpect(jsonPath("$.books[?(@.title == 'テスト書籍2')].currencyCode").value("USD"))
                .andExpect(jsonPath("$.books[?(@.title == 'テスト書籍2')].publicationStatus").value("UNPUBLISHED"))
        }

        @Test
        fun `著者に書籍がない場合は空リストが返される`() {
            // 1. 準備 (Arrange)
            // テスト用の著者を作成（書籍は作成しない）
            val authorId = createTestAuthor("書籍なし著者", LocalDate.of(1985, 5, 15))

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                get("/api/authors/{id}/books", authorId),
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.author.id").value(authorId))
                .andExpect(jsonPath("$.author.name").value("書籍なし著者"))
                .andExpect(jsonPath("$.author.birthDate").value("1985-05-15"))
                .andExpect(jsonPath("$.books").exists())
                .andExpect(jsonPath("$.books").isArray())
                .andExpect(jsonPath("$.books.length()").value(0))
        }

        @Test
        fun `存在しない著者IDの場合_404エラーが返される`() {
            // 1. 準備 (Arrange) - 存在しない著者ID

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                // 存在しない著者ID
                get("/api/authors/{id}/books", 999L),
            )
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").exists())
        }

        @Test
        fun `共著書籍がある場合も正しく取得される`() {
            // 1. 準備 (Arrange)
            // テスト用の著者を複数作成
            val author1Id = createTestAuthor("著者1", LocalDate.of(1980, 1, 1))
            val author2Id = createTestAuthor("著者2", LocalDate.of(1985, 5, 15))

            // 共著の書籍を作成
            val sharedBookId =
                createTestBook(
                    title = "共著書籍",
                    price = BigDecimal("3000.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.PUBLISHED,
                    authorIds = listOf(author1Id, author2Id),
                )

            // 著者1のみの書籍も作成
            val soloBookId =
                createTestBook(
                    title = "単独書籍",
                    price = BigDecimal("2500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(author1Id),
                )

            // 2. 実行 (Act) & 3. 検証 (Assert) - 著者1の書籍一覧取得
            mockMvc.perform(
                get("/api/authors/{id}/books", author1Id),
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.author.id").value(author1Id))
                .andExpect(jsonPath("$.author.name").value("著者1"))
                .andExpect(jsonPath("$.books").exists())
                .andExpect(jsonPath("$.books").isArray())
                .andExpect(jsonPath("$.books.length()").value(2))
                .andExpect(jsonPath("$.books[?(@.title == '共著書籍')].price").value(3000.00))
                .andExpect(jsonPath("$.books[?(@.title == '単独書籍')].price").value(2500.00))
        }
    }
}
