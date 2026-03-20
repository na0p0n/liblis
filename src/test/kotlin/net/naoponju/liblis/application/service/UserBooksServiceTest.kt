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