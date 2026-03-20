package net.naoponju.liblis.web.controller

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import net.naoponju.liblis.application.dto.UserDto
import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserService
import net.naoponju.liblis.domain.entity.BookEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.ui.Model
import java.util.UUID

class BookControllerTest {
    private val bookService: BookService = mockk(relaxed = true)
    private val userService: UserService = mockk(relaxed = true)
    private val model: Model = mockk(relaxed = true)

    private val bookController =
        spyk(
            objToCopy =
                BookController(
                    bookService = bookService,
                    userService = userService,
                ),
        )

    @Test
    @DisplayName("書籍一覧表示_正常系_UserDetails認証")
    fun showBookListSuccess01() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL)
                .password("password")
                .roles("USER")
                .build()

        every { bookService.getAllBookCount() } returns 5
        every { bookService.getBookListPaged(0, 20) } returns listOf(defaultBookEntity)
        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every {
            bookService.fetchUserHavingBookIdsInBookIdList(
                DEFAULT_USER_ID,
                listOf(DEFAULT_BOOK_ID),
            )
        } returns emptyList()

        val actual = bookController.showBookList(page = 1, size = 20, userDetails = userDetails, model = model)
        Assertions.assertEquals("books/list", actual)
    }

    @Test
    @DisplayName("書籍一覧表示_正常系_OAuth2User認証")
    fun showBookListSuccess02() {
        val oauth2User: OAuth2User =
            DefaultOAuth2User(
                emptyList(),
                mapOf("email" to DEFAULT_EMAIL, "sub" to "google-sub-id"),
                "sub",
            )

        every { bookService.getAllBookCount() } returns 5
        every { bookService.getBookListPaged(0, 20) } returns listOf(defaultBookEntity)
        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every {
            bookService.fetchUserHavingBookIdsInBookIdList(
                DEFAULT_USER_ID,
                listOf(DEFAULT_BOOK_ID),
            )
        } returns emptyList()

        val actual = bookController.showBookList(page = 1, size = 20, userDetails = oauth2User, model = model)
        Assertions.assertEquals("books/list", actual)
    }

    @Test
    @DisplayName("書籍一覧表示_異常系_未認証はログインにリダイレクト")
    fun showBookListFailure01() {
        val actual = bookController.showBookList(page = 1, size = 20, userDetails = null, model = model)
        Assertions.assertEquals("redirect:/login", actual)
    }

    @Test
    @DisplayName("書籍一覧表示_異常系_無効なページ番号はリダイレクト")
    fun showBookListFailure02() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL)
                .password("password")
                .roles("USER")
                .build()

        every { bookService.getAllBookCount() } returns 20

        // page=0 is invalid
        val actual = bookController.showBookList(page = 0, size = 20, userDetails = userDetails, model = model)
        Assertions.assertEquals("redirect:/books/list?page=1", actual)
    }

    @Test
    @DisplayName("書籍一覧表示_異常系_ページ数超過はリダイレクト")
    fun showBookListFailure03() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL)
                .password("password")
                .roles("USER")
                .build()

        // 20 books with pageSize=20 → 1 page total, page=2 is out of range
        every { bookService.getAllBookCount() } returns 20

        val actual = bookController.showBookList(page = 2, size = 20, userDetails = userDetails, model = model)
        Assertions.assertEquals("redirect:/books/list?page=1", actual)
    }

    @Test
    @DisplayName("書籍一覧表示_正常系_許可外のサイズはデフォルト値を使用")
    fun showBookListSuccess03() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL)
                .password("password")
                .roles("USER")
                .build()

        // size=15 is not in ALLOWED_PAGE_SIZES, should fall back to 20
        every { bookService.getAllBookCount() } returns 5
        every { bookService.getBookListPaged(0, 20) } returns listOf(defaultBookEntity)
        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every {
            bookService.fetchUserHavingBookIdsInBookIdList(
                DEFAULT_USER_ID,
                listOf(DEFAULT_BOOK_ID),
            )
        } returns emptyList()

        val actual = bookController.showBookList(page = 1, size = 15, userDetails = userDetails, model = model)
        Assertions.assertEquals("books/list", actual)
    }

    @Test
    @DisplayName("書籍一覧表示_正常系_ユーザーIDなしでも書籍一覧は表示する")
    fun showBookListSuccess04() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL)
                .password("password")
                .roles("USER")
                .build()

        every { bookService.getAllBookCount() } returns 5
        every { bookService.getBookListPaged(0, 20) } returns listOf(defaultBookEntity)
        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto.copy(id = null)

        val actual = bookController.showBookList(page = 1, size = 20, userDetails = userDetails, model = model)
        Assertions.assertEquals("books/list", actual)
    }

    companion object {
        private const val DEFAULT_EMAIL = "test@example.com"
        private val DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val DEFAULT_BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
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
        private val defaultBookEntity =
            BookEntity(
                id = DEFAULT_BOOK_ID,
                title = "Test Book",
                author = listOf("Author"),
                publisher = "Publisher",
                publishDate = null,
                pages = 200,
                description = null,
                isbn10 = null,
                isbn13 = "9784000000001",
                listPrice = null,
                category = null,
                thumbnailUrl = null,
                registrationCount = 0,
                isSearchedNDL = false,
                ndlUrl = null,
                isSearchedGoogle = false,
                googleUrl = null,
                createdAt = null,
                updatedAt = null,
            )
    }
}
