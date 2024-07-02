package klev.db.users.google

import klev.db.users.Users
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UsersToGoogleUsers : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("userId") references Users.id
    val googleUserId = varchar("googleUserId", 64) references GoogleUsers.id
    val authToken = varchar("authToken", 256)
    val created = timestamp("created").defaultExpression(CurrentTimestamp())
    val updated = timestamp("updated").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(Users.id)
}
