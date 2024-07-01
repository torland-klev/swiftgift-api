package klev.db.users

data class UserSession(
    val state: String,
    val token: String,
)

data class UserAndSession(
    val user: User,
    val session: UserSession,
)
