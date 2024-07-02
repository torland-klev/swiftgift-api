package klev.db.wishes

import klev.db.UserCRUD
import klev.db.wishes.Wishes.description
import klev.db.wishes.Wishes.img
import klev.db.wishes.Wishes.occasion
import klev.db.wishes.Wishes.status
import klev.db.wishes.Wishes.updated
import klev.db.wishes.Wishes.url
import klev.db.wishes.Wishes.userId
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

class WishesService(
    database: Database,
) : UserCRUD<Wish>(database, Wishes) {
    override fun createMap(
        statement: InsertStatement<Number>,
        obj: Wish,
    ) {
        statement[userId] = obj.userId
        statement[occasion] = obj.occasion.name
        statement[status] = Status.OPEN.name
        statement[description] = obj.description
        statement[url] = obj.url
        statement[img] = obj.img
    }

    override fun readMap(input: ResultRow): Wish =
        Wish(
            id = input[Wishes.id].value,
            userId = input[Wishes.userId],
            description = input[description],
            url = input[url],
            occasion = Occasion.valueOf(input[occasion]),
            status = Status.valueOf(input[status]),
            img = input[img],
        )

    override fun updateMap(
        update: UpdateStatement,
        obj: Wish,
    ) {
        update[userId] = obj.userId
        update[occasion] = obj.occasion.name
        update[status] = obj.status.name
        update[description] = obj.description
        update[url] = obj.url
        update[img] = obj.img
        update[updated] = CurrentTimestamp()
    }

    override suspend fun all(userId: Int?) = super.all(userId).filterNot { it.status == Status.DELETED }

    suspend fun update(
        id: Int?,
        userId: Int?,
        partial: PartialWish,
    ): Wish? {
        val existing = read(id, userId)
        return if (existing == null) {
            null
        } else {
            val occasion = partial.occasion?.let { Occasion.valueOf(it.uppercase()) }
            val status = partial.status?.let { Status.valueOf(it.uppercase()) }
            update(
                id,
                userId,
                existing.copy(
                    occasion = occasion ?: existing.occasion,
                    status = status ?: existing.status,
                    url = partial.url ?: existing.url,
                    description = partial.description ?: existing.description,
                    img = partial.img ?: existing.img,
                ),
            )
        }
    }

    override suspend fun delete(
        id: Int?,
        userId: Int?,
    ): Int {
        val existing = read(id, userId)
        return if (existing == null) {
            0
        } else {
            update(id, userId, existing.copy(status = Status.DELETED))
            1
        }
    }
}
