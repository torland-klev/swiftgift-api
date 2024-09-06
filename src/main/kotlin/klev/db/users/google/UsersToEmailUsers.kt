package klev.db.users.google

import klev.db.UserTable

object UsersToEmailUsers : UserTable() {
    val authToken = varchar("authToken", 256)
}
