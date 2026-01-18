package net.naoponju.liblis.dto

import java.util.UUID

data class UserDto(
    val id: UUID?,
    val displayName: String,
    val mailAddress: String,
    val role: String,
    val isGoogleLinked: Boolean,
    val isGithubLinked: Boolean,
    val isAppleLinked: Boolean
)