package klev.db.wishes

import kotlinx.serialization.Serializable

@Serializable
data class PartialWish(
    val occasion: String?,
    val url: String?,
    val description: String?,
)
