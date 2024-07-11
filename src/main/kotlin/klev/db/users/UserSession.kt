package klev.db.users

import java.util.UUID

data class UserSession(
    val state: String,
    val token: String,
)

data class UserAndSession(
    val user: User,
    val session: UserSession,
)

data class InviteData(
    val inviteId: UUID,
)
