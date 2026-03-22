package net.naoponju.liblis.web.controller

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import net.naoponju.liblis.application.dto.UserBooksDto
import net.naoponju.liblis.application.dto.UserDto
import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserBooksService
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
import java.time.LocalDate
import java.util.UUID

class UserBookViewControllerTest {
    private val userService: UserService = mockk(relaxed = true)
    private val userBooksService: UserBooksService = mockk(relaxed = true)
    private val bookService: BookService = mockk(relaxed = true)
    private val model: Model = mockk(relaxed = true)

    private val controller =
        spyk(
            objToCopy =
                UserBookViewController(
                    userService = userService,
                    userBooksService = userBooksService,
                    bookService = bookService,
                ),
        )

    @Test
    @DisplayName("書庫画面表示_正常系_UserDetails認証")
    fun showUserBooksSuccess01() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.countUserBooks(DEFAULT_USER_ID) } returns 2
        every { bookService.getHavingBooksPaged(DEFAULT_USER_ID, 0, 20) } returns listOf(defaultBookEntity)
        every { userBooksService.getUserHavingBooks(DEFAULT_USER_ID) } returns listOf(defaultUserBooksDto)

        val actual = controller.showUserBooks(
            q = null,
            type = null,
            page = 1,
            size = 20,
            userDetails = userDetails,
            model = model,
        )
        Assertions.assertEquals("books/library", actual)
    }

    @Test
    @DisplayName("書庫画面表示_正常系_OAuth2User認証")
    fun showUserBooksSuccess02() {
        val oauth2User: OAuth2User =
            DefaultOAuth2User(emptyList(), mapOf("email" to DEFAULT_EMAIL, "sub" to "sub-id"), "sub")

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.countUserBooks(DEFAULT_USER_ID) } returns 1
        every { bookService.getHavingBooksPaged(DEFAULT_USER_ID, 0, 20) } returns listOf(defaultBookEntity)
        every { userBooksService.getUserHavingBooks(DEFAULT_USER_ID) } returns listOf(defaultUserBooksDto)

        val actual = controller.showUserBooks(
            q = null,
            type = null,
            page = 1,
            size = 20,
            userDetails = oauth2User,
            model = model,
        )
        Assertions.assertEquals("books/library", actual)
    }

    @Test
    @DisplayName("書庫画面表示_異常系_未認証はログインにリダイレクト")
    fun showUserBooksFailure01() {
        val actual = controller.showUserBooks(
            q = null,
            type = null,
            page = 1,
            size = 20,
            userDetails = null,
            model = model,
        )
        Assertions.assertEquals("redirect:/login", actual)
    }

    @Test
    @DisplayName("書庫画面表示_異常系_ユーザーが見つからない場合リダイレクト")
    fun showUserBooksFailure02() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns null

        val actual = controller.showUserBooks(
            q = null,
            type = null,
            page = 1,
            size = 20,
            userDetails = userDetails,
            model = model,
        )
        Assertions.assertEquals("redirect:/login", actual)
    }

    @Test
    @DisplayName("書庫画面表示_異常系_無効なページ番号はリダイレクト")
    fun showUserBooksFailure03() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.countUserBooks(DEFAULT_USER_ID) } returns 20

        // page=0 は不正 (< 1) → リダイレクト
        val actual = controller.showUserBooks(
            q = null,
            type = null,
            page = 0,
            size = 20,
            userDetails = userDetails,
            model = model,
        )
        Assertions.assertEquals("redirect:/library?page=1", actual)
    }

    @Test
    @DisplayName("書庫画面表示_正常系_許可外のサイズはデフォルト値を使用")
    fun showUserBooksSuccess03() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        // size=30 は ALLOWED_PAGE_SIZES 外 → DEFAULT_PAGE_SIZE=20 にフォールバック
        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { userBooksService.countUserBooks(DEFAULT_USER_ID) } returns 1
        every { bookService.getHavingBooksPaged(DEFAULT_USER_ID, 0, 20) } returns listOf(defaultBookEntity)
        every { userBooksService.getUserHavingBooks(DEFAULT_USER_ID) } returns listOf(defaultUserBooksDto)

        val actual = controller.showUserBooks(
            q = null,
            type = null,
            page = 1,
            size = 30,
            userDetails = userDetails,
            model = model,
        )
        Assertions.assertEquals("books/library", actual)
    }

    @Test
    @DisplayName("書庫画面表示_正常系_書籍なしの場合は空リストでlibrary表示")
    fun showUserBooksSuccess04() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        // countUserBooks=0 → totalPages は coerceAtLeast(1) で 1 になるため page=1 は有効
        every { userBooksService.countUserBooks(DEFAULT_USER_ID) } returns 0
        every { bookService.getHavingBooksPaged(DEFAULT_USER_ID, 0, 20) } returns emptyList()
        every { userBooksService.getUserHavingBooks(DEFAULT_USER_ID) } returns emptyList()

        val actual = controller.showUserBooks(
            q = null,
            type = null,
            page = 1,
            size = 20,
            userDetails = userDetails,
            model = model,
        )
        Assertions.assertEquals("books/library", actual)
    }

    @Test
    @DisplayName("書庫画面表示_正常系_タイトル検索クエリあり_ページネーションなし全件返す")
    fun showUserBooksSuccess_searchByTitle() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { bookService.findUserBooksByTitle(DEFAULT_USER_ID, "テスト") } returns listOf(defaultBookEntity)
        every { userBooksService.getUserHavingBooks(DEFAULT_USER_ID) } returns listOf(defaultUserBooksDto)

        val actual = controller.showUserBooks(
            q = "テスト",
            type = "title",
            page = 1,
            size = 20,
            userDetails = userDetails,
            model = model,
        )
        Assertions.assertEquals("books/library", actual)
    }

    @Test
    @DisplayName("書庫画面表示_正常系_著者検索クエリあり_ページネーションなし全件返す")
    fun showUserBooksSuccess_searchByAuthor() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { bookService.findUserBooksByAuthor(DEFAULT_USER_ID, "著者名") } returns listOf(defaultBookEntity)
        every { userBooksService.getUserHavingBooks(DEFAULT_USER_ID) } returns listOf(defaultUserBooksDto)

        val actual = controller.showUserBooks(
            q = "著者名",
            type = "author",
            page = 1,
            size = 20,
            userDetails = userDetails,
            model = model,
        )
        Assertions.assertEquals("books/library", actual)
    }

    @Test
    @DisplayName("書庫画面表示_正常系_検索結果なしの場合も空リストでlibrary表示")
    fun showUserBooksSuccess_searchNoResult() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL).password("pw").roles("USER").build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { bookService.findUserBooksByTitle(DEFAULT_USER_ID, "存在しない本") } returns emptyList()
        every { userBooksService.getUserHavingBooks(DEFAULT_USER_ID) } returns emptyList()

        val actual = controller.showUserBooks(
            q = "存在しない本",
            type = "title",
            page = 1,
            size = 20,
            userDetails = userDetails,
            model = model,
        )
        Assertions.assertEquals("books/library", actual)
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
