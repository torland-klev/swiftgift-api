package klev.db.groups

import klev.db.users.User
import klev.plugins.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Group(
    @Serializable(with = UUIDSerializer::class) val id: UUID = UUID.randomUUID(),
    val name: String,
    val createdBy: User,
    val visibility: GroupVisibility,
)
