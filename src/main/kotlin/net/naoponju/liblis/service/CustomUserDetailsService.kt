package net.naoponju.liblis.service

import org.springframework.security.core.userdetails.User.withUsername
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userService: UserService
): UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userService.findEntityByEmail(email) ?: throw UsernameNotFoundException("User not found: $email")

        return withUsername(user.mailAddress)
            .password(user.passwordHash)
            .roles(user.role)
            .build()
    }
}