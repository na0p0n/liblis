package net.naoponju.liblis.web.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Suppress("FunctionOnlyReturningConstant")
@Controller
class StaticPageViewController {
    @GetMapping("/privacy")
    fun privacy(): String = "privacy"

    @GetMapping("/terms")
    fun terms(): String = "terms"
}
