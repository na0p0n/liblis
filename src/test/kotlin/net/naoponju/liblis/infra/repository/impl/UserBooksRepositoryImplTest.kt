package net.naoponju.liblis.infra.repository.impl

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import net.naoponju.liblis.application.dto.UserBooksDto
import net.naoponju.liblis.domain.entity.UserBooksEntity
import net.naoponju.liblis.infra.mapper.UserBooksMapper
import org.apache.ibatis.exceptions.PersistenceException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.sql.SQLException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class UserBooksRepositoryImplTest {
    @AfterEach
    fun tearDown() {
        unmockkStatic(UUID::class)
        unmockkStatic(LocalDateTime::class)
    }

    private val userBooksMapper: UserBooksMapper = mockk()

    private val userBooksRepositoryImpl =
        spyk(
            objToCopy =
                UserBooksRepositoryImpl(
                    userBooksMapper = userBooksMapper,
                ),
        )

    @Test
    @DisplayName("ユーザー所持書籍一覧取得_正常系_書籍あり")
    fun getUserBooksListSuccess01() {
        every {
            userBooksMapper.findBooksByUserId(DEFAULT_USER_ID)
        } returns listOf(defaultUserBooksEntity)

        val actual = userBooksRepositoryImpl.getUserBooksList(DEFAULT_USER_ID)

        Assertions.assertNotNull(actual)
        Assertions.assertEquals(1, actual!!.size)
        val dto = actual[0]
        Assertions.assertEquals(DEFAULT_USER_BOOKS_ID, dto.id)
        Assertions.assertEquals(DEFAULT_USER_ID, dto.userId)
        Assertions.assertEquals(DEFAULT_BOOK_ID, dto.bookId)
        Assertions.assertEquals("OWNED", dto.status)
        Assertions.assertEquals(1500, dto.purchasePrice)
        Assertions.assertEquals(LocalDate.of(2024, 1, 1), dto.purchaseDate)
    }

    @Test
    @DisplayName("ユーザー所持書籍一覧取得_正常系_書籍なし（null）")
    fun getUserBooksListSuccess02() {
        every {
            userBooksMapper.findBooksByUserId(DEFAULT_USER_ID)
        } returns null

        val actual = userBooksRepositoryImpl.getUserBooksList(DEFAULT_USER_ID)
        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("ユーザー所持書籍一覧取得_正常系_空リスト")
    fun getUserBooksListSuccess03() {
        every {
            userBooksMapper.findBooksByUserId(DEFAULT_USER_ID)
        } returns emptyList()

        val actual = userBooksRepositoryImpl.getUserBooksList(DEFAULT_USER_ID)
        Assertions.assertNotNull(actual)
        Assertions.assertEquals(emptyList<UserBooksDto>(), actual)
    }

    @Test
    @DisplayName("最近追加された書籍ID一覧取得_正常系_書籍あり")
    fun getRecentAddedBooksSuccess01() {
        val bookId1 = UUID.fromString("00000000-0000-0000-0000-000000000010")
        val bookId2 = UUID.fromString("00000000-0000-0000-0000-000000000011")
        val expect = listOf(bookId1, bookId2)

        every { userBooksMapper.fetchRecentAddedBooks() } returns listOf(bookId1, bookId2)

        val actual = userBooksRepositoryImpl.getRecentAddedBooks()
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("最近追加された書籍ID一覧取得_正常系_書籍なし")
    fun getRecentAddedBooksSuccess02() {
        every { userBooksMapper.fetchRecentAddedBooks() } returns emptyList()

        val actual = userBooksRepositoryImpl.getRecentAddedBooks()
        Assertions.assertEquals(emptyList<UUID>(), actual)
    }

    @Test
    @DisplayName("書籍所有確認(userId×bookId)_正常系_所有している")
    fun existsByUserIdAndBookIdSuccess01() {
        every {
            userBooksMapper.existsByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns true

        val actual = userBooksRepositoryImpl.existsByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        Assertions.assertTrue(actual)
    }

    @Test
    @DisplayName("書籍所有確認(userId×bookId)_正常系_所有していない")
    fun existsByUserIdAndBookIdSuccess02() {
        every {
            userBooksMapper.existsByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns false

        val actual = userBooksRepositoryImpl.existsByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        Assertions.assertFalse(actual)
    }

    @Test
    @DisplayName("書籍所有確認(userId×userBooksId)_正常系_所有している")
    fun existsByUserIdAndUserBooksIdSuccess01() {
        every {
            userBooksMapper.existsByUserIdAndUserBooksId(DEFAULT_USER_ID, DEFAULT_USER_BOOKS_ID)
        } returns true

        val actual = userBooksRepositoryImpl.existsByUserIdAndUserBooksId(DEFAULT_USER_ID, DEFAULT_USER_BOOKS_ID)
        Assertions.assertTrue(actual)
    }

    @Test
    @DisplayName("書籍所有確認(userId×userBooksId)_正常系_所有していない")
    fun existsByUserIdAndUserBooksIdSuccess02() {
        every {
            userBooksMapper.existsByUserIdAndUserBooksId(DEFAULT_USER_ID, DEFAULT_USER_BOOKS_ID)
        } returns false

        val actual = userBooksRepositoryImpl.existsByUserIdAndUserBooksId(DEFAULT_USER_ID, DEFAULT_USER_BOOKS_ID)
        Assertions.assertFalse(actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍登録_正常系")
    fun insertUserBooksDataSuccess01() {
        val fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000099")
        mockkStatic(UUID::class)
        every { UUID.randomUUID() } returns fixedUuid

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns LocalDateTime.of(2024, 1, 1, 0, 0)

        every {
            userBooksMapper.existsByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns false

        every {
            userBooksMapper.insert(any<UserBooksEntity>())
        } returns 1

        val dto = defaultUserBooksDto.copy(id = null)
        val actual = userBooksRepositoryImpl.insertUserBooksData(dto)

        Assertions.assertEquals(fixedUuid, actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍登録_正常系_重複登録はnullを返す")
    fun insertUserBooksDataSuccess02() {
        every {
            userBooksMapper.existsByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns true

        val dto = defaultUserBooksDto.copy(id = null)
        val actual = userBooksRepositoryImpl.insertUserBooksData(dto)

        Assertions.assertNull(actual)
        verify(exactly = 0) { userBooksMapper.insert(any()) }
    }

    @Test
    @DisplayName("ユーザー書庫書籍登録_正常系_影響件数0はnullを返す")
    fun insertUserBooksDataSuccess03() {
        val fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000099")
        mockkStatic(UUID::class)
        every { UUID.randomUUID() } returns fixedUuid

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns LocalDateTime.of(2024, 1, 1, 0, 0)

        every {
            userBooksMapper.existsByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns false

        every {
            userBooksMapper.insert(any<UserBooksEntity>())
        } returns 0

        val dto = defaultUserBooksDto.copy(id = null)
        val actual = userBooksRepositoryImpl.insertUserBooksData(dto)

        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍登録_異常系_PersistenceExceptionはnullを返す")
    fun insertUserBooksDataFailure01() {
        val fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000099")
        mockkStatic(UUID::class)
        every { UUID.randomUUID() } returns fixedUuid

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns LocalDateTime.of(2024, 1, 1, 0, 0)

        every {
            userBooksMapper.existsByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns false

        every {
            userBooksMapper.insert(any<UserBooksEntity>())
        } throws PersistenceException("DB error")

        val dto = defaultUserBooksDto.copy(id = null)
        val actual = userBooksRepositoryImpl.insertUserBooksData(dto)

        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍登録_異常系_SQLExceptionはnullを返す")
    fun insertUserBooksDataFailure02() {
        val fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000099")
        mockkStatic(UUID::class)
        every { UUID.randomUUID() } returns fixedUuid

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns LocalDateTime.of(2024, 1, 1, 0, 0)

        every {
            userBooksMapper.existsByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns false

        every {
            userBooksMapper.insert(any<UserBooksEntity>())
        } throws SQLException("SQL error")

        val dto = defaultUserBooksDto.copy(id = null)
        val actual = userBooksRepositoryImpl.insertUserBooksData(dto)

        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍更新_正常系")
    fun editUserBooksDataSuccess01() {
        justRun { userBooksMapper.updateUserBooksData(defaultUserBooksDto) }

        Assertions.assertDoesNotThrow {
            userBooksRepositoryImpl.editUserBooksData(defaultUserBooksDto)
        }
        verify(exactly = 1) { userBooksMapper.updateUserBooksData(defaultUserBooksDto) }
    }

    @Test
    @DisplayName("ユーザー書庫書籍更新_異常系_PersistenceExceptionをスロー")
    fun editUserBooksDataFailure01() {
        every {
            userBooksMapper.updateUserBooksData(defaultUserBooksDto)
        } throws PersistenceException("DB error")

        Assertions.assertThrows(PersistenceException::class.java) {
            userBooksRepositoryImpl.editUserBooksData(defaultUserBooksDto)
        }
    }

    @Test
    @DisplayName("ユーザー書庫書籍更新_異常系_SQLExceptionをスロー")
    fun editUserBooksDataFailure02() {
        every {
            userBooksMapper.updateUserBooksData(defaultUserBooksDto)
        } throws SQLException("SQL error")

        Assertions.assertThrows(SQLException::class.java) {
            userBooksRepositoryImpl.editUserBooksData(defaultUserBooksDto)
        }
    }

    @Test
    @DisplayName("ユーザー書庫書籍削除_正常系")
    fun deleteUserBooksSuccess01() {
        justRun { userBooksMapper.deleteUserBooks(DEFAULT_USER_BOOKS_ID) }

        Assertions.assertDoesNotThrow {
            userBooksRepositoryImpl.deleteUserBooks(DEFAULT_USER_BOOKS_ID)
        }
        verify(exactly = 1) { userBooksMapper.deleteUserBooks(DEFAULT_USER_BOOKS_ID) }
    }

    @Test
    @DisplayName("ユーザー書庫書籍削除_異常系_PersistenceExceptionをスロー")
    fun deleteUserBooksFailure01() {
        every {
            userBooksMapper.deleteUserBooks(DEFAULT_USER_BOOKS_ID)
        } throws PersistenceException("DB error")

        Assertions.assertThrows(PersistenceException::class.java) {
            userBooksRepositoryImpl.deleteUserBooks(DEFAULT_USER_BOOKS_ID)
        }
    }

    @Test
    @DisplayName("ユーザー書庫書籍削除_異常系_SQLExceptionをスロー")
    fun deleteUserBooksFailure02() {
        every {
            userBooksMapper.deleteUserBooks(DEFAULT_USER_BOOKS_ID)
        } throws SQLException("SQL error")

        Assertions.assertThrows(SQLException::class.java) {
            userBooksRepositoryImpl.deleteUserBooks(DEFAULT_USER_BOOKS_ID)
        }
    }

    @Test
    @DisplayName("ユーザー書庫書籍件数取得_正常系")
    fun countUserBooksSuccess01() {
        every { userBooksMapper.countUserBooks(DEFAULT_USER_ID) } returns 7

        val actual = userBooksRepositoryImpl.countUserBooks(DEFAULT_USER_ID)
        Assertions.assertEquals(7, actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍件数取得_正常系_0件")
    fun countUserBooksSuccess02() {
        every { userBooksMapper.countUserBooks(DEFAULT_USER_ID) } returns 0

        val actual = userBooksRepositoryImpl.countUserBooks(DEFAULT_USER_ID)
        Assertions.assertEquals(0, actual)
    }

    @Test
    @DisplayName("ユーザー所持書籍一覧取得_正常系_複数件のEntityがDTOにマッピングされる")
    fun getUserBooksListMapsMultipleEntities() {
        val bookId2 = UUID.fromString("00000000-0000-0000-0000-000000000020")
        val userBooksId2 = UUID.fromString("00000000-0000-0000-0000-000000000030")
        val entity2 =
            UserBooksEntity(
                id = userBooksId2,
                userId = DEFAULT_USER_ID,
                bookId = bookId2,
                status = "READING",
                purchasePrice = null,
                purchaseDate = LocalDate.of(2025, 6, 1),
                isDeleted = false,
                createdAt = LocalDateTime.of(2025, 6, 1, 0, 0),
                updatedAt = LocalDateTime.of(2025, 6, 1, 0, 0),
            )

        every {
            userBooksMapper.findBooksByUserId(DEFAULT_USER_ID)
        } returns listOf(defaultUserBooksEntity, entity2)

        val actual = userBooksRepositoryImpl.getUserBooksList(DEFAULT_USER_ID)

        Assertions.assertNotNull(actual)
        Assertions.assertEquals(2, actual!!.size)
        Assertions.assertEquals(userBooksId2, actual[1].id)
        Assertions.assertEquals("READING", actual[1].status)
        Assertions.assertNull(actual[1].purchasePrice)
    }

    @Test
    @DisplayName("ユーザー書庫書籍ID取得_正常系_IDが存在する場合はUUIDを返す")
    fun fetchUserBooksIdFromUserIdAndBookIdSuccess01() {
        every {
            userBooksMapper.fetchUserBooksIdFromUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns DEFAULT_USER_BOOKS_ID

        val actual = userBooksRepositoryImpl.fetchUserBooksIdFromUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        Assertions.assertEquals(DEFAULT_USER_BOOKS_ID, actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍ID取得_正常系_IDが存在しない場合はnullを返す")
    fun fetchUserBooksIdFromUserIdAndBookIdSuccess02() {
        every {
            userBooksMapper.fetchUserBooksIdFromUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns null

        val actual = userBooksRepositoryImpl.fetchUserBooksIdFromUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("書籍詳細ページ用ユーザー書庫取得_正常系_EntityがDTOにマッピングされる")
    fun findUserBookByBookIdSuccess01() {
        every {
            userBooksMapper.findByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns defaultUserBooksEntity

        val actual = userBooksRepositoryImpl.findUserBookByBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)

        Assertions.assertNotNull(actual)
        Assertions.assertEquals(DEFAULT_USER_BOOKS_ID, actual!!.id)
        Assertions.assertEquals(DEFAULT_USER_ID, actual.userId)
        Assertions.assertEquals(DEFAULT_BOOK_ID, actual.bookId)
        Assertions.assertEquals("OWNED", actual.status)
        Assertions.assertEquals(1500, actual.purchasePrice)
        Assertions.assertEquals(LocalDate.of(2024, 1, 1), actual.purchaseDate)
    }

    @Test
    @DisplayName("書籍詳細ページ用ユーザー書庫取得_正常系_書籍が見つからない場合nullを返す")
    fun findUserBookByBookIdSuccess02() {
        every {
            userBooksMapper.findByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns null

        val actual = userBooksRepositoryImpl.findUserBookByBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)

        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("書籍詳細ページ用ユーザー書庫取得_正常系_purchasePriceがnullでもマッピングされる")
    fun findUserBookByBookIdNullPurchasePrice() {
        val entityWithNullPrice = defaultUserBooksEntity.copy(purchasePrice = null)
        every {
            userBooksMapper.findByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns entityWithNullPrice

        val actual = userBooksRepositoryImpl.findUserBookByBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)

        Assertions.assertNotNull(actual)
        Assertions.assertNull(actual!!.purchasePrice)
    }

    @Test
    @DisplayName("ユーザー書庫書籍登録_正常系_purchasePriceがnullの場合も登録できる")
    fun insertUserBooksDataNullPurchasePrice() {
        val fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000099")
        mockkStatic(UUID::class)
        every { UUID.randomUUID() } returns fixedUuid

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns LocalDateTime.of(2024, 1, 1, 0, 0)

        every {
            userBooksMapper.existsByUserIdAndBookId(DEFAULT_USER_ID, DEFAULT_BOOK_ID)
        } returns false

        every {
            userBooksMapper.insert(any<UserBooksEntity>())
        } returns 1

        val dto = defaultUserBooksDto.copy(id = null, purchasePrice = null)
        val actual = userBooksRepositoryImpl.insertUserBooksData(dto)

        Assertions.assertEquals(fixedUuid, actual)
    }

    companion object {
        private val DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val DEFAULT_BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val DEFAULT_USER_BOOKS_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val defaultUserBooksDto =
            UserBooksDto(
                id = DEFAULT_USER_BOOKS_ID,
                userId = DEFAULT_USER_ID,
                bookId = DEFAULT_BOOK_ID,
                status = "OWNED",
                purchasePrice = 1500,
                purchaseDate = LocalDate.of(2024, 1, 1),
            )
        private val defaultUserBooksEntity =
            UserBooksEntity(
                id = DEFAULT_USER_BOOKS_ID,
                userId = DEFAULT_USER_ID,
                bookId = DEFAULT_BOOK_ID,
                status = "OWNED",
                purchasePrice = 1500,
                purchaseDate = LocalDate.of(2024, 1, 1),
                isDeleted = false,
                createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
                updatedAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            )
    }
}