package net.naoponju.liblis.web.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/books")
class BookController {
    @GetMapping("/list")
    fun showBookList(): String {
        return "books/list"
    }

    @GetMapping("/register")
    fun showBookRegister(): String {
        return "books/register"
    }
}