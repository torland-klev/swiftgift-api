package klev.db.wishes

import klev.db.users.Users
import klev.db.users.google.GoogleUsers
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Wishes : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("userId") references Users.id
    val occasion = varchar("occasion", 15)
    val status = varchar("status", 15)
    val url = varchar("url", 255).nullable()
    val description = varchar("description", 511).nullable()
    val created = timestamp("created").defaultExpression(CurrentTimestamp())
    val updated = timestamp("updated").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(GoogleUsers.id)
}
