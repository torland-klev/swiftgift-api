package klev.db.groups.invitations

import klev.db.UserCRUD
import klev.db.groups.invitations.Invitations.validUntil
import klev.db.groups.memberships.GroupMemberships.groupId
import klev.db.groups.memberships.GroupMemberships.userId
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import java.util.UUID

class InvitationService(
    database: Database,
) : UserCRUD<Invitation>(database, Invitations) {
    override suspend fun readMap(input: ResultRow) =
        Invitation(
            id = input[Invitations.id].value,
            groupId = input[Invitations.groupId],
            invitedBy = input[Invitations.userId],
            validUntil = input[validUntil],
        )

    override suspend fun publicPrivacyFilter(input: Invitation) = false

    override fun createMap(
        statement: InsertStatement<Number>,
        obj: Invitation,
    ) {
        statement[groupId] = obj.groupId
        statement[userId] = obj.invitedBy
        statement[validUntil] = obj.validUntil
    }

    override fun updateMap(
        update: UpdateStatement,
        obj: Invitation,
    ) = Unit

    suspend fun create(
        groupId: UUID,
        invitedBy: UUID,
    ) = create(Invitation(id = UUID.randomUUID(), groupId = groupId, invitedBy = invitedBy))
}
