package klev.db.images

import io.ktor.http.ContentType
import klev.db.UserCRUD
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

class ImageService(
    database: Database,
) : UserCRUD<Image>(database, Images) {
    override suspend fun readMap(input: ResultRow) =
        Image(
            id = input[Images.id].value,
            userId = input[Images.userId],
            image = input[Images.image],
            fileType = input[Images.fileType]?.let { ContentType.parse(it) },
        )

    override suspend fun publicPrivacyFilter(input: Image) = false

    override fun createMap(
        statement: InsertStatement<Number>,
        obj: Image,
    ) {
        statement[Images.id] = obj.id
        statement[Images.userId] = obj.userId
        statement[Images.image] = obj.image
        statement[Images.fileType] = obj.fileType?.toString()
    }

    override fun updateMap(
        update: UpdateStatement,
        obj: Image,
    ) {
        update[Images.userId] = obj.userId
        update[Images.image] = obj.image
        update[Images.fileType] = obj.fileType?.toString()
        update[Images.updated] = CurrentTimestamp()
    }
}
