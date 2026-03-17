package net.naoponju.liblis.common.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component("jsonHelper")
class JsonHelper(
    private val objectMapper: ObjectMapper,
) {
    fun toJson(obj: Any?): String {
        return objectMapper.writeValueAsString(obj)
    }
}
