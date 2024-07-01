package klev.db.users

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val firstName = varchar("firstName", length = 63)
    val lastName = varchar("lastName", length = 63)
    val email = varchar("email", length = 127)

    override val primaryKey = PrimaryKey(id)
}
