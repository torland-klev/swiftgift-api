package klev.db.users

import klev.plugins.UUIDSerializer
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

@Serializable
data class InviteData(
    @Serializable(with = UUIDSerializer::class) val inviteId: UUID,
)
