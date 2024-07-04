package klev.db.groups

import kotlinx.serialization.Serializable

@Serializable
data class PartialGroup(
    val name: String? = null,
    val visibility: String? = null,
)
