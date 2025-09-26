package com.bookmanageapp.bookmanagementapi.controller

import com.bookmanageapp.bookmanagementapi.domain.NewAuthor
import com.bookmanageapp.bookmanagementapi.domain.NewBook
import com.bookmanageapp.bookmanagementapi.domain.PublicationStatus
import com.bookmanageapp.bookmanagementapi.dto.CreateBookRequest
import com.bookmanageapp.bookmanagementapi.dto.UpdateBookRequest
import com.bookmanageapp.bookmanagementapi.repository.AuthorRepository
import com.bookmanageapp.bookmanagementapi.repository.BookRepository
import com.bookmanageapp.jooq.tables.MBooks.Companion.M_BOOKS
import com.bookmanageapp.jooq.tables.TBookAuthors.Companion.T_BOOK_AUTHORS
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasItem
import org.jooq.DSLContext
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
class BookControllerIT {
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

    private fun createTestAuthor(
        name: String,
        birthDate: LocalDate,
    ): Long {
        return authorRepository.create(NewAuthor(name = name, birthDate = birthDate))
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
        return bookRepository.create(newBook)
    }

    private fun findBookByTitleAndAuthorIds(
        title: String,
        authorIds: List<Long>,
    ) = dslContext
        .select(M_BOOKS.asterisk())
        .from(M_BOOKS)
        .join(T_BOOK_AUTHORS).on(M_BOOKS.ID.eq(T_BOOK_AUTHORS.BOOK_ID))
        .where(M_BOOKS.TITLE.eq(title))
        .and(T_BOOK_AUTHORS.AUTHOR_ID.`in`(authorIds))
        .groupBy(M_BOOKS.ID)
        .having(org.jooq.impl.DSL.count().eq(authorIds.size))
        .fetchOne()

    private fun findBookAuthorRelations(bookId: Long) = dslContext
        .selectFrom(T_BOOK_AUTHORS)
        .where(T_BOOK_AUTHORS.BOOK_ID.eq(bookId))
        .fetch()

    @Nested
    inner class CreateBookTests {
        @Test
        fun `正常なリクエストの場合_201ステータスと作成されたIDが返される`() {
            // 1. 準備 (Arrange)
            // テスト用の著者データを作成
            val author1Id = createTestAuthor("テスト著者1", LocalDate.of(1980, 1, 1))
            val author2Id = createTestAuthor("テスト著者2", LocalDate.of(1985, 5, 15))

            val request =
                CreateBookRequest(
                    title = "テスト書籍タイトル",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    authorIds = listOf(author1Id, author2Id),
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(greaterThan(0)))

            // 4. DB検証 - titleとauthorIdsで検索して登録データを確認
            val createdBook = findBookByTitleAndAuthorIds(request.title, request.authorIds)
            assertThat(createdBook).isNotNull
            assertThat(createdBook!!.get(M_BOOKS.TITLE)).isEqualTo(request.title)
            assertThat(createdBook.get(M_BOOKS.PRICE)).isEqualTo(request.price)
            assertThat(createdBook.get(M_BOOKS.CURRENCY_CODE)).isEqualTo(request.currencyCode)
            assertThat(createdBook.get(M_BOOKS.PUBLICATION_STATUS)).isEqualTo(request.publicationStatus)
        }

        @Test
        fun `出版状況が出版済みの場合_201ステータスと作成されたIDが返される`() {
            // 1. 準備 (Arrange)
            // テスト用の著者データを作成
            val author1Id = createTestAuthor("テスト著者1", LocalDate.of(1980, 1, 1))
            val author2Id = createTestAuthor("テスト著者2", LocalDate.of(1985, 5, 15))

            val request =
                CreateBookRequest(
                    title = "テスト書籍タイトル（出版済み）",
                    price = BigDecimal("2000.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.PUBLISHED.code,
                    authorIds = listOf(author1Id, author2Id),
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(greaterThan(0)))

            // 4. DB検証 - titleとauthorIdsで検索して登録データを確認
            val createdBook = findBookByTitleAndAuthorIds(request.title, request.authorIds)
            assertThat(createdBook).isNotNull
            assertThat(createdBook!!.get(M_BOOKS.TITLE)).isEqualTo(request.title)
            assertThat(createdBook.get(M_BOOKS.PRICE)).isEqualTo(request.price)
            assertThat(createdBook.get(M_BOOKS.CURRENCY_CODE)).isEqualTo(request.currencyCode)
            assertThat(createdBook.get(M_BOOKS.PUBLICATION_STATUS)).isEqualTo(request.publicationStatus)
        }

        @Test
        fun `タイトルが空文字の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（タイトルが空）
            val invalidRequest =
                CreateBookRequest(
                    // 空のタイトル（バリデーションエラー）
                    title = "",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    authorIds = listOf(1L),
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validation_errors").exists())
                .andExpect(jsonPath("$.validation_errors").isArray())
                .andExpect(jsonPath("$.validation_errors[?(@.field == 'title')].message").value("タイトルは必須です"))
        }

        @Test
        fun `価格が負の値の場合_400ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（価格が負の値）
            val invalidRequest =
                CreateBookRequest(
                    title = "テスト書籍",
                    // 負の価格（バリデーションエラー）
                    price = BigDecimal("-100.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    authorIds = listOf(1L),
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validation_errors").exists())
                .andExpect(jsonPath("$.validation_errors").isArray())
                .andExpect(jsonPath("$.validation_errors[?(@.field == 'price')].message").value("価格は0以上で入力してください"))
        }

        @Test
        fun `著者IDリストが空の場合_400ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（著者IDが空のリスト）
            val invalidRequest =
                CreateBookRequest(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    // 空の著者IDリスト（バリデーションエラー）
                    authorIds = emptyList(),
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validation_errors").exists())
                .andExpect(jsonPath("$.validation_errors").isArray())
                .andExpect(jsonPath("$.validation_errors[?(@.field == 'authorIds')].message").value(hasItem("著者IDは必須です")))
                .andExpect(jsonPath("$.validation_errors[?(@.field == 'authorIds')].message").value(hasItem("著者は1人以上指定してください")))
        }

        @Test
        fun `出版状況が無効な値の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（無効な出版ステータス）
            val invalidRequest =
                CreateBookRequest(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    // 無効な出版ステータス（00: UNPUBLISHED, 01: PUBLISHED のみ有効）
                    publicationStatus = "02",
                    authorIds = listOf(1L),
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validation_errors").exists())
                .andExpect(jsonPath("$.validation_errors").isArray())
                .andExpect(jsonPath("$.validation_errors[?(@.field == 'publicationStatus')].message").value("出版状況の値が不正です"))
        }

        @Test
        fun `通貨コードが2文字の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange) - 無効なリクエスト（文字数が不正な通貨コード）
            val invalidRequest =
                CreateBookRequest(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    // 文字数が不正（3文字でないとエラー）
                    currencyCode = "JP",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    authorIds = listOf(1L),
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validation_errors").exists())
                .andExpect(jsonPath("$.validation_errors").isArray())
                .andExpect(jsonPath("$.validation_errors[?(@.field == 'currencyCode')].message").value("通貨コードは3文字で入力してください"))
        }

        @Test
        fun `存在しない著者IDを指定した場合_404ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange) - 存在しない著者IDを指定
            val requestWithNonExistentAuthor =
                CreateBookRequest(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    // 存在しない著者ID
                    authorIds = listOf(999L),
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestWithNonExistentAuthor)),
            )
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("見つかりません"))
                .andExpect(jsonPath("$.message").value("指定されたIDの著者が見つかりません"))
        }

        @Test
        fun `一部の著者IDが存在しない場合_404ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange) - 存在する著者IDと存在しない著者IDを混在
            // テスト用の著者データを1つだけ作成
            val existingAuthorId = createTestAuthor("存在する著者", LocalDate.of(1980, 1, 1))

            val requestWithMixedAuthorIds =
                CreateBookRequest(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    // existingAuthorIdは存在、999Lは存在しない
                    authorIds = listOf(existingAuthorId, 999L),
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestWithMixedAuthorIds)),
            )
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("見つかりません"))
                .andExpect(jsonPath("$.message").value("指定されたIDの著者が見つかりません"))
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
            val createdBookId =
                createTestBook(
                    title = "テスト書籍タイトル",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(author1Id, author2Id),
                )

            // 更新リクエストを準備
            val updateRequest =
                UpdateBookRequest(
                    title = "更新されたテスト書籍タイトル",
                    price = BigDecimal("2000.00"),
                    currencyCode = "USD",
                    publicationStatus = PublicationStatus.PUBLISHED.code,
                    authorIds = listOf(author1Id),
                    lockNo = 1,
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            )
                .andExpect(status().isNoContent)

            // 4. DB検証 - titleとauthorIdsで検索して更新データを確認
            val updatedBook = requireNotNull(findBookByTitleAndAuthorIds(updateRequest.title, updateRequest.authorIds))
            assertThat(updatedBook.get(M_BOOKS.ID)).isEqualTo(createdBookId)
            assertThat(updatedBook.get(M_BOOKS.TITLE)).isEqualTo(updateRequest.title)
            assertThat(updatedBook.get(M_BOOKS.PRICE)).isEqualTo(updateRequest.price)
            assertThat(updatedBook.get(M_BOOKS.CURRENCY_CODE)).isEqualTo(updateRequest.currencyCode)
            assertThat(updatedBook.get(M_BOOKS.PUBLICATION_STATUS)).isEqualTo(updateRequest.publicationStatus)
            assertThat(updatedBook.get(M_BOOKS.LOCK_NO)).isEqualTo(2)

            val bookAuthorRelations = findBookAuthorRelations(createdBookId)
            assertThat(bookAuthorRelations).hasSize(1)
            assertThat(bookAuthorRelations[0].get(T_BOOK_AUTHORS.AUTHOR_ID)).isEqualTo(author1Id)
        }

        @Test
        fun `出版状況を未出版から出版済みに変更する場合_204ステータスが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 未出版の書籍を作成（BookRepositoryを直接使用）
            val createdBookId =
                createTestBook(
                    title = "未出版書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(authorId),
                )

            // 出版済みに変更するリクエスト
            val updateRequest =
                UpdateBookRequest(
                    title = "出版された書籍",
                    price = BigDecimal("1800.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.PUBLISHED.code,
                    authorIds = listOf(authorId),
                    lockNo = 1,
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            )
                .andExpect(status().isNoContent)

            // 4. DB検証 - titleとauthorIdsで検索して更新データを確認
            val updatedBook = requireNotNull(findBookByTitleAndAuthorIds(updateRequest.title, updateRequest.authorIds))
            assertThat(updatedBook.get(M_BOOKS.ID)).isEqualTo(createdBookId)
            assertThat(updatedBook.get(M_BOOKS.TITLE)).isEqualTo(updateRequest.title)
            assertThat(updatedBook.get(M_BOOKS.PRICE)).isEqualTo(updateRequest.price)
            assertThat(updatedBook.get(M_BOOKS.CURRENCY_CODE)).isEqualTo(updateRequest.currencyCode)
            assertThat(updatedBook.get(M_BOOKS.PUBLICATION_STATUS)).isEqualTo(updateRequest.publicationStatus)
            assertThat(updatedBook.get(M_BOOKS.LOCK_NO)).isEqualTo(2)

            val bookAuthorRelations = findBookAuthorRelations(createdBookId)
            assertThat(bookAuthorRelations).hasSize(1)
            assertThat(bookAuthorRelations[0].get(T_BOOK_AUTHORS.AUTHOR_ID)).isEqualTo(authorId)
        }

        @Test
        fun `タイトルが空文字の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId =
                createTestBook(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(authorId),
                )

            // 無効なリクエスト（タイトルが空）
            val invalidRequest =
                UpdateBookRequest(
                    // 空のタイトル（バリデーションエラー）
                    title = "",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    authorIds = listOf(authorId),
                    lockNo = 1,
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validation_errors").exists())
                .andExpect(jsonPath("$.validation_errors").isArray())
                .andExpect(jsonPath("$.validation_errors[?(@.field == 'title')].message").value("タイトルは必須です"))
        }

        @Test
        fun `価格が負の値の場合_400ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId =
                createTestBook(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(authorId),
                )

            // 無効なリクエスト（価格が負の値）
            val invalidRequest =
                UpdateBookRequest(
                    title = "テスト書籍",
                    // 負の価格（バリデーションエラー）
                    price = BigDecimal("-100.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    authorIds = listOf(authorId),
                    lockNo = 1,
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validation_errors").exists())
                .andExpect(jsonPath("$.validation_errors").isArray())
                .andExpect(jsonPath("$.validation_errors[?(@.field == 'price')].message").value("価格は0以上で入力してください"))
        }

        @Test
        fun `著者IDリストが空の場合_400ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId =
                createTestBook(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(authorId),
                )

            // 無効なリクエスト（著者IDが空のリスト）
            val invalidRequest =
                UpdateBookRequest(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    // 空の著者IDリスト（バリデーションエラー）
                    authorIds = emptyList(),
                    lockNo = 1,
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validation_errors").exists())
                .andExpect(jsonPath("$.validation_errors").isArray())
                .andExpect(jsonPath("$.validation_errors[?(@.field == 'authorIds')].message").value(hasItem("著者IDは必須です")))
                .andExpect(jsonPath("$.validation_errors[?(@.field == 'authorIds')].message").value(hasItem("著者は1人以上指定してください")))
        }

        @Test
        fun `通貨コードが2文字の場合_400ステータスとバリデーションエラーが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId =
                createTestBook(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(authorId),
                )

            // 無効なリクエスト（文字数が不正な通貨コード）
            val invalidRequest =
                UpdateBookRequest(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    // 文字数が不正（3文字でないとエラー）
                    currencyCode = "JP",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    authorIds = listOf(authorId),
                    lockNo = 1,
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
                .andExpect(jsonPath("$.validation_errors").exists())
                .andExpect(jsonPath("$.validation_errors").isArray())
                .andExpect(jsonPath("$.validation_errors[?(@.field == 'currencyCode')].message").value("通貨コードは3文字で入力してください"))
        }

        @Test
        fun `存在しない書籍IDを指定した場合_404ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange)
            val authorId = createTestAuthor("テスト著者", LocalDate.of(1980, 1, 1))

            val updateRequest =
                UpdateBookRequest(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    authorIds = listOf(authorId),
                    lockNo = 1,
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                // 存在しない書籍ID
                put("/api/books/{id}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
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
            val createdBookId =
                createTestBook(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(authorId),
                )

            val updateRequest =
                UpdateBookRequest(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    // 存在しない著者ID
                    authorIds = listOf(999L),
                    lockNo = 1,
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
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
            val createdBookId =
                createTestBook(
                    title = "出版済み書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.PUBLISHED,
                    authorIds = listOf(authorId),
                )

            // 未出版に戻すリクエスト（無効な遷移）
            val updateRequest =
                UpdateBookRequest(
                    title = "未出版に戻した書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    // 無効な遷移
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    authorIds = listOf(authorId),
                    lockNo = 1,
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("不正なリクエスト"))
                .andExpect(jsonPath("$.message").value("出版状況を出版済みから未出版に変更することはできません"))
        }

        @Test
        fun `一部の著者IDが存在しない場合_404ステータスとエラーメッセージが返される`() {
            // 1. 準備 (Arrange)
            val existingAuthorId = createTestAuthor("存在する著者", LocalDate.of(1980, 1, 1))

            // 書籍を作成（BookRepositoryを直接使用）
            val createdBookId =
                createTestBook(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(existingAuthorId),
                )

            val updateRequest =
                UpdateBookRequest(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED.code,
                    // existingAuthorIdは存在、999Lは存在しない
                    authorIds = listOf(existingAuthorId, 999L),
                    lockNo = 1,
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
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
            val createdBookId =
                createTestBook(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(authorId),
                )

            // 間違ったlockNoで更新リクエスト
            val updateRequest =
                UpdateBookRequest(
                    title = "更新されたタイトル",
                    price = BigDecimal("2000.00"),
                    currencyCode = "USD",
                    publicationStatus = PublicationStatus.PUBLISHED.code,
                    authorIds = listOf(authorId),
                    // 間違ったlockNo（実際は1のはず）
                    lockNo = 999,
                )

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
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
            val createdBookId =
                createTestBook(
                    title = "テスト書籍",
                    price = BigDecimal("1500.00"),
                    currencyCode = "JPY",
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(authorId),
                )

            // lockNoフィールドを省略したJSONリクエスト
            val invalidJson =
                """
                {
                    "title": "更新されたタイトル",
                    "price": 2000.00,
                    "currencyCode": "USD",
                    "publicationStatus": "${PublicationStatus.PUBLISHED.code}",
                    "authorIds": [$authorId]
                }
                """.trimIndent()

            // 2. 実行 (Act) & 3. 検証 (Assert)
            mockMvc.perform(
                put("/api/books/{id}", createdBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson),
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.message").value("リクエストのバリデーションに失敗しました"))
        }
    }
}
