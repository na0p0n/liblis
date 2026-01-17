package net.naoponju.liblis.service

import net.naoponju.liblis.mapper.UserMapper
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(private val userMapper: UserMapper): DefaultOAuth2UserService() {

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

        val user = when (registrationId) {
            "google" -> userMapper.findByGoogleCredential(providerId)
            "github" -> userMapper.findByGoogleCredential(providerId)
            "apple" -> userMapper.findByAppleCredential(providerId)
            else -> null
        }

        if(user == null) {
            throw OAuth2AuthenticationException("連携しているアカウントが見つかりません。")
        }

        return DefaultOAuth2User(
            listOf(SimpleGrantedAuthority(user.role)),
            attributes,
            userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName
        )
    }
}