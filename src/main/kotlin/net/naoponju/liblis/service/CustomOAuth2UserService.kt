package net.naoponju.liblis.service

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val userService: UserService
): DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId

        val attributes = oAuth2User.attributes

        val providerId = when (registrationId) {
            "google" -> attributes["sub"] as String
            "github" -> attributes["id"].toString()
            "apple" -> attributes["sub"] as String
            else -> throw OAuth2AuthenticationException("サポートされていない認証プロバイダです。")
        }

        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication != null && authentication.isAuthenticated && authentication.principal !is String) {
            val email = when (val principal = authentication.principal) {
                is UserDetails -> principal.username
                is OAuth2User -> principal.attributes["email"]?.toString()
                else -> null
            }

            if (email != null) {
                val currentUser = userService.findEntityByEmail(email)
                if (currentUser != null) {
                    when (registrationId) {
                        "google" -> userService.linkGoogleAccount(currentUser.id, providerId)
                        "github" -> userService.linkGithubAccount(currentUser.id, providerId)
                        "apple" -> userService.linkAppleAccount(currentUser.id, providerId)
                    }

                    return oAuth2User
                }
            }
        }

        println(providerId)
        val user = when (registrationId) {
            "google" -> userService.findEntityByGoogleId(providerId)
            "github" -> userService.findEntityByGithubId(providerId)
            "apple" -> userService.findEntityByAppleId(providerId)
            else -> null
        }

        if(user == null) {
            throw OAuth2AuthenticationException("連携しているアカウントが見つかりません。")
        }

        return DefaultOAuth2User(
            listOf(SimpleGrantedAuthority(user.role)),
            attributes + ("displayName" to user.displayName),
            userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName
        )
    }
}