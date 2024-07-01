package klev.db.users

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val firstName = varchar("firstName", length = 64)
    val lastName = varchar("lastName", length = 64)
    val email = varchar("email", length = 128)

    override val primaryKey = PrimaryKey(id)
}
