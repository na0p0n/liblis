package net.naoponju.liblis.domain.repository

import net.naoponju.liblis.domain.entity.UserEntity
import java.util.UUID

interface UserRepository {
    fun findByEmail(email: String): UserEntity?
    fun findByGoogleCredential(googleId: String): UserEntity?
    fun findByAppleCredential(appleId: String): UserEntity?
    fun findByGitHubCredential(githubId: String): UserEntity?

    fun save(user: UserEntity)

    fun addGoogleCredentialById(id: UUID, googleId: String)
    fun addGithubCredentialById(id: UUID, githubId: String)
    fun addAppleCredentialById(id: UUID, appleId: String)

    fun clearGoogleCredentialByMailAddress(email: String)
    fun clearGithubCredentialByMailAddress(email: String)
    fun clearAppleCredentialByMailAddress(email: String)
}