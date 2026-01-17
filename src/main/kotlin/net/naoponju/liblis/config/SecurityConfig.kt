package net.naoponju.liblis.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/login", "/css/**", "/js/**").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form.loginPage("/login")
                    .usernameParameter("email")
                    .defaultSuccessUrl("/home", true)
                    .permitAll()
            }
//            .oauth2Login { auth ->
//                auth.loginPage("/login")
//                    .defaultSuccessUrl("/home", true)
//                    .userInfoEndpoint { userInfo ->
//                        userInfo.userService(customOAuth2UserService())
//                    }
//            }
            .logout { it.logoutSuccessUrl("/login") }

        return http.build()
    }

//    @Bean
//    fun customOAuth2UserService(): OAuth2UserService<OAuth2UserRequest, OAuth2User> {
//        return CustomOAuth2UserService()
//    }
}