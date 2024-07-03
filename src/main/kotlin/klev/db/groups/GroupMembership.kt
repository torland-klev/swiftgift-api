package klev.db.groups

data class GroupMembership(
    val groupId: Int,
    val userId: Int,
    val role: GroupMembershipRole,
)
