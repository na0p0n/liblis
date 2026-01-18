package net.naoponju.liblis.service.impl

import net.naoponju.liblis.dto.UserDto
import net.naoponju.liblis.entity.UserEntity
import net.naoponju.liblis.repository.UserRepository
import net.naoponju.liblis.service.UserService

class UserServiceImpl(
    private val userRepository: UserRepository
): UserService {
    override fun findByEmail(email: String): UserDto? {
        val user = userRepository.findByEmail(email)
        return user?.let { toDto(it) }
    }

    override fun findEntityByEmail(email: String): UserEntity? = userRepository.findByEmail(email)
    override fun findEntityByGoogleId(googleId: String): UserEntity? = userRepository.findByGoogleCredential(googleId)
    override fun findEntityByGithubId(githubId: String): UserEntity? = userRepository.findByGitHubCredential(githubId)
    override fun findEntityByAppleId(appleId: String): UserEntity? = userRepository.findByAppleCredential(appleId)

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
}