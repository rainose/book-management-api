package com.bookmanageapp.bookmanagementapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.bookmanageapp.bookmanagementapi.dto.CreateBookRequest
import com.bookmanageapp.bookmanagementapi.dto.UpdateBookRequest
import com.bookmanageapp.bookmanagementapi.repository.AuthorRepository
import com.bookmanageapp.bookmanagementapi.repository.BookRepository
import com.bookmanageapp.bookmanagementapi.domain.NewAuthor
import com.bookmanageapp.bookmanagementapi.domain.NewBook
import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import java.time.LocalDate
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test-testcontainers")
@Transactional
class BookControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var authorRepository: AuthorRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    companion object {
        @Container
        @JvmStatic
        val postgreSQLContainer = PostgreSQLContainer("postgres:15")
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
        // テスト用の著者データを事前に挿入
        // このセットアップは各テストメソッドの前に実行される
    }

    private fun createTestAuthor(name: String, birthDate: LocalDate): Long {
        val authorId = authorRepository.create(NewAuthor(name = name, birthDate = birthDate))
        return requireNotNull(authorId)
    }

    private fun createTestBook(
        title: String,
        price: BigDecimal,
        currencyCode: String,
        publicationStatus: PublicationStatus,
        authorIds: List<Long>
    ): Long {
        val newBook = NewBook(
            title = title,
            price = price,
            currencyCode = currencyCode,
            publicationStatus = publicationStatus,
            authorIds = authorIds
        )
        val bookId = bookRepository.create(newBook)
        return requireNotNull(bookId)
    }

    @Nested
    inner class CreateBookTests {

        @Test
        fun `正常なリクエストの場合_201ステータスと作成されたIDが返される`() {
            // 1. 準備 (Arrange)
            // テスト用の著者データを作成
            val author1Id = createTestAuthor("テスト著者1", LocalDate.of(1980, 1, 1))
            val author2Id = createTestAuthor("テスト著者2", LocalDate.of(1985, 5, 15))

            val request = CreateBookRequest(
                title = "テスト書籍タイトル",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(author1Id, author2Id)
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
        }

        @Test
        fun `出版状況が出版済みの場合_201ステータスと作成されたIDが返される`() {
            // 1. 準備 (Arrange)
            // テスト用の著者データを作成
            val author1Id = createTestAuthor("テスト著者1", LocalDate.of(1980, 1, 1))
            val author2Id = createTestAuthor("テスト著者2", LocalDate.of(1985, 5, 15))

            val request = CreateBookRequest(
                title = "テスト書籍タイトル（出版済み）",
                price = BigDecimal("2000.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.PUBLISHED.code,
                authorIds = listOf(author1Id, author2Id)
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
        }

        @Test
        fun `タイトルが空文字の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（タイトルが空）
            val invalidRequest = CreateBookRequest(
                title = "", // 空のタイトル（バリデーションエラー）
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(1L)
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
        }

        @Test
        fun `価格が負の値の場合_400ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（価格が負の値）
            val invalidRequest = CreateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("-100.00"), // 負の価格（バリデーションエラー）
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(1L)
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
        }

        @Test
        fun `著者IDリストが空の場合_400ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（著者IDが空のリスト）
            val invalidRequest = CreateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = emptyList() // 空の著者IDリスト（バリデーションエラー）
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
        }

        @Test
        fun `出版状況が無効な値の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（無効な出版ステータス）
            val invalidRequest = CreateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = "02", // 無効な出版ステータス（00: UNPUBLISHED, 01: PUBLISHED のみ有効）
                authorIds = listOf(1L)
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
        }

        @Test
        fun `通貨コードが2文字の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（文字数が不正な通貨コード）
            val invalidRequest = CreateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JP", // 文字数が不正（3文字でないとエラー）
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(1L)
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
        }

        @Test
        fun `存在しない著者IDを指定した場合_404ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange) - 存在しない著者IDを指定
            val requestWithNonExistentAuthor = CreateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(999L) // 存在しない著者ID
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestWithNonExistentAuthor))
            )
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").exists())
        }

        @Test
        fun `一部の著者IDが存在しない場合_404ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange) - 存在する著者IDと存在しない著者IDを混在
            // テスト用の著者データを1つだけ作成
            val existingAuthorId = createTestAuthor("存在する著者", LocalDate.of(1980, 1, 1))

            val requestWithMixedAuthorIds = CreateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(existingAuthorId, 999L) // existingAuthorIdは存在、999Lは存在しない
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestWithMixedAuthorIds))
            )
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").exists())
        }

    }

    @Nested
    inner class UpdateBookTests {

        @Test
        fun `正常なリクエストの場合_204ステータスが返される`() {
            // 1. 準備 (Arrange)
            // テスト用の著者データを作成
            val author1Id = createTestAuthor("テスト著者1", LocalDate.of(1980, 1, 1))
            val author2Id = createTestAuthor("テスト著者2", LocalDate.of(1985, 5, 15))

            // テスト用の書籍を事前に作成（BookRepositoryを直接使用）
            val createdBookId = createTestBook(
                title = "テスト書籍タイトル",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author1Id, author2Id)
            )

            // 更新リクエストを準備
            val updateRequest = UpdateBookRequest(
                title = "更新されたテスト書籍タイトル",
                price = BigDecimal("2000.00"),
                currencyCode = "USD",
                publicationStatus = PublicationStatus.PUBLISHED.code,
                authorIds = listOf(author1Id),
                lockNo = 1
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isNoContent)
        }

        @Test
        fun `出版状況を未出版から出版済みに変更する場合_204ステータスが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 未出版の書籍を作成（BookRepositoryを直接使用）
            val createdBookId = createTestBook(
                title = "未出版書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(authorId)
            )

            // 出版済みに変更するリクエスト
            val updateRequest = UpdateBookRequest(
                title = "出版された書籍",
                price = BigDecimal("1800.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.PUBLISHED.code,
                authorIds = listOf(authorId),
                lockNo = 1
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isNoContent)
        }

        @Test
        fun `タイトルが空文字の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId = createTestBook(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(authorId)
            )

            // 無効なリクエスト（タイトルが空）
            val invalidRequest = UpdateBookRequest(
                title = "", // 空のタイトル（バリデーションエラー）
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(authorId),
                lockNo = 1
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
        }

        @Test
        fun `価格が負の値の場合_400ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId = createTestBook(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(authorId)
            )

            // 無効なリクエスト（価格が負の値）
            val invalidRequest = UpdateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("-100.00"), // 負の価格（バリデーションエラー）
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(authorId),
                lockNo = 1
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
        }

        @Test
        fun `著者IDリストが空の場合_400ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId = createTestBook(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(authorId)
            )

            // 無効なリクエスト（著者IDが空のリスト）
            val invalidRequest = UpdateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = emptyList(), // 空の著者IDリスト（バリデーションエラー）
                lockNo = 1
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
        }

        @Test
        fun `通貨コードが2文字の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId = createTestBook(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(authorId)
            )

            // 無効なリクエスト（文字数が不正な通貨コード）
            val invalidRequest = UpdateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JP", // 文字数が不正（3文字でないとエラー）
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(authorId),
                lockNo = 1
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
        }

        @Test
        fun `存在しない書籍IDを指定した場合_404ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            val updateRequest = UpdateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(authorId),
                lockNo = 1
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", 999L) // 存在しない書籍ID
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").exists())
        }

        @Test
        fun `存在しない著者IDを指定した場合_404ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId = createTestBook(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(authorId)
            )

            val updateRequest = UpdateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(999L), // 存在しない著者ID
                lockNo = 1
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").exists())
        }

        @Test
        fun `出版済みから未出版への変更の場合_400ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 出版済みの書籍を作成（BookRepositoryを直接使用）
            val createdBookId = createTestBook(
                title = "出版済み書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(authorId)
            )

            // 未出版に戻すリクエスト（無効な遷移）
            val updateRequest = UpdateBookRequest(
                title = "未出版に戻した書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code, // 無効な遷移
                authorIds = listOf(authorId),
                lockNo = 1
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").exists())
        }

        @Test
        fun `一部の著者IDが存在しない場合_404ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange)
            val existingAuthorId = createTestAuthor("存在する著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId = createTestBook(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(existingAuthorId)
            )

            val updateRequest = UpdateBookRequest(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED.code,
                authorIds = listOf(existingAuthorId, 999L), // existingAuthorIdは存在、999Lは存在しない
                lockNo = 1
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").exists())
        }

        @Test
        fun `間違ったlockNoで更新_409ステータスとOptimisticLockExceptionが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId = createTestBook(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(authorId)
            )

            // 間違ったlockNoで更新リクエスト
            val updateRequest = UpdateBookRequest(
                title = "更新されたタイトル",
                price = BigDecimal("2000.00"),
                currencyCode = "USD",
                publicationStatus = PublicationStatus.PUBLISHED.code,
                authorIds = listOf(authorId),
                lockNo = 999 // 間違ったlockNo（実際は1のはず）
            )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isConflict)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("競合"))
                .andExpect(jsonPath("$.message").exists())
        }

        @Test
        fun `lockNoが省略された場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId = createTestBook(
                title = "テスト書籍",
                price = BigDecimal("1500.00"),
                currencyCode = "JPY",
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(authorId)
            )

            // lockNoフィールドを省略したJSONリクエスト
            val invalidJson = """
                {
                    "title": "更新されたタイトル",
                    "price": 2000.00,
                    "currencyCode": "USD",
                    "publicationStatus": "PUBLISHED",
                    "authorIds": [$authorId]
                }
            """.trimIndent()

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson)
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
        }

    } // UpdateBookTests終了
}