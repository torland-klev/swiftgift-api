package klev.db.groups.memberships

import klev.db.UserTable
import klev.db.groups.Groups
import org.jetbrains.exposed.sql.ReferenceOption

object GroupMemberships : UserTable() {
    val groupId = uuid("groupId").references(Groups.id, onDelete = ReferenceOption.CASCADE)
    val role = enumerationByName<GroupMembershipRole>("role", 15)
}
