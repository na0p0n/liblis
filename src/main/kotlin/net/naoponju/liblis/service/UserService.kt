package net.naoponju.liblis.service

import net.naoponju.liblis.dto.UserDto
import net.naoponju.liblis.dto.UserRegistrationDto
import net.naoponju.liblis.entity.UserEntity
import net.naoponju.liblis.exception.InvalidPasswordException
import net.naoponju.liblis.exception.UserAlreadyExistsException
import net.naoponju.liblis.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun registerUser(dto: UserRegistrationDto) {
        if (userRepository.findByEmail(dto.mailAddress) != null) {
            throw UserAlreadyExistsException("このメールアドレスは既に登録されています。")
        }

        validatePassword(dto.password)

        val encodedPassword = passwordEncoder.encode(dto.password)

        val user = UserEntity(
            id = UUID.randomUUID(),
            displayName = dto.displayName,
            mailAddress = dto.mailAddress,
            passwordHash = encodedPassword,
            role = "USER",
            isDeleted = false,
            googleId = null,
            githubId = null,
            appleId = null
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
    fun findEntityByAppleId(appleId: String): UserEntity? = userRepository.findByAppleCredential(appleId)

    @Transactional
    fun linkGoogleAccount(userId: UUID, googleId: String) {
        userRepository.addGoogleCredentialById(userId, googleId)
    }

    @Transactional
    fun linkGithubAccount(userId: UUID, githubId: String) {
        userRepository.addGithubCredentialById(userId, githubId)
    }

    @Transactional
    fun linkAppleAccount(userId: UUID, appleId: String) {
        userRepository.addAppleCredentialById(userId, appleId)
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
    fun unLinkAppleAccount(email: String) {
        userRepository.clearAppleCredentialByMailAddress(email)
    }

    private fun toDto(entity: UserEntity): UserDto {
        return UserDto(
            id = entity.id,
            displayName = entity.displayName,
            mailAddress = entity.mailAddress,
            role = entity.role,
            isGoogleLinked = entity.googleId != null,
            isGithubLinked = entity.githubId != null,
            isAppleLinked = entity.appleId != null
        )
    }

    private fun validatePassword(password: String) {
        if (password.length < 8) {
            throw InvalidPasswordException("パスワードは8文字以上で入力してください。")
        }
        val regex = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*(),.-?\":{}|<>]).{8,}\$")
        if (!regex.matches(password)) {
            throw InvalidPasswordException("パスワードは英字、数字、記号を含めてください。")
        }
    }
}
