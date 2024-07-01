package klev.db.wishes

import kotlinx.serialization.Serializable

@Serializable
data class Wish(
    val id: Int,
    val userId: Int,
    val occasion: Occasion,
    val status: Status,
    val url: String?,
    val description: String?,
)
