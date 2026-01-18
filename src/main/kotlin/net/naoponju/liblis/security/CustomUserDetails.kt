package net.naoponju.liblis.security

import net.naoponju.liblis.entity.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    val user: UserEntity
): UserDetails {
    val displayName: String = user.displayName

    override fun getAuthorities(): Collection<out GrantedAuthority> = listOf(SimpleGrantedAuthority(user.role))

    override fun getPassword(): String = user.passwordHash ?: throw IllegalStateException("Password hash is missing")
    override fun getUsername(): String = user.mailAddress
}