package net.naoponju.liblis.domain.repository

import net.naoponju.liblis.domain.entity.UserEntity
import java.util.UUID

@Suppress("TooManyFunctions")
interface UserRepository {
    fun findByEmail(email: String): UserEntity?

    fun findByGoogleCredential(googleId: String): UserEntity?

    fun findByAppleCredential(appleId: String): UserEntity?

    fun findByGitHubCredential(githubId: String): UserEntity?

    fun findById(userId: UUID): UserEntity?

    fun save(user: UserEntity)

    fun addGoogleCredentialById(
        id: UUID,
        googleId: String,
    )

    fun addGithubCredentialById(
        id: UUID,
        githubId: String,
    )

    fun addAppleCredentialById(
        id: UUID,
        appleId: String,
    )

    fun clearGoogleCredentialByMailAddress(email: String)

    fun clearGithubCredentialByMailAddress(email: String)

    fun clearAppleCredentialByMailAddress(email: String)

    fun deleteUser(userId: UUID)

    fun updatePassword(
        userId: UUID,
        hashedPassword: String,
    )
}
