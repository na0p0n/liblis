package net.naoponju.liblis.common.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class JsonHelperTest {
    private val objectMapper = ObjectMapper()
    private val jsonHelper = JsonHelper(objectMapper)

    @Test
    @DisplayName("JSON変換_正常系_単純なオブジェクト")
    fun toJsonSuccess01() {
        val obj = mapOf("key" to "value")
        val actual = jsonHelper.toJson(obj)
        Assertions.assertEquals("""{"key":"value"}""", actual)
    }

    @Test
    @DisplayName("JSON変換_正常系_null値")
    fun toJsonSuccess02() {
        val actual = jsonHelper.toJson(null)
        Assertions.assertEquals("null", actual)
    }

    @Test
    @DisplayName("JSON変換_正常系_リスト")
    fun toJsonSuccess03() {
        val obj = listOf(1, 2, 3)
        val actual = jsonHelper.toJson(obj)
        Assertions.assertEquals("[1,2,3]", actual)
    }

    @Test
    @DisplayName("JSON変換_正常系_空のマップ")
    fun toJsonSuccess04() {
        val obj = emptyMap<String, Any>()
        val actual = jsonHelper.toJson(obj)
        Assertions.assertEquals("{}", actual)
    }

    @Test
    @DisplayName("JSON変換_正常系_ネストされたオブジェクト")
    fun toJsonSuccess05() {
        data class Inner(val x: Int)

        data class Outer(val inner: Inner)

        val obj = Outer(Inner(42))
        val actual = jsonHelper.toJson(obj)
        Assertions.assertEquals("""{"inner":{"x":42}}""", actual)
    }

    @Test
    @DisplayName("JSON変換_正常系_文字列値")
    fun toJsonSuccess06() {
        val actual = jsonHelper.toJson("hello world")
        Assertions.assertEquals(""""hello world"""", actual)
    }

    @Test
    @DisplayName("JSON変換_正常系_数値")
    fun toJsonSuccess07() {
        val actual = jsonHelper.toJson(42)
        Assertions.assertEquals("42", actual)
    }

    @Test
    @DisplayName("JSON変換_正常系_booleanのtrue")
    fun toJsonSuccess08() {
        val actual = jsonHelper.toJson(true)
        Assertions.assertEquals("true", actual)
    }
}
