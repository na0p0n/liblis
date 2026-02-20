package net.naoponju.liblis.common.exception

import java.lang.RuntimeException

class UserAlreadyExistsException(message: String): RuntimeException(message)
