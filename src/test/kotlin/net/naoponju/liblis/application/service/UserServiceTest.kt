package net.naoponju.liblis.application.service

import io.mockk.justRun
import io.mockk.mockk
import io.mockk.neq
import io.mockk.spyk
import io.mockk.verify
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

    companion object {
        private val DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    }
}
