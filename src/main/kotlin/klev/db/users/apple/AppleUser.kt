package klev.db.users.apple

import kotlinx.serialization.Serializable

@Serializable
data class AppleUser(
    val userIdentifier: String,
    val givenName: String,
    val familyName: String,
    val email: String,
    val authorizationCode: String,
    val identityToken: String,
)

@Serializable
data class AppleUserDTO(
    val userIdentifier: String,
    val givenName: String?,
    val familyName: String?,
    val email: String?,
    val authorizationCode: String,
    val identityToken: String,
) {
    fun isFirstTimeLogin() = !email.isNullOrBlank()

    fun toAppUser(): AppleUser {
        requireNotNull(givenName) { "Missing required field givenName" }
        requireNotNull(familyName) { "Missing required field familyName" }
        requireNotNull(email) { "Missing required field email" }
        return AppleUser(
            userIdentifier = userIdentifier,
            givenName = givenName,
            familyName = familyName,
            email = email,
            authorizationCode = authorizationCode,
            identityToken = identityToken,
        )
    }
}
