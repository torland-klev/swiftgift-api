package klev.db.users

import kotlinx.serialization.Serializable

@Serializable
data class PartialUser(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
)
