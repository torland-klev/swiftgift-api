package klev.db.groups

import klev.db.UserTable
import org.jetbrains.exposed.sql.ReferenceOption

object GroupMemberships : UserTable() {
    val groupId = integer("groupId").references(Groups.id, onDelete = ReferenceOption.CASCADE)
    val role = enumerationByName<GroupMembershipRole>("role", 15)
}
