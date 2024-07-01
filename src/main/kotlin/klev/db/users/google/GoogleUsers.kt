package klev.db.users.google

import org.jetbrains.exposed.sql.Table

object GoogleUsers : Table() {
    val id = varchar("id", length = 64)
    val name = varchar("name", length = 64)
    val givenName = varchar("givenName", length = 64)
    val familyName = varchar("familyName", length = 64)
    val email = varchar("email", length = 128)
    val verifiedEmail = bool("verifiedEmail")
    val picture = varchar("picture", length = 256)

    override val primaryKey = PrimaryKey(id)
}
