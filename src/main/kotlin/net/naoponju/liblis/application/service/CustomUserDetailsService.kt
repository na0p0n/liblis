package net.naoponju.liblis.application.service

import net.naoponju.liblis.common.security.CustomUserDetails
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userService: UserService,
) : UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userService.findEntityByEmail(email) ?: throw UsernameNotFoundException("User not found: $email")

        return CustomUserDetails(user)
    }
}
