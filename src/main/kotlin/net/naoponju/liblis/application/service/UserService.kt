package net.naoponju.liblis.application.service

import net.naoponju.liblis.application.dto.UserDto
import net.naoponju.liblis.application.dto.UserRegistrationDto
import net.naoponju.liblis.common.constraint.ChangePasswordResult
import net.naoponju.liblis.common.exception.InvalidPasswordException
import net.naoponju.liblis.common.exception.UserAlreadyExistsException
import net.naoponju.liblis.domain.entity.UserEntity
import net.naoponju.liblis.domain.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Suppress("TooManyFunctions", "ReturnCount")
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun registerUser(dto: UserRegistrationDto) {
        if (userRepository.findByEmail(dto.mailAddress) != null) {
            throw UserAlreadyExistsException("このメールアドレスは既に登録されています。")
        }

        validatePassword(dto.password)

        val encodedPassword = passwordEncoder.encode(dto.password)

        val user =
            UserEntity(
                id = UUID.randomUUID(),
                displayName = dto.displayName,
                mailAddress = dto.mailAddress,
                passwordHash = encodedPassword,
                role = "USER",
                isDeleted = false,
                googleId = null,
                githubId = null,
                appleId = null,
            )

        userRepository.save(user)
    }

    fun findByEmail(email: String): UserDto? {
        val user = userRepository.findByEmail(email)
        return user?.let { toDto(it) }
    }

    fun findEntityByEmail(email: String): UserEntity? = userRepository.findByEmail(email)

    fun findEntityByGoogleId(googleId: String): UserEntity? = userRepository.findByGoogleCredential(googleId)

    fun findEntityByGithubId(githubId: String): UserEntity? = userRepository.findByGitHubCredential(githubId)

    @Transactional
    fun linkGoogleAccount(
        userId: UUID,
        googleId: String,
    ) {
        userRepository.addGoogleCredentialById(userId, googleId)
    }

    @Transactional
    fun linkGithubAccount(
        userId: UUID,
        githubId: String,
    ) {
        userRepository.addGithubCredentialById(userId, githubId)
    }

    @Transactional
    fun unLinkGoogleAccount(email: String) {
        userRepository.clearGoogleCredentialByMailAddress(email)
    }

    @Transactional
    fun unLinkGithubAccount(email: String) {
        userRepository.clearGithubCredentialByMailAddress(email)
    }

    @Transactional
    fun deleteAccount(userId: UUID) {
        userRepository.deleteUser(userId) // user_books は CASCADE DELETE により自動削除される
    }

    @Transactional
    fun changePassword(
        userId: UUID,
        currentPassword: String,
        newPassword: String,
    ): ChangePasswordResult {
        val user = userRepository.findById(userId) ?: return ChangePasswordResult.NOT_SUPPORTED

        user.passwordHash ?: return ChangePasswordResult.NOT_SUPPORTED

        if (!passwordEncoder.matches(currentPassword, user.passwordHash)) return ChangePasswordResult.WRONG_CURRENT

        try {
            validatePassword(newPassword)
        } catch (e: InvalidPasswordException) {
            logger.info("パスワード変更API: バリデーションエラー {}", e.message)
            return ChangePasswordResult.VALIDATION_ERROR
        }

        val newHashedPassword = passwordEncoder.encode(newPassword)

        userRepository.updatePassword(userId, newHashedPassword)
        return ChangePasswordResult.SUCCESS
    }

    private fun toDto(entity: UserEntity): UserDto {
        return UserDto(
            id = entity.id,
            displayName = entity.displayName,
            mailAddress = entity.mailAddress,
            role = entity.role,
            isGoogleLinked = entity.googleId != null,
            isGithubLinked = entity.githubId != null,
            isAppleLinked = entity.appleId != null,
        )
    }

    @Suppress("MagicNumber")
    private fun validatePassword(password: String) {
        if (password.length < 8) {
            throw InvalidPasswordException("パスワードは8文字以上で入力してください。")
        }
        val regex = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*(),.-?\":{}|<>]).{8,}\$")
        if (!regex.matches(password)) {
            throw InvalidPasswordException("パスワードは英字、数字、記号を含めてください。")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserService::class.java)
    }
}
