package klev.db.images

import klev.db.UserTable

object Images : UserTable() {
    val image = blob("image")
    val fileType = varchar("fileType", 63).nullable()
}
