package net.naoponju.liblis.web.controller.api

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import net.naoponju.liblis.application.dto.UserBooksDto
import net.naoponju.liblis.application.dto.UserBooksForm
import net.naoponju.liblis.application.dto.UserBooksUpdateForm
import net.naoponju.liblis.application.dto.UserDto
import net.naoponju.liblis.application.service.UserBooksService
import net.naoponju.liblis.application.service.UserService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.LocalDate
import java.util.UUID

class UserBooksRestControllerTest {
    private val userBooksService: UserBooksService = mockk(relaxed = true)
    private val userService: UserService = mockk(relaxed = true)

    private val controller =
        spyk(
            objToCopy =
                UserBooksRestController(
                    userBooksService = userBooksService,
                    userService = userService,
                ),
        )

    // ===== getUserBookList =====

    @Test
    @DisplayName("ユーザー書庫一覧取得API_正常系_UserDetails認証")
    fun getUserBookListSuccess01() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.getUserHavingBooks(DEFAULT_USER_ID) } returns listOf(defaultUserBooksDto)

        val response = controller.getUserBookList(userDetails)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(listOf(defaultUserBooksDto), response.body)
    }

    @Test
    @DisplayName("ユーザー書庫一覧取得API_正常系_OAuth2User認証")
    fun getUserBookListSuccess02() {
        val oauth2User: OAuth2User =
            DefaultOAuth2User(emptyList(), mapOf("email" to DEFAULT_EMAIL, "sub" to "sub-id"), "sub")

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.getUserHavingBooks(DEFAULT_USER_ID) } returns listOf(defaultUserBooksDto)

        val response = controller.getUserBookList(oauth2User)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    @DisplayName("ユーザー書庫一覧取得API_異常系_未認証は401")
    fun getUserBookListFailure01() {
        val response = controller.getUserBookList(null)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    @DisplayName("ユーザー書庫一覧取得API_異常系_ユーザーが見つからない場合401")
    fun getUserBookListFailure02() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns null

        val response = controller.getUserBookList(userDetails)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    // ===== addUserBooks =====

    @Test
    @DisplayName("ユーザー書庫書籍追加API_正常系")
    fun addUserBooksSuccess01() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()
        val form =
            UserBooksForm(
                bookId = DEFAULT_BOOK_ID,
                status = "OWNED",
                purchasePrice = 1500,
                purchaseYear = 2024,
                purchaseMonth = 1,
                purchaseDay = 15,
            )

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.insertUserBooksData(any()) } returns DEFAULT_USER_BOOKS_ID

        val response = controller.addUserBooks(userDetails, form)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.OK, response!!.statusCode)
        Assertions.assertEquals(DEFAULT_USER_BOOKS_ID, response.body)
    }

    @Test
    @DisplayName("ユーザー書庫書籍追加API_異常系_未認証は401")
    fun addUserBooksFailure01() {
        val form =
            UserBooksForm(
                bookId = DEFAULT_BOOK_ID,
                status = "OWNED",
                purchasePrice = null,
                purchaseYear = 2024,
                purchaseMonth = 1,
                purchaseDay = 1,
            )

        val response = controller.addUserBooks(null, form)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response!!.statusCode)
    }

    @Test
    @DisplayName("ユーザー書庫書籍追加API_異常系_購入日が不完全の場合400")
    fun addUserBooksFailure02() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()
        val form =
            UserBooksForm(
                bookId = DEFAULT_BOOK_ID,
                status = "OWNED",
                purchasePrice = null,
                purchaseYear = null,
                purchaseMonth = null,
                purchaseDay = null,
            )

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto

        val response = controller.addUserBooks(userDetails, form)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response!!.statusCode)
    }

    @Test
    @DisplayName("ユーザー書庫書籍追加API_異常系_無効な日付は400")
    fun addUserBooksFailure03() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()
        val form =
            UserBooksForm(
                bookId = DEFAULT_BOOK_ID,
                status = "OWNED",
                purchasePrice = null,
                purchaseYear = 2024,
                purchaseMonth = 13, // invalid month
                purchaseDay = 1,
            )

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto

        val response = controller.addUserBooks(userDetails, form)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response!!.statusCode)
    }

    @Test
    @DisplayName("ユーザー書庫書籍追加API_異常系_重複登録は400")
    fun addUserBooksFailure04() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()
        val form =
            UserBooksForm(
                bookId = DEFAULT_BOOK_ID,
                status = "OWNED",
                purchasePrice = null,
                purchaseYear = 2024,
                purchaseMonth = 1,
                purchaseDay = 1,
            )

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.insertUserBooksData(any()) } returns null

        val response = controller.addUserBooks(userDetails, form)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response!!.statusCode)
    }

    // ===== updateUserBooks =====

    @Test
    @DisplayName("ユーザー書庫書籍更新API_正常系")
    fun updateUserBooksSuccess01() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()
        val form =
            UserBooksUpdateForm(
                bookId = DEFAULT_BOOK_ID,
                status = "OWNED",
                purchasePrice = 2000,
                purchaseDate = LocalDate.of(2024, 6, 1),
            )

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.isOwnedByUser(DEFAULT_USER_ID, DEFAULT_USER_BOOKS_ID) } returns true
        justRun { userBooksService.updateUserBooksData(any()) }

        val response = controller.updateUserBooks(userDetails, DEFAULT_USER_BOOKS_ID, form)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    @DisplayName("ユーザー書庫書籍更新API_異常系_未認証は401")
    fun updateUserBooksFailure01() {
        val form =
            UserBooksUpdateForm(
                bookId = DEFAULT_BOOK_ID,
                status = "OWNED",
                purchasePrice = null,
                purchaseDate = LocalDate.of(2024, 1, 1),
            )

        val response = controller.updateUserBooks(null, DEFAULT_USER_BOOKS_ID, form)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    @DisplayName("ユーザー書庫書籍更新API_異常系_所有していない場合403")
    fun updateUserBooksFailure02() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()
        val form =
            UserBooksUpdateForm(
                bookId = DEFAULT_BOOK_ID,
                status = "OWNED",
                purchasePrice = null,
                purchaseDate = LocalDate.of(2024, 1, 1),
            )

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.isOwnedByUser(DEFAULT_USER_ID, DEFAULT_USER_BOOKS_ID) } returns false

        val response = controller.updateUserBooks(userDetails, DEFAULT_USER_BOOKS_ID, form)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        verify(exactly = 0) { userBooksService.updateUserBooksData(any()) }
    }

    // ===== deleteUserBooks =====

    @Test
    @DisplayName("ユーザー書庫書籍削除API_正常系")
    fun deleteUserBooksSuccess01() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.isOwnedByUser(DEFAULT_USER_ID, DEFAULT_USER_BOOKS_ID) } returns true
        justRun { userBooksService.deleteUserBooksData(DEFAULT_USER_BOOKS_ID) }

        val response = controller.deleteUserBooks(userDetails, DEFAULT_USER_BOOKS_ID)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    @DisplayName("ユーザー書庫書籍削除API_異常系_未認証は401")
    fun deleteUserBooksFailure01() {
        val response = controller.deleteUserBooks(null, DEFAULT_USER_BOOKS_ID)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    @DisplayName("ユーザー書庫書籍削除API_異常系_所有していない場合403")
    fun deleteUserBooksFailure02() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.isOwnedByUser(DEFAULT_USER_ID, DEFAULT_USER_BOOKS_ID) } returns false

        val response = controller.deleteUserBooks(userDetails, DEFAULT_USER_BOOKS_ID)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        verify(exactly = 0) { userBooksService.deleteUserBooksData(any()) }
    }

    @Test
    @DisplayName("ユーザー書庫書籍削除API_異常系_OAuth2User認証でユーザー見つからない場合401")
    fun deleteUserBooksFailure03() {
        val oauth2User: OAuth2User =
            DefaultOAuth2User(emptyList(), mapOf("email" to DEFAULT_EMAIL, "sub" to "sub-id"), "sub")

        every { userService.findByEmail(DEFAULT_EMAIL) } returns null

        val response = controller.deleteUserBooks(oauth2User, DEFAULT_USER_BOOKS_ID)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    @DisplayName("最近追加された書籍一覧取得API_正常系_認証なしでも取得可能")
    fun getRecentAddedBookListSuccess01() {
        val bookId1 = UUID.fromString("00000000-0000-0000-0000-000000000010")
        val bookId2 = UUID.fromString("00000000-0000-0000-0000-000000000011")
        val bookIds = listOf(bookId1, bookId2)

        every { userBooksService.getRecentAddedBooks() } returns bookIds

        val response = controller.getRecentAddedBookList()
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(bookIds, response.body)
    }

    @Test
    @DisplayName("最近追加された書籍一覧取得API_正常系_書籍なしは空リストを返す")
    fun getRecentAddedBookListSuccess02() {
        every { userBooksService.getRecentAddedBooks() } returns emptyList()

        val response = controller.getRecentAddedBookList()
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(emptyList<UUID>(), response.body)
    }

    @Test
    @DisplayName("ユーザー書庫書籍削除API_異常系_未認証は401")
    fun deleteUserBooksUnauth() {
        val response = controller.deleteUserBooks(null, DEFAULT_USER_BOOKS_ID)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        verify(exactly = 0) { userBooksService.deleteUserBooksData(any()) }
    }

    companion object {
        private const val DEFAULT_EMAIL = "test@example.com"
        private val DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val DEFAULT_BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val DEFAULT_USER_BOOKS_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val defaultUserDto =
            UserDto(
                id = DEFAULT_USER_ID,
                displayName = "TestUser",
                mailAddress = DEFAULT_EMAIL,
                role = "USER",
                isGoogleLinked = false,
                isGithubLinked = false,
                isAppleLinked = false,
            )
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
