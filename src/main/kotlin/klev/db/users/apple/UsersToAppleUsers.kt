package klev.db.users.apple

import klev.db.UserTable

object UsersToAppleUsers : UserTable() {
    val appleUserId = varchar("appleUserId", 64) references AppleUsers.id
    val authToken = varchar("authToken", 256)
}
