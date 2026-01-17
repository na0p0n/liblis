package net.naoponju.liblis.entity

import java.util.UUID

data class User(
    val id: UUID?,
    val displayName: String,
    val mailAddress: String,
    val passwordHash: String,
    val role: String,
    val isDeleted: Boolean
)
