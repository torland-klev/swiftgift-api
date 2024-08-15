package klev.db.users.apple

import kotlinx.serialization.Serializable

@Serializable
data class AppleUser(
    val userIdentifier: String,
    val givenName: String?,
    val familyName: String?,
    val email: String?,
    val authorizationCode: String,
    val identityToken: String,
)
