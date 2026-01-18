package net.naoponju.liblis.service

import net.naoponju.liblis.dto.UserDto
import net.naoponju.liblis.dto.UserRegistrationDto
import net.naoponju.liblis.entity.UserEntity
import java.util.UUID

interface UserService {
    fun findByEmail(email: String): UserDto?

    fun findEntityByEmail(email: String): UserEntity?
    fun findEntityByGoogleId(googleId: String): UserEntity?
    fun findEntityByGithubId(githubId: String): UserEntity?
    fun findEntityByAppleId(appleId: String): UserEntity?

    fun registerUser(dto: UserRegistrationDto)

    fun linkGoogleAccount(userId: UUID, googleId: String)
    fun linkGithubAccount(userId: UUID, githubId: String)
    fun linkAppleAccount(userId: UUID, appleId: String)

    fun unLinkGoogleAccount(email: String)
    fun unLinkGithubAccount(email: String)
    fun unLinkAppleAccount(email: String)
}
