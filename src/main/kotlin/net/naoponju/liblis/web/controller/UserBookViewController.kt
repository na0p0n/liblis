package net.naoponju.liblis.web.controller

import net.naoponju.liblis.application.service.BookService
import net.naoponju.liblis.application.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@Suppress("FunctionOnlyReturningConstant")
@RequestMapping("/library")
class UserBookViewController(
    private val userService: UserService,
    private val bookService: BookService,
) {
    @GetMapping("")
    fun showUserBooks(
        @AuthenticationPrincipal userDetails: UserDetails,
        model: Model,
    ): String {
        val userId = userService.findByEmail(userDetails.username)?.id
        val result = userId?.let { bookService.getHavingBooks(userId) }
        println(result)

        model.addAttribute("myBooks", result)
        return "books/library"
    }
}
