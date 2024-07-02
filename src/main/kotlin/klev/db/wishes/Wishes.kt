package klev.db.wishes

import klev.db.UserTable

object Wishes : UserTable() {
    val occasion = varchar("occasion", 15)
    val status = varchar("status", 15)
    val url = varchar("url", 255).nullable()
    val description = varchar("description", 511).nullable()
}
