package klev.db.groups

import klev.db.UserCRUD
import klev.db.groups.GroupMemberships.groupId
import klev.db.groups.GroupMemberships.role
import klev.db.groups.GroupMemberships.userId
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

class GroupMembershipService(
    database: Database,
) : UserCRUD<GroupMembership>(database, GroupMemberships) {
    override suspend fun readMap(input: ResultRow) =
        GroupMembership(
            groupId = input[groupId],
            userId = input[userId],
            role = input[role],
        )

    override suspend fun publicPrivacyFilter(input: GroupMembership): Boolean = false

    override fun createMap(
        statement: InsertStatement<Number>,
        obj: GroupMembership,
    ) {
        statement[groupId] = obj.groupId
        statement[userId] = obj.userId
        statement[role] = obj.role
    }

    override fun updateMap(
        update: UpdateStatement,
        obj: GroupMembership,
    ) {
        update[groupId] = obj.groupId
        update[userId] = obj.userId
        update[role] = obj.role
    }

    suspend fun isMember(
        userId: Int,
        groupId: Int,
    ) = dbQuery {
        GroupMemberships.select { (GroupMemberships.userId eq userId) and (GroupMemberships.groupId eq groupId) }.count()
    } > 0

    suspend fun isOwner(
        userId: Int,
        groupId: Int,
    ) = dbQuery {
        GroupMemberships
            .select {
                (GroupMemberships.userId eq userId) and (GroupMemberships.groupId eq groupId) and
                    (role eq GroupMembershipRole.OWNER)
            }.count()
    } > 0
}
