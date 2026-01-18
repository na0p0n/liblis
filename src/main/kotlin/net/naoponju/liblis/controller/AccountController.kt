package net.naoponju.liblis.controller

import net.naoponju.liblis.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AccountController(
    private val userService: UserService
) {
    @GetMapping("/account")
    fun showAccountSettings(
        @AuthenticationPrincipal principal: Any?,
        model: Model
    ): String {
        val email = when (principal) {
            is UserDetails -> principal.username
            is OAuth2User -> principal.attributes["email"] as? String
            else -> null
        }

        if (email == null) {
            return "redirect:/login"
        }

        val userDto = userService.findByEmail(email)
        model.addAttribute("user", userDto)

        return "account"
    }
}