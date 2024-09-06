package klev.db.users

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserSession(
    val state: String,
    val token: String,
)

@Serializable
data class UserAndSession(
    val user: User,
    val session: UserSession,
)

data class InviteData(
    val inviteId: UUID,
)
