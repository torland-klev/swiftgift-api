package klev.db.auth

import kotlinx.serialization.Serializable

@Serializable
data class EmailLogin(
    val email: String,
    val code: Int?,
)
