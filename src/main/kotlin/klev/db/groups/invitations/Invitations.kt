package klev.db.groups.invitations

import klev.db.UserTable
import klev.db.groups.Groups
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Invitations : UserTable() {
    val groupId = uuid("groupId").references(Groups.id, onDelete = ReferenceOption.CASCADE)
    val validUntil = timestamp("validUntil")
}
