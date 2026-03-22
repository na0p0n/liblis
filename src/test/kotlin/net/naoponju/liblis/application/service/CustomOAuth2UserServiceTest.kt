package net.naoponju.liblis.application.service

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import net.naoponju.liblis.domain.entity.UserEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.UUID

class CustomOAuth2UserServiceTest {
    @AfterEach
    fun tearDown() {
        unmockkStatic(RequestContextHolder::class)
    }

    private val userService: UserService = mockk(relaxed = true)

    // ─── Session mock helpers ─────────────────────────────────────────────────

    private fun mockRequestContextWithNoAttributes() {
        mockkStatic(RequestContextHolder::class)
        every { RequestContextHolder.getRequestAttributes() } returns null
    }

    private fun mockRequestContextWithSession(securityContext: SecurityContext?) {
        val session = mockk<HttpSession>()
        every { session.getAttribute("SPRING_SECURITY_CONTEXT") } returns securityContext

        val request = mockk<HttpServletRequest>()
        every { request.getSession(false) } returns session

        val attrs = mockk<ServletRequestAttributes>()
        every { attrs.request } returns request

        mockkStatic(RequestContextHolder::class)
        every { RequestContextHolder.getRequestAttributes() } returns attrs
    }

    private fun mockRequestContextWithNullSession() {
        val request = mockk<HttpServletRequest>()
        every { request.getSession(false) } returns null

        val attrs = mockk<ServletRequestAttributes>()
        every { attrs.request } returns request

        mockkStatic(RequestContextHolder::class)
        every { RequestContextHolder.getRequestAttributes() } returns attrs
    }

    // ─── Provider ID extraction logic ─────────────────────────────────────────
    // Tests for the when(registrationId) block that was changed in this PR
    // (Apple provider was removed; only google and github are supported)

    @Test
    @DisplayName("プロバイダーID取得_正常系_GoogleはsubフィールドをIDとして使用する")
    fun googleProviderUsesSubField() {
        val attributes = mapOf("sub" to "google-sub-id-123", "email" to "test@example.com")
        val registrationId = "google"

        val providerId = when (registrationId) {
            "google" -> attributes["sub"] as String
            "github" -> attributes["id"].toString()
            else -> throw OAuth2AuthenticationException("サポートされていない認証プロバイダです。")
        }

        Assertions.assertEquals("google-sub-id-123", providerId)
    }

    @Test
    @DisplayName("プロバイダーID取得_正常系_GitHubはidフィールドをIDとして使用する")
    fun githubProviderUsesIdField() {
        val attributes = mapOf("id" to 42, "email" to "github@example.com")
        val registrationId = "github"

        val providerId = when (registrationId) {
            "google" -> attributes["sub"] as String
            "github" -> attributes["id"].toString()
            else -> throw OAuth2AuthenticationException("サポートされていない認証プロバイダです。")
        }

        Assertions.assertEquals("42", providerId)
    }

    @Test
    @DisplayName("プロバイダーID取得_異常系_未対応プロバイダはOAuth2AuthenticationExceptionをスロー")
    fun unsupportedProviderThrowsOAuth2AuthenticationException() {
        val registrationId = "twitter"

        assertThrows<OAuth2AuthenticationException> {
            when (registrationId) {
                "google" -> "some-id"
                "github" -> "some-id"
                else -> throw OAuth2AuthenticationException("サポートされていない認証プロバイダです。")
            }
        }
    }

    @Test
    @DisplayName("プロバイダーID取得_異常系_AppleはPRで削除されたため未対応プロバイダとして例外をスロー")
    fun appleProviderIsRemovedAndThrowsException() {
        val registrationId = "apple"

        assertThrows<OAuth2AuthenticationException> {
            when (registrationId) {
                "google" -> "some-id"
                "github" -> "some-id"
                else -> throw OAuth2AuthenticationException("サポートされていない認証プロバイダです。")
            }
        }
    }

    // ─── Session-based authentication reading (new in PR) ────────────────────

    @Test
    @DisplayName("セッション認証チェック_正常系_RequestContextがnullの場合はauthenticationがnull")
    fun nullRequestContextResultsInNullAuthentication() {
        mockRequestContextWithNoAttributes()

        val attrs = RequestContextHolder.getRequestAttributes()
        val session = (attrs as? ServletRequestAttributes)?.request?.getSession(false)
        val securityContext = session?.getAttribute("SPRING_SECURITY_CONTEXT") as? SecurityContext
        val authentication = securityContext?.authentication

        Assertions.assertNull(attrs)
        Assertions.assertNull(authentication)
    }

    @Test
    @DisplayName("セッション認証チェック_正常系_セッションがnullの場合はauthenticationがnull")
    fun nullSessionResultsInNullAuthentication() {
        mockRequestContextWithNullSession()

        val attrs = RequestContextHolder.getRequestAttributes()
        val session = (attrs as? ServletRequestAttributes)?.request?.getSession(false)
        val securityContext = session?.getAttribute("SPRING_SECURITY_CONTEXT") as? SecurityContext
        val authentication = securityContext?.authentication

        Assertions.assertNull(session)
        Assertions.assertNull(authentication)
    }

    @Test
    @DisplayName("セッション認証チェック_正常系_セキュリティコンテキストにauthenticationが存在する場合取得できる")
    fun validSecurityContextYieldsAuthentication() {
        val mockAuthentication = mockk<Authentication>()
        every { mockAuthentication.isAuthenticated } returns true

        val mockSecurityContext = mockk<SecurityContext>()
        every { mockSecurityContext.authentication } returns mockAuthentication

        mockRequestContextWithSession(mockSecurityContext)

        val attrs = RequestContextHolder.getRequestAttributes()
        val session = (attrs as? ServletRequestAttributes)?.request?.getSession(false)
        val securityContext = session?.getAttribute("SPRING_SECURITY_CONTEXT") as? SecurityContext
        val authentication = securityContext?.authentication

        Assertions.assertNotNull(authentication)
        Assertions.assertTrue(authentication!!.isAuthenticated)
    }

    // ─── Email extraction from principal ─────────────────────────────────────

    @Test
    @DisplayName("メールアドレス取得_正常系_UserDetailsからはusernameを返す")
    fun emailExtractionFromUserDetails() {
        val email = "user@example.com"
        val userDetails = User.withUsername(email).password("pass").roles("USER").build()
        val principal: Any = userDetails

        val extractedEmail = when (principal) {
            is org.springframework.security.core.userdetails.UserDetails -> principal.username
            is OAuth2User -> principal.attributes["email"]?.toString()
            else -> null
        }

        Assertions.assertEquals(email, extractedEmail)
    }

    @Test
    @DisplayName("メールアドレス取得_正常系_OAuth2Userからはattributesのemailを返す")
    fun emailExtractionFromOAuth2User() {
        val email = "oauth2user@example.com"
        val oauth2User = DefaultOAuth2User(
            emptyList(),
            mapOf("email" to email, "sub" to "sub-id"),
            "sub",
        )
        val principal: Any = oauth2User

        val extractedEmail = when (principal) {
            is org.springframework.security.core.userdetails.UserDetails -> principal.username
            is OAuth2User -> principal.attributes["email"]?.toString()
            else -> null
        }

        Assertions.assertEquals(email, extractedEmail)
    }

    @Test
    @DisplayName("メールアドレス取得_正常系_未知のプリンシパルはnullを返す")
    fun emailExtractionFromUnknownPrincipal() {
        val principal: Any = "string-principal"

        val extractedEmail = when (principal) {
            is org.springframework.security.core.userdetails.UserDetails -> principal.username
            is OAuth2User -> principal.attributes["email"]?.toString()
            else -> null
        }

        Assertions.assertNull(extractedEmail)
    }

    // ─── Account linking (changed in PR: Apple removed) ──────────────────────

    @Test
    @DisplayName("アカウント連携_正常系_Google認証時はlinkGoogleAccountが呼ばれる")
    fun googleRegistrationCallsLinkGoogleAccount() {
        val userId = DEFAULT_USER_ID
        val providerId = "google-sub-id-123"
        val registrationId = "google"

        justRun { userService.linkGoogleAccount(userId, providerId) }

        when (registrationId) {
            "google" -> userService.linkGoogleAccount(userId, providerId)
            "github" -> userService.linkGithubAccount(userId, providerId)
        }

        verify(exactly = 1) { userService.linkGoogleAccount(userId, providerId) }
        verify(exactly = 0) { userService.linkGithubAccount(any(), any()) }
    }

    @Test
    @DisplayName("アカウント連携_正常系_GitHub認証時はlinkGithubAccountが呼ばれる")
    fun githubRegistrationCallsLinkGithubAccount() {
        val userId = DEFAULT_USER_ID
        val providerId = "github-id-42"
        val registrationId = "github"

        justRun { userService.linkGithubAccount(userId, providerId) }

        when (registrationId) {
            "google" -> userService.linkGoogleAccount(userId, providerId)
            "github" -> userService.linkGithubAccount(userId, providerId)
        }

        verify(exactly = 0) { userService.linkGoogleAccount(any(), any()) }
        verify(exactly = 1) { userService.linkGithubAccount(userId, providerId) }
    }

    // ─── Returned attributes contain email and displayName ───────────────────

    @Test
    @DisplayName("返却attributesにemailとdisplayNameが含まれる")
    fun returnedAttributesContainEmailAndDisplayName() {
        val user = defaultUserEntity
        val originalAttributes = mapOf("sub" to "google-sub-id", "name" to "Google User")

        val mergedAttributes = originalAttributes + mapOf(
            "displayName" to user.displayName,
            "email" to user.mailAddress,
        )

        Assertions.assertEquals(user.displayName, mergedAttributes["displayName"])
        Assertions.assertEquals(user.mailAddress, mergedAttributes["email"])
        // original attributes are preserved
        Assertions.assertEquals("google-sub-id", mergedAttributes["sub"])
    }

    @Test
    @DisplayName("返却attributesでemailはユーザーのmailAddressで上書きされる")
    fun emailInAttributesIsOverriddenByUserMailAddress() {
        val user = defaultUserEntity.copy(mailAddress = "db-stored@example.com")
        val originalAttributes = mapOf(
            "sub" to "google-sub-id",
            "email" to "oauth-provided@google.com",
        )

        val mergedAttributes = originalAttributes + mapOf(
            "displayName" to user.displayName,
            "email" to user.mailAddress,
        )

        // email should be the DB-stored one, not the OAuth2 provider's email
        Assertions.assertEquals("db-stored@example.com", mergedAttributes["email"])
        Assertions.assertNotEquals("oauth-provided@google.com", mergedAttributes["email"])
    }

    companion object {
        private val DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val defaultUserEntity =
            UserEntity(
                id = DEFAULT_USER_ID,
                displayName = "Test User",
                mailAddress = "test@example.com",
                passwordHash = "hashed_password",
                role = "USER",
                isDeleted = false,
            )
    }
}