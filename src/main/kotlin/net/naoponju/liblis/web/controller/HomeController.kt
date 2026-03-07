package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
@Suppress("FunctionOnlyReturningConstant")
class HomeController(
    private val bookService: BookService,
    private val userService: UserService,
) {
    @GetMapping("/")
    fun home(
        @AuthenticationPrincipal userDetails: UserDetails,
        model: Model,
    ): String {
        val email = userDetails.username
        val userId = userService.findByEmail(email = email)?.id

        val allBookCount = bookService.getAllBookCount()
        val haveBookCount = userId?.let { bookService.getHavingBookCount(it) }

        model.addAttribute("allBookCount", allBookCount)
        model.addAttribute("haveBookCount", haveBookCount)

        return "home"
    }
}
