package net.naoponju.liblis.application.service

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import net.naoponju.liblis.application.dto.UserBooksDto
import net.naoponju.liblis.domain.repository.UserBooksRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class UserBooksServiceTest {
    private val userBooksRepository: UserBooksRepository = mockk(relaxed = true)

    private val userBooksService =
        spyk(
            objToCopy =
                UserBooksService(
                    userBooksRepository = userBooksRepository,
                ),
        )

    @Test
    @DisplayName("ユーザー所持書籍一覧取得_正常系_書籍あり")
    fun getUserHavingBooksSuccess01() {
        val userId = DEFAULT_USER_ID
        val expect = listOf(defaultUserBooksDto)

        every { userBooksRepository.getUserBooksList(userId) } returns listOf(defaultUserBooksDto)

        val actual = userBooksService.getUserHavingBooks(userId)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("ユーザー所持書籍一覧取得_正常系_nullを返す")
    fun getUserHavingBooksSuccess02() {
        val userId = DEFAULT_USER_ID

        every { userBooksRepository.getUserBooksList(userId) } returns null

        val actual = userBooksService.getUserHavingBooks(userId)
        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("最近追加された書籍ID一覧取得_正常系_書籍あり")
    fun getRecentAddedBooksSuccess01() {
        val bookId1 = UUID.fromString("00000000-0000-0000-0000-000000000010")
        val bookId2 = UUID.fromString("00000000-0000-0000-0000-000000000011")
        val expect = listOf(bookId1, bookId2)

        every { userBooksRepository.getRecentAddedBooks() } returns listOf(bookId1, bookId2)

        val actual = userBooksService.getRecentAddedBooks()
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("最近追加された書籍ID一覧取得_正常系_書籍なし")
    fun getRecentAddedBooksSuccess02() {
        every { userBooksRepository.getRecentAddedBooks() } returns emptyList()

        val actual = userBooksService.getRecentAddedBooks()
        Assertions.assertEquals(emptyList<UUID>(), actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍登録_正常系")
    fun insertUserBooksDataSuccess01() {
        val dto = defaultUserBooksDto
        val expect = DEFAULT_USER_BOOKS_ID

        every { userBooksRepository.insertUserBooksData(dto) } returns DEFAULT_USER_BOOKS_ID

        val actual = userBooksService.insertUserBooksData(dto)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍登録_正常系_重複でnullを返す")
    fun insertUserBooksDataSuccess02() {
        val dto = defaultUserBooksDto

        every { userBooksRepository.insertUserBooksData(dto) } returns null

        val actual = userBooksService.insertUserBooksData(dto)
        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍更新_正常系")
    fun updateUserBooksDataSuccess01() {
        val dto = defaultUserBooksDto
        justRun { userBooksRepository.editUserBooksData(dto) }

        userBooksService.updateUserBooksData(dto)
        verify(exactly = 1) { userBooksRepository.editUserBooksData(dto) }
    }

    @Test
    @DisplayName("ユーザー書庫書籍削除_正常系")
    fun deleteUserBooksDataSuccess01() {
        val userBooksId = DEFAULT_USER_BOOKS_ID
        justRun { userBooksRepository.deleteUserBooks(userBooksId) }

        userBooksService.deleteUserBooksData(userBooksId)
        verify(exactly = 1) { userBooksRepository.deleteUserBooks(userBooksId) }
    }

    @Test
    @DisplayName("ユーザー書庫書籍件数取得_正常系")
    fun countUserBooksSuccess01() {
        val userId = DEFAULT_USER_ID
        val expect = 5

        every { userBooksRepository.countUserBooks(userId) } returns 5

        val actual = userBooksService.countUserBooks(userId)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍件数取得_正常系_0件")
    fun countUserBooksSuccess02() {
        val userId = DEFAULT_USER_ID

        every { userBooksRepository.countUserBooks(userId) } returns 0

        val actual = userBooksService.countUserBooks(userId)
        Assertions.assertEquals(0, actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍所有確認_正常系_所有している")
    fun isOwnedByUserSuccess01() {
        val userId = DEFAULT_USER_ID
        val userBooksId = DEFAULT_USER_BOOKS_ID

        every { userBooksRepository.existsByUserIdAndUserBooksId(userId, userBooksId) } returns true

        val actual = userBooksService.isOwnedByUser(userId, userBooksId)
        Assertions.assertTrue(actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍所有確認_正常系_所有していない")
    fun isOwnedByUserSuccess02() {
        val userId = DEFAULT_USER_ID
        val userBooksId = DEFAULT_USER_BOOKS_ID

        every { userBooksRepository.existsByUserIdAndUserBooksId(userId, userBooksId) } returns false

        val actual = userBooksService.isOwnedByUser(userId, userBooksId)
        Assertions.assertFalse(actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍更新_正常系_editUserBooksDataに正確なdtoが渡される")
    fun updateUserBooksDataPassesCorrectDto() {
        val dto = defaultUserBooksDto
        justRun { userBooksRepository.editUserBooksData(dto) }

        userBooksService.updateUserBooksData(dto)

        verify(exactly = 1) { userBooksRepository.editUserBooksData(dto) }
        verify(exactly = 0) { userBooksRepository.editUserBooksData(neq(dto)) }
    }

    @Test
    @DisplayName("ユーザー書庫書籍削除_正常系_deleteUserBooksに正確なIDが渡される")
    fun deleteUserBooksDataPassesCorrectId() {
        val userBooksId = DEFAULT_USER_BOOKS_ID
        val otherId = UUID.fromString("00000000-0000-0000-0000-000000000099")
        justRun { userBooksRepository.deleteUserBooks(userBooksId) }

        userBooksService.deleteUserBooksData(userBooksId)

        verify(exactly = 1) { userBooksRepository.deleteUserBooks(userBooksId) }
        verify(exactly = 0) { userBooksRepository.deleteUserBooks(otherId) }
    }

    @Test
    @DisplayName("ユーザー所持書籍一覧取得_正常系_空リストを返す")
    fun getUserHavingBooksReturnsEmptyList() {
        val userId = DEFAULT_USER_ID

        every { userBooksRepository.getUserBooksList(userId) } returns emptyList()

        val actual = userBooksService.getUserHavingBooks(userId)
        Assertions.assertNotNull(actual)
        Assertions.assertEquals(emptyList<UserBooksDto>(), actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍ID取得_正常系_IDが存在する場合はUUIDを返す")
    fun getUserBooksIdFromUserIdAndBookIdSuccess01() {
        val userId = DEFAULT_USER_ID
        val bookId = DEFAULT_BOOK_ID
        val expect = DEFAULT_USER_BOOKS_ID

        every {
            userBooksRepository.fetchUserBooksIdFromUserIdAndBookId(userId, bookId)
        } returns DEFAULT_USER_BOOKS_ID

        val actual = userBooksService.getUserBooksIdFromUserIdAndBookId(userId, bookId)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍ID取得_正常系_IDが存在しない場合はnullを返す")
    fun getUserBooksIdFromUserIdAndBookIdSuccess02() {
        val userId = DEFAULT_USER_ID
        val bookId = DEFAULT_BOOK_ID

        every {
            userBooksRepository.fetchUserBooksIdFromUserIdAndBookId(userId, bookId)
        } returns null

        val actual = userBooksService.getUserBooksIdFromUserIdAndBookId(userId, bookId)
        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("ユーザー書庫書籍ID取得_正常系_fetchUserBooksIdFromUserIdAndBookIdに正確な引数が渡される")
    fun getUserBooksIdFromUserIdAndBookIdPassesCorrectArgs() {
        val userId = DEFAULT_USER_ID
        val bookId = DEFAULT_BOOK_ID

        every {
            userBooksRepository.fetchUserBooksIdFromUserIdAndBookId(userId, bookId)
        } returns DEFAULT_USER_BOOKS_ID

        userBooksService.getUserBooksIdFromUserIdAndBookId(userId, bookId)

        verify(exactly = 1) {
            userBooksRepository.fetchUserBooksIdFromUserIdAndBookId(userId, bookId)
        }
    }

    @Test
    @DisplayName("ユーザー書庫書籍件数取得_正常系_大きな件数")
    fun countUserBooksLargeCount() {
        val userId = DEFAULT_USER_ID
        val expect = 9999

        every { userBooksRepository.countUserBooks(userId) } returns 9999

        val actual = userBooksService.countUserBooks(userId)
        Assertions.assertEquals(expect, actual)
    }

    @Test
    @DisplayName("書籍詳細用ユーザー書庫取得_正常系_書籍が見つかる")
    fun findUserBookByBookIdSuccess01() {
        val userId = DEFAULT_USER_ID
        val bookId = DEFAULT_BOOK_ID
        val expect = defaultUserBooksDto

        every { userBooksRepository.findUserBookByBookId(userId, bookId) } returns defaultUserBooksDto

        val actual = userBooksService.findUserBookByBookId(userId, bookId)
        Assertions.assertEquals(expect, actual)
        verify(exactly = 1) { userBooksRepository.findUserBookByBookId(userId, bookId) }
    }

    @Test
    @DisplayName("書籍詳細用ユーザー書庫取得_正常系_書籍が見つからない場合nullを返す")
    fun findUserBookByBookIdSuccess02() {
        val userId = DEFAULT_USER_ID
        val bookId = DEFAULT_BOOK_ID

        every { userBooksRepository.findUserBookByBookId(userId, bookId) } returns null

        val actual = userBooksService.findUserBookByBookId(userId, bookId)
        Assertions.assertNull(actual)
    }

    @Test
    @DisplayName("書籍詳細用ユーザー書庫取得_正常系_リポジトリに正確な引数が渡される")
    fun findUserBookByBookIdPassesCorrectArgs() {
        val userId = DEFAULT_USER_ID
        val bookId = DEFAULT_BOOK_ID
        val otherId = UUID.fromString("00000000-0000-0000-0000-000000000099")

        every { userBooksRepository.findUserBookByBookId(userId, bookId) } returns defaultUserBooksDto

        userBooksService.findUserBookByBookId(userId, bookId)

        verify(exactly = 1) { userBooksRepository.findUserBookByBookId(userId, bookId) }
        verify(exactly = 0) { userBooksRepository.findUserBookByBookId(otherId, bookId) }
        verify(exactly = 0) { userBooksRepository.findUserBookByBookId(userId, otherId) }
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
    }
}