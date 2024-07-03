package klev.db.wishes

import klev.db.UserTable

object Wishes : UserTable() {
    val occasion = enumerationByName<Occasion>("occasion", 15)
    val status = enumerationByName<Status>("status", 15)
    val visibility = enumerationByName<WishVisibility>("visibility", 15)
    val url = varchar("url", 255).nullable()
    val description = varchar("description", 511).nullable()
    val img = varchar("img", 255).nullable()
}
