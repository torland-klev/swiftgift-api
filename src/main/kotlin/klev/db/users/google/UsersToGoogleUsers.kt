package klev.db.users.google

import klev.db.users.Users
import org.jetbrains.exposed.sql.Table

object UsersToGoogleUsers : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("userId") references Users.id
    val googleUserId = varchar("googleUserId", 64) references GoogleUsers.id
    val authToken = varchar("authToken", 256)

    override val primaryKey = PrimaryKey(Users.id)
}
