package net.naoponju.liblis.domain.entity

import java.util.UUID

data class UserEntity(
    val id: UUID,
    val displayName: String,
    val mailAddress: String,
    val passwordHash: String?,
    val role: String,
    val googleId: String? = null,
    val githubId: String? = null,
    val appleId: String? = null,
    val isDeleted: Boolean,
)
