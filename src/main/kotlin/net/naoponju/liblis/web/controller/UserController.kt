package net.naoponju.liblis.web.controller

import jakarta.servlet.http.HttpServletRequest
import net.naoponju.liblis.application.service.UserBooksService
import net.naoponju.liblis.application.service.UserService
import net.naoponju.liblis.common.constraint.ChangePasswordResult
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@Suppress("FunctionOnlyReturningConstant", "SwallowedException", "TooGenericExceptionCaught", "ReturnCount")
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
    private val userBooksService: UserBooksService,
) {
    @GetMapping("/info")
    fun showAccountSettings(
        @AuthenticationPrincipal userDetails: Any?,
        model: Model,
    ): String {
        val email = getEmailFromPrincipal(userDetails)

        if (email == null) {
            return "redirect:/login"
        }

        val userDto = userService.findByEmail(email) ?: "redirect:/login"
        model.addAttribute("user", userDto)
        model.addAttribute("userDetails", userDetails)

        return "/user/info"
    }

    @PostMapping("/unlink/{provider}")
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
                else -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "不明なプロバイダーです。")
                    return "redirect:/user/info"
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "${provider.capitalize()} の連携を解除しました。")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "解除に失敗しました。")
        }

        return "redirect:/user/info"
    }

    @PostMapping("/change-password")
    fun changeUserPassword(
        @RequestParam currentPassword: String,
        @RequestParam newPassword: String,
        @RequestParam confirmPassword: String,
        @AuthenticationPrincipal userDetails: Any?,
        redirectAttributes: RedirectAttributes,
    ): String {
        val email = getEmailFromPrincipal(userDetails)
            ?: run {
                // 認証情報が取得できない場合（通常は Spring Security が弾くため到達しないはず）
                redirectAttributes.addFlashAttribute("error", "認証情報が確認できませんでした。再度ログインしてください。")
                return "redirect:/login"
            }

        val userId =
            userService.findByEmail(email)?.id
                ?: run {
                    // email に対応するユーザーが DB に存在しない場合
                    redirectAttributes.addFlashAttribute("error", "ユーザーが見つかりませんでした。")
                    return "redirect:/login"
                }

        if (newPassword != confirmPassword) {
            redirectAttributes.addAttribute("passwordError", "mismatch")
            return "redirect:/user/info"
        }
        return when (userService.changePassword(userId, currentPassword, newPassword)) {
            ChangePasswordResult.SUCCESS -> {
                redirectAttributes.addAttribute("passwordChanged", true)
                "redirect:/user/info"
            }
            ChangePasswordResult.WRONG_CURRENT -> {
                redirectAttributes.addAttribute("passwordError", "wrongCurrent")
                "redirect:/user/info"
            }
            ChangePasswordResult.VALIDATION_ERROR -> {
                redirectAttributes.addAttribute("passwordError", "validation")
                "redirect:/user/info"
            }
            ChangePasswordResult.NOT_SUPPORTED -> {
                redirectAttributes.addAttribute("passwordError", "notSupported")
                "redirect:/user/info"
            }
        }
    }

    @DeleteMapping("/delete")
    fun deleteAccount(
        @AuthenticationPrincipal userDetails: Any?,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes,
    ): String {
        val email =
            when (userDetails) {
                is UserDetails -> userDetails.username
                is OAuth2User -> userDetails.attributes["email"]?.toString()
                else -> null
            } ?: run {
                // 認証情報が取得できない場合（通常は Spring Security が弾くため到達しないはず）
                redirectAttributes.addFlashAttribute("error", "認証情報が確認できませんでした。再度ログインしてください。")
                return "redirect:/login"
            }

        val userId =
            userService.findByEmail(email)?.id
                ?: run {
                    // email に対応するユーザーが DB に存在しない場合
                    redirectAttributes.addFlashAttribute("error", "ユーザーが見つかりませんでした。")
                    return "redirect:/login"
                }

        val deletedUserBooksDataCount = userBooksService.countUserBooks(userId)
        userService.deleteAccount(userId)
        request.session.invalidate()
        SecurityContextHolder.clearContext()
        redirectAttributes.addFlashAttribute("message", "アカウントを削除しました。")
        logger.info("Account deleted: userId={}, user_books deleted: {} records", userId, deletedUserBooksDataCount)

        return "redirect:/"
    }

    private fun getEmailFromPrincipal(principal: Any?): String? {
        return when (principal) {
            is UserDetails -> principal.username
            is OAuth2User -> principal.attributes["email"]?.toString()
            else -> null
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }
}
