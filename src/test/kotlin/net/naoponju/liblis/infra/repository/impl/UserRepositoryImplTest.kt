package net.naoponju.liblis.infra.repository.impl

import io.mockk.justRun
import io.mockk.mockk
import io.mockk.neq
import io.mockk.spyk
import io.mockk.verify
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

    companion object {
        private val DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    }
}