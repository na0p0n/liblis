package net.naoponju.liblis.common.security

import net.naoponju.liblis.domain.entity.UserEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.UUID

class CustomUserDetailsTest {
    @Test
    @DisplayName("hasPassword_正常系_passwordHashが存在する場合はtrueを返す")
    fun hasPasswordReturnsTrueWhenPasswordHashPresent() {
        val user = defaultUserEntity.copy(passwordHash = "hashed_password")
        val userDetails = CustomUserDetails(user)

        Assertions.assertTrue(userDetails.hasPassword)
    }

    @Test
    @DisplayName("hasPassword_正常系_passwordHashがnullの場合はfalseを返す")
    fun hasPasswordReturnsFalseWhenPasswordHashNull() {
        val user = defaultUserEntity.copy(passwordHash = null)
        val userDetails = CustomUserDetails(user)

        Assertions.assertFalse(userDetails.hasPassword)
    }

    @Test
    @DisplayName("hasPassword_正常系_passwordHashが空文字の場合はtrueを返す")
    fun hasPasswordReturnsTrueWhenPasswordHashEmptyString() {
        val user = defaultUserEntity.copy(passwordHash = "")
        val userDetails = CustomUserDetails(user)

        // empty string is not null, so hasPassword should be true
        Assertions.assertTrue(userDetails.hasPassword)
    }

    @Test
    @DisplayName("displayName_正常系_ユーザーの表示名を返す")
    fun displayNameReturnsUserDisplayName() {
        val user = defaultUserEntity.copy(displayName = "Test User Name")
        val userDetails = CustomUserDetails(user)

        Assertions.assertEquals("Test User Name", userDetails.displayName)
    }

    @Test
    @DisplayName("getUsername_正常系_ユーザーのメールアドレスを返す")
    fun getUsernameReturnsMailAddress() {
        val user = defaultUserEntity.copy(mailAddress = "user@example.com")
        val userDetails = CustomUserDetails(user)

        Assertions.assertEquals("user@example.com", userDetails.username)
    }

    @Test
    @DisplayName("getAuthorities_正常系_ユーザーのロールを権限として返す")
    fun getAuthoritiesReturnsUserRole() {
        val user = defaultUserEntity.copy(role = "USER")
        val userDetails = CustomUserDetails(user)

        val authorities = userDetails.authorities
        Assertions.assertEquals(1, authorities.size)
        Assertions.assertTrue(authorities.contains(SimpleGrantedAuthority("USER")))
    }

    @Test
    @DisplayName("getAuthorities_正常系_ADMINロールを権限として返す")
    fun getAuthoritiesReturnsAdminRole() {
        val user = defaultUserEntity.copy(role = "ADMIN")
        val userDetails = CustomUserDetails(user)

        val authorities = userDetails.authorities
        Assertions.assertEquals(1, authorities.size)
        Assertions.assertTrue(authorities.contains(SimpleGrantedAuthority("ADMIN")))
    }

    @Test
    @DisplayName("hasPassword_正常系_passwordHashが変更された後もgetterが最新値を返す")
    fun hasPasswordReflectsUserPasswordHashState() {
        val userWithPassword = defaultUserEntity.copy(passwordHash = "some_hash")
        val userWithoutPassword = defaultUserEntity.copy(passwordHash = null)

        val detailsWithPassword = CustomUserDetails(userWithPassword)
        val detailsWithoutPassword = CustomUserDetails(userWithoutPassword)

        Assertions.assertTrue(detailsWithPassword.hasPassword)
        Assertions.assertFalse(detailsWithoutPassword.hasPassword)
    }

    companion object {
        private val DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val defaultUserEntity =
            UserEntity(
                id = DEFAULT_USER_ID,
                displayName = "Default User",
                mailAddress = "default@example.com",
                passwordHash = "hashed_password",
                role = "USER",
                isDeleted = false,
            )
    }
}
