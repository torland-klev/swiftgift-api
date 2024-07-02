package klev.db.users

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Users : IntIdTable() {
    val firstName = varchar("firstName", length = 63)
    val lastName = varchar("lastName", length = 63)
    val email = varchar("email", length = 127)
    val created = timestamp("created").defaultExpression(CurrentTimestamp())
    val updated = timestamp("updated").defaultExpression(CurrentTimestamp())
}
