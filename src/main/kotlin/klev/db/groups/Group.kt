package klev.db.groups

import klev.db.users.User

data class Group(
    val id: Int = 0,
    val name: String,
    val createdBy: User,
    val visibility: GroupVisibility,
)
