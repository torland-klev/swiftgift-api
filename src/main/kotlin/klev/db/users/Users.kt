package klev.db.users

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Users : Table() {
    val id = integer("id").autoIncrement()
    val firstName = varchar("firstName", length = 63)
    val lastName = varchar("lastName", length = 63)
    val email = varchar("email", length = 127)
    val created = timestamp("created").defaultExpression(CurrentTimestamp())
    val updated = timestamp("updated").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}
