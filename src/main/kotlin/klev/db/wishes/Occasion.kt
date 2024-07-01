package klev.db.wishes

import kotlinx.serialization.Serializable

@Serializable
enum class Occasion {
    BIRTHDAY,
    CHRISTMAS,
    WEDDING,
    GRADUATION,
    NONE,
}
