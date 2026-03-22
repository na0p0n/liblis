package net.naoponju.liblis.web.controller

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class StaticPageViewControllerTest {
    private val controller = StaticPageViewController()

    @Test
    @DisplayName("プライバシーポリシーページ表示_正常系")
    fun privacySuccess01() {
        val actual = controller.privacy()
        Assertions.assertEquals("privacy", actual)
    }

    @Test
    @DisplayName("利用規約ページ表示_正常系")
    fun termsSuccess01() {
        val actual = controller.terms()
        Assertions.assertEquals("terms", actual)
    }
}
