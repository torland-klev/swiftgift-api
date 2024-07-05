package klev.db.groups.memberships

import klev.plugins.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class GroupMembership(
    @Serializable(with = UUIDSerializer::class) val groupId: UUID,
    @Serializable(with = UUIDSerializer::class) val userId: UUID,
    val role: GroupMembershipRole,
)
