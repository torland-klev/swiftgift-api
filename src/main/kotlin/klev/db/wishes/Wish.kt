package klev.db.wishes

import klev.plugins.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Wish(
    @Serializable(with = UUIDSerializer::class) val id: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class) val userId: UUID,
    val occasion: Occasion,
    val status: Status = Status.OPEN,
    val url: String?,
    val title: String,
    val description: String?,
    val img: String?,
    val visibility: WishVisibility = WishVisibility.PRIVATE,
)
