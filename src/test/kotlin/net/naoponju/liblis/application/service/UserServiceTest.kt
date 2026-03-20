package net.naoponju.liblis.application.service

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import net.naoponju.liblis.common.constraint.ChangePasswordResult
import net.naoponju.liblis.domain.entity.UserEntity
import net.naoponju.liblis.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class UserServiceTest {
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val passwordEncoder: PasswordEncoder = mockk(relaxed = true)

    private val userService =
        spyk(
            objToCopy =
                UserService(
                    userRepository = userRepository,
                    passwordEncoder = passwordEncoder,
                ),
        )

    @Test
    @DisplayName("アカウント削除_正常系_リポジトリのdeleteUserが呼ばれる")
    fun deleteAccountSuccess01() {
        val userId = DEFAULT_USER_ID
        justRun { userRepository.deleteUser(userId) }

        Assertions.assertDoesNotThrow {
            userService.deleteAccount(userId)
        }
        verify(exactly = 1) { userRepository.deleteUser(userId) }
    }

    @Test
    @DisplayName("アカウント削除_正常系_異なるユーザーIDで呼ばれる")
    fun deleteAccountSuccess02() {
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000099")
        justRun { userRepository.deleteUser(userId) }

        userService.deleteAccount(userId)
        verify(exactly = 1) { userRepository.deleteUser(userId) }
    }

    @Test
    @DisplayName("アカウント削除_正常系_deleteUserに正確なuserIdが渡される")
    fun deleteAccountSuccess03() {
        val userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
        justRun { userRepository.deleteUser(userId) }

        userService.deleteAccount(userId)

        // verify that the exact userId is passed - not a different one
        verify(exactly = 0) { userRepository.deleteUser(neq(userId)) }
        verify(exactly = 1) { userRepository.deleteUser(userId) }
    }

    @Test
    @DisplayName("パスワード変更_正常系_成功")
    fun changePasswordSuccess01() {
        val userId = DEFAULT_USER_ID
        val currentPassword = "OldPass1!"
        val newPassword = "NewPass1!"

        every { userRepository.findById(userId) } returns defaultUserEntity
        every { passwordEncoder.matches(currentPassword, defaultUserEntity.passwordHash) } returns true
        every { passwordEncoder.encode(newPassword) } returns "hashed_new"
        justRun { userRepository.updatePassword(userId, "hashed_new") }

        val actual = userService.changePassword(userId, currentPassword, newPassword)

        Assertions.assertEquals(ChangePasswordResult.SUCCESS, actual)
        verify(exactly = 1) { userRepository.updatePassword(userId, "hashed_new") }
    }

    @Test
    @DisplayName("パスワード変更_正常系_ユーザーが見つからない場合はNOT_SUPPORTED")
    fun changePasswordNotSupportedWhenUserNotFound() {
        val userId = DEFAULT_USER_ID

        every { userRepository.findById(userId) } returns null

        val actual = userService.changePassword(userId, "OldPass1!", "NewPass1!")

        Assertions.assertEquals(ChangePasswordResult.NOT_SUPPORTED, actual)
        verify(exactly = 0) { userRepository.updatePassword(any(), any()) }
    }

    @Test
    @DisplayName("パスワード変更_正常系_passwordHashがnullの場合はNOT_SUPPORTED")
    fun changePasswordNotSupportedWhenPasswordHashNull() {
        val userId = DEFAULT_USER_ID
        val userWithNullPassword = defaultUserEntity.copy(passwordHash = null)

        every { userRepository.findById(userId) } returns userWithNullPassword

        val actual = userService.changePassword(userId, "OldPass1!", "NewPass1!")

        Assertions.assertEquals(ChangePasswordResult.NOT_SUPPORTED, actual)
        verify(exactly = 0) { userRepository.updatePassword(any(), any()) }
    }

    @Test
    @DisplayName("パスワード変更_正常系_現在パスワードが一致しない場合はWRONG_CURRENT")
    fun changePasswordWrongCurrent() {
        val userId = DEFAULT_USER_ID
        val currentPassword = "WrongPass1!"

        every { userRepository.findById(userId) } returns defaultUserEntity
        every { passwordEncoder.matches(currentPassword, defaultUserEntity.passwordHash) } returns false

        val actual = userService.changePassword(userId, currentPassword, "NewPass1!")

        Assertions.assertEquals(ChangePasswordResult.WRONG_CURRENT, actual)
        verify(exactly = 0) { userRepository.updatePassword(any(), any()) }
    }

    @Test
    @DisplayName("パスワード変更_正常系_新パスワードが短い場合はVALIDATION_ERROR")
    fun changePasswordValidationErrorTooShort() {
        val userId = DEFAULT_USER_ID
        val currentPassword = "OldPass1!"
        val newPassword = "ab1!"

        every { userRepository.findById(userId) } returns defaultUserEntity
        every { passwordEncoder.matches(currentPassword, defaultUserEntity.passwordHash) } returns true

        val actual = userService.changePassword(userId, currentPassword, newPassword)

        Assertions.assertEquals(ChangePasswordResult.VALIDATION_ERROR, actual)
        verify(exactly = 0) { userRepository.updatePassword(any(), any()) }
    }

    @Test
    @DisplayName("パスワード変更_正常系_新パスワードが英字のみの場合はVALIDATION_ERROR")
    fun changePasswordValidationErrorLettersOnly() {
        val userId = DEFAULT_USER_ID
        val currentPassword = "OldPass1!"
        val newPassword = "abcdefgh"

        every { userRepository.findById(userId) } returns defaultUserEntity
        every { passwordEncoder.matches(currentPassword, defaultUserEntity.passwordHash) } returns true

        val actual = userService.changePassword(userId, currentPassword, newPassword)

        Assertions.assertEquals(ChangePasswordResult.VALIDATION_ERROR, actual)
        verify(exactly = 0) { userRepository.updatePassword(any(), any()) }
    }

    @Test
    @DisplayName("パスワード変更_正常系_updatePasswordに正確なハッシュが渡される")
    fun changePasswordPassesCorrectHashedPassword() {
        val userId = DEFAULT_USER_ID
        val currentPassword = "OldPass1!"
        val newPassword = "ValidPass1!"
        val hashedNew = "hashed_valid"

        every { userRepository.findById(userId) } returns defaultUserEntity
        every { passwordEncoder.matches(currentPassword, defaultUserEntity.passwordHash) } returns true
        every { passwordEncoder.encode(newPassword) } returns hashedNew
        justRun { userRepository.updatePassword(userId, hashedNew) }

        userService.changePassword(userId, currentPassword, newPassword)

        verify(exactly = 1) { userRepository.updatePassword(userId, hashedNew) }
        verify(exactly = 0) { userRepository.updatePassword(userId, neq(hashedNew)) }
    }

    companion object {
        private val DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val defaultUserEntity =
            UserEntity(
                id = DEFAULT_USER_ID,
                displayName = "Test User",
                mailAddress = "test@example.com",
                passwordHash = "hashed_old",
                role = "USER",
                isDeleted = false,
            )
    }
}
