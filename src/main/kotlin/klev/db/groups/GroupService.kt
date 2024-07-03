package klev.db.groups

import klev.db.CRUD
import klev.db.groups.Groups.createdBy
import klev.db.groups.Groups.name
import klev.db.groups.Groups.updated
import klev.db.groups.Groups.visibility
import klev.db.users.UserService
import klev.userService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

class GroupService(
    database: Database,
    groupMembershipService: GroupMembershipService,
    userService: UserService,
) : CRUD<Group>(database, Groups) {
    override suspend fun readMap(input: ResultRow): Group {
        val userId = input[createdBy]
        val user = requireNotNull(userService.read(userId)) { "User $userId not found" }
        return Group(id = input[Groups.id].value, name = input[name], createdBy = user, visibility = input[visibility])
    }

    override suspend fun publicPrivacyFilter(input: Group) = input.visibility == GroupVisibility.PUBLIC

    override fun createMap(
        statement: InsertStatement<Number>,
        obj: Group,
    ) {
        statement[name] = obj.name
        statement[createdBy] = obj.createdBy.id
        statement[visibility] = obj.visibility
    }

    override fun updateMap(
        update: UpdateStatement,
        obj: Group,
    ) {
        update[name] = obj.name
        update[createdBy] = obj.createdBy.id
        update[visibility] = obj.visibility
        update[updated] = CurrentTimestamp()
    }

    suspend fun all(userId: Int?) = all().filterNot { it.createdBy.id == userId }
}
