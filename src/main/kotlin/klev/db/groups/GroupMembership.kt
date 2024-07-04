package klev.db.groups

import kotlinx.serialization.Serializable

@Serializable
data class GroupMembership(
    val groupId: Int,
    val userId: Int,
    val role: GroupMembershipRole,
)
