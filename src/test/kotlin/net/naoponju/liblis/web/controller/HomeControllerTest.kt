package net.naoponju.liblis.web.controller

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import net.naoponju.liblis.application.dto.UserDto
import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserBooksService
import net.naoponju.liblis.application.service.UserService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.ui.Model
import java.util.UUID

class HomeControllerTest {
    private val bookService: BookService = mockk(relaxed = true)
    private val userService: UserService = mockk(relaxed = true)
    private val userBooksService: UserBooksService = mockk(relaxed = true)
    private val model: Model = mockk(relaxed = true)

    private val homeController =
        spyk(
            objToCopy =
                HomeController(
                    bookService = bookService,
                    userService = userService,
                    userBooksService = userBooksService,
                ),
        )

    @Test
    @DisplayName("ホーム画面表示_正常系_UserDetails認証")
    fun homeSuccess01() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL)
                .password("password")
                .roles("USER")
                .build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { bookService.getAllBookCount() } returns 100
        every { userBooksService.countUserBooks(DEFAULT_USER_ID) } returns 5

        val actual = homeController.home(userDetails, model)
        Assertions.assertEquals("home", actual)
    }

    @Test
    @DisplayName("ホーム画面表示_正常系_OAuth2User認証")
    fun homeSuccess02() {
        val oauth2User: OAuth2User =
            DefaultOAuth2User(
                emptyList(),
                mapOf("email" to DEFAULT_EMAIL, "sub" to "google-sub-id"),
                "sub",
            )

        every { userService.findByEmail(DEFAULT_EMAIL) } returns defaultUserDto
        every { bookService.getAllBookCount() } returns 50
        every { userBooksService.countUserBooks(DEFAULT_USER_ID) } returns 3

        val actual = homeController.home(oauth2User, model)
        Assertions.assertEquals("home", actual)
    }

    @Test
    @DisplayName("ホーム画面表示_異常系_未認証はログインにリダイレクト")
    fun homeFailure01() {
        val actual = homeController.home(null, model)
        Assertions.assertEquals("redirect:/login", actual)
    }

    @Test
    @DisplayName("ホーム画面表示_異常系_OAuth2Userにemailなし")
    fun homeFailure02() {
        val oauth2User: OAuth2User =
            DefaultOAuth2User(
                emptyList(),
                mapOf("sub" to "google-sub-id"),
                "sub",
            )

        val actual = homeController.home(oauth2User, model)
        Assertions.assertEquals("redirect:/login", actual)
    }

    @Test
    @DisplayName("ホーム画面表示_異常系_ユーザーが見つからない場合はリダイレクト")
    fun homeFailure03() {
        val userDetails: UserDetails =
            User.withUsername(DEFAULT_EMAIL)
                .password("password")
                .roles("USER")
                .build()

        every { userService.findByEmail(DEFAULT_EMAIL) } returns null

        val actual = homeController.home(userDetails, model)
        Assertions.assertEquals("redirect:/login", actual)
    }

    companion object {
        private const val DEFAULT_EMAIL = "test@example.com"
        private val DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
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
    }
}
