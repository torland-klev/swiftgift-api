package klev.db.users.google

import klev.db.UserTable

object UsersToGoogleUsers : UserTable() {
    val googleUserId = varchar("googleUserId", 64) references GoogleUsers.id
    val authToken = varchar("authToken", 256)
}
