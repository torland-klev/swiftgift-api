package klev.db.groups.groupsToWishes

import klev.plugins.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class GroupToWish(
    @Serializable(with = UUIDSerializer::class) val wishId: UUID,
    @Serializable(with = UUIDSerializer::class) val groupId: UUID,
)
