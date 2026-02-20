package net.naoponju.liblis.common.config

import net.naoponju.liblis.application.service.CustomOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form.loginPage("/login")
                    .usernameParameter("email")
                    .defaultSuccessUrl("/", true)
                    .permitAll()
            }
            .oauth2Login { auth ->
                auth
                    .loginPage("/login")
                    .defaultSuccessUrl("/", true)
                    // SNSから取得した情報を自前DBと照合するロジックを指定
                    .userInfoEndpoint { userInfo ->
                        userInfo.userService(customOAuth2UserService)
                    }
                    // 連携エラー（未連携時など）の遷移先を指定
                    .failureUrl("/login?error=oauth2_failure")
            }
            .logout { it.logoutSuccessUrl("/login") }

        return http.build()
    }
}
