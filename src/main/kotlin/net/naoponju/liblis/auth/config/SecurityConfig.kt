package net.naoponju.liblis.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/login", permitAll)
                authorize("/css/**", permitAll) // CSSなども忘れずに
                authorize(anyRequest, authenticated)
            }
            // OAuth2のログイン設定
            oauth2Login {
                loginPage = "/login"
                defaultSuccessUrl("/", true)
            }
            // 通常のフォームログインも同じページに設定する（重要）
            formLogin {
                loginPage = "/login"
                permitAll()
            }
        }
        return http.build()
    }
}