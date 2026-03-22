package net.naoponju.liblis.infra.repository.impl

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import net.naoponju.liblis.domain.entity.UserEntity
import net.naoponju.liblis.infra.mapper.UserMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

class UserRepositoryImplTest {
    private val userMapper: UserMapper = mockk(relaxed = true)

    private val userRepositoryImpl =
        spyk(
            objToCopy =
                UserRepositoryImpl(
                    userMapper = userMapper,
                ),
        )

    @Test
    @DisplayName("ユーザー削除_正常系_マッパーのdeleteUserByIdが呼ばれる")
    fun deleteUserSuccess01() {
        val userId = DEFAULT_USER_ID
        justRun { userMapper.deleteUserById(userId) }

        Assertions.assertDoesNotThrow {
            userRepositoryImpl.deleteUser(userId)
        }
        verify(exactly = 1) { userMapper.deleteUserById(userId) }
    }

    @Test
    @DisplayName("ユーザー削除_正常系_異なるユーザーIDで呼ばれる")
    fun deleteUserSuccess02() {
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000099")
        justRun { userMapper.deleteUserById(userId) }

        userRepositoryImpl.deleteUser(userId)
        verify(exactly = 1) { userMapper.deleteUserById(userId) }
    }

    @Test
    @DisplayName("ユーザー削除_正常系_deleteUserByIdに正確なuserIdが渡される")
    fun deleteUserSuccess03() {
        val userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
        justRun { userMapper.deleteUserById(userId) }

        userRepositoryImpl.deleteUser(userId)

        verify(exactly = 0) { userMapper.deleteUserById(neq(userId)) }
        verify(exactly = 1) { userMapper.deleteUserById(userId) }
    }

    @Test
    @DisplayName("ID検索_正常系_マッパーのfindByIdが呼ばれUserEntityを返す")
    fun findByIdSuccess01() {
        val userId = DEFAULT_USER_ID

        every { userMapper.findById(userId) } returns defaultUserEntity

        val actual = userRepositoryImpl.findById(userId)

        Assertions.assertEquals(defaultUserEntity, actual)
        verify(exactly = 1) { userMapper.findById(userId) }
    }

    @Test
    @DisplayName("ID検索_正常系_ユーザーが存在しない場合はnullを返す")
    fun findByIdSuccess02() {
        val userId = DEFAULT_USER_ID

        every { userMapper.findById(userId) } returns null

        val actual = userRepositoryImpl.findById(userId)

        Assertions.assertNull(actual)
        verify(exactly = 1) { userMapper.findById(userId) }
    }

    @Test
    @DisplayName("ID検索_正常系_findByIdに正確なuserIdが渡される")
    fun findByIdPassesCorrectUserId() {
        val userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")

        every { userMapper.findById(userId) } returns defaultUserEntity

        userRepositoryImpl.findById(userId)

        verify(exactly = 1) { userMapper.findById(userId) }
        verify(exactly = 0) { userMapper.findById(neq(userId)) }
    }

    @Test
    @DisplayName("パスワード更新_正常系_マッパーのupdatePasswordが呼ばれる")
    fun updatePasswordSuccess01() {
        val userId = DEFAULT_USER_ID
        val hashedPassword = "hashed_password_123"
        justRun { userMapper.updatePassword(userId, hashedPassword) }

        Assertions.assertDoesNotThrow {
            userRepositoryImpl.updatePassword(userId, hashedPassword)
        }
        verify(exactly = 1) { userMapper.updatePassword(userId, hashedPassword) }
    }

    @Test
    @DisplayName("パスワード更新_正常系_updatePasswordに正確なuserIdとhashedPasswordが渡される")
    fun updatePasswordPassesCorrectArguments() {
        val userId = DEFAULT_USER_ID
        val hashedPassword = "hashed_correct"
        val wrongHash = "hashed_wrong"
        justRun { userMapper.updatePassword(userId, hashedPassword) }

        userRepositoryImpl.updatePassword(userId, hashedPassword)

        verify(exactly = 1) { userMapper.updatePassword(userId, hashedPassword) }
        verify(exactly = 0) { userMapper.updatePassword(userId, wrongHash) }
    }

    @Test
    @DisplayName("パスワード更新_正常系_異なるユーザーIDで呼ばれる")
    fun updatePasswordSuccess02() {
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000099")
        val hashedPassword = "hashed_99"
        justRun { userMapper.updatePassword(userId, hashedPassword) }

        userRepositoryImpl.updatePassword(userId, hashedPassword)

        verify(exactly = 1) { userMapper.updatePassword(userId, hashedPassword) }
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
