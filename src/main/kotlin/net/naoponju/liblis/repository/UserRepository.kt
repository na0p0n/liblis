package net.naoponju.liblis.repository

import net.naoponju.liblis.entity.UserEntity

interface UserRepository {
    fun findByEmail(email: String): UserEntity?
    fun findByGoogleCredential(googleId: String): UserEntity?
    fun findByAppleCredential(appleId: String): UserEntity?
    fun findByGitHubCredential(githubId: String): UserEntity?
    fun save(user: UserEntity)
}