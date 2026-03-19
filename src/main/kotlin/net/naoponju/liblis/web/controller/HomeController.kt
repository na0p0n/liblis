package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserBooksService
import net.naoponju.liblis.application.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
@Suppress("FunctionOnlyReturningConstant")
class HomeController(
    private val bookService: BookService,
    private val userService: UserService,
    private val userBooksService: UserBooksService,
) {
    @GetMapping("/")
    @Suppress("ReturnCount")
    fun home(
        @AuthenticationPrincipal userDetails: Any?,
        model: Model,
    ): String {
        val email =
            when (userDetails) {
                is UserDetails -> userDetails.username
                is OAuth2User -> userDetails.attributes["email"]?.toString()
                else -> null
            } ?: return "redirect:/login"

        val userId = userService.findByEmail(email = email)?.id ?: return "redirect:/login"

        val allBookCount = bookService.getAllBookCount()
        val haveBookCount = userBooksService.countUserBooks(userId)
        val recentAddedBookIds = userBooksService.getRecentAddedBooks()
        val recentAddedBooks = bookService.findBookListByBookIds(recentAddedBookIds)

        model.addAttribute("allBookCount", allBookCount)
        model.addAttribute("haveBookCount", haveBookCount)
        model.addAttribute("recentAddedBooks", recentAddedBooks)

        return "home"
    }
}
