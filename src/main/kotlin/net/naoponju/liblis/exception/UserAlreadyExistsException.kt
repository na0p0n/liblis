package net.naoponju.liblis.exception

import java.lang.RuntimeException

class UserAlreadyExistsException(message: String): RuntimeException(message) {
}