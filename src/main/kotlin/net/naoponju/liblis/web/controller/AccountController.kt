package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@Suppress("FunctionOnlyReturningConstant", "SwallowedException", "TooGenericExceptionCaught")
class AccountController(
    private val userService: UserService,
) {
    @GetMapping("/account")
    fun showAccountSettings(
        @AuthenticationPrincipal principal: Any?,
        model: Model,
    ): String {
        val email = getEmailFromPrincipal(principal)

        if (email == null) {
            return "redirect:/login"
        }

        val userDto = userService.findByEmail(email)
        model.addAttribute("user", userDto)

        return "account"
    }

    @PostMapping("/account/unlink/{provider}")
    fun unlinkAccount(
        @PathVariable provider: String,
        @AuthenticationPrincipal principal: Any?,
        redirectAttributes: RedirectAttributes,
    ): String {
        val email = getEmailFromPrincipal(principal) ?: return "redirect:/login"

        try {
            when (provider) {
                "google" -> userService.unLinkGoogleAccount(email)
                "github" -> userService.unLinkGithubAccount(email)
                "apple" -> userService.unLinkAppleAccount(email)
                else -> redirectAttributes.addFlashAttribute("errorMessage", "不明なプロバイダーです。")
            }
            redirectAttributes.addFlashAttribute("successMessage", "${provider.capitalize()} の連携を解除しました。")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "解除に失敗しました。")
        }

        return "redirect:/account"
    }

    private fun getEmailFromPrincipal(principal: Any?): String? {
        return when (principal) {
            is UserDetails -> principal.username
            is OAuth2User -> principal.attributes["email"]?.toString()
            else -> null
        }
    }
}
