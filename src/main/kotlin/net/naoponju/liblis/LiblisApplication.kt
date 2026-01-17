package net.naoponju.liblis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LiblisApplication

fun main(args: Array<String>) {
	runApplication<LiblisApplication>(*args)
}
