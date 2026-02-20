package net.naoponju.liblis.web.controller.auth

import net.naoponju.liblis.application.dto.UserRegistrationDto
import net.naoponju.liblis.common.exception.InvalidPasswordException
import net.naoponju.liblis.common.exception.UserAlreadyExistsException
import net.naoponju.liblis.application.service.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class RegistrationController(
    private val userService: UserService
) {
    @GetMapping("/register")
    fun showRegistrationForm(model: Model): String {
        model.addAttribute("userRegistrationDto", UserRegistrationDto("", "", ""))

        return "auth/register"
    }

    @PostMapping("/register")
    fun registerUser(
        @ModelAttribute userRegistrationDto: UserRegistrationDto,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            userService.registerUser(userRegistrationDto)

            redirectAttributes.addFlashAttribute("successMessage", "ユーザー登録が完了しました。ログインしてください。")
            return "redirect:/login"

        } catch (e: UserAlreadyExistsException) {
            model.addAttribute("errorMessage", e.message)
        } catch (e: InvalidPasswordException) {
            model.addAttribute("errorMessage", e.message)
        } catch (e: Exception) {
            model.addAttribute("errorMessage", "予期せぬエラーが発生しました。")
        }

        return "auth/register"
    }
}
