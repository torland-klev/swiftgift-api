package klev.db.wishes

import klev.db.UserCRUD
import klev.db.wishes.Wishes.description
import klev.db.wishes.Wishes.img
import klev.db.wishes.Wishes.occasion
import klev.db.wishes.Wishes.status
import klev.db.wishes.Wishes.updated
import klev.db.wishes.Wishes.url
import klev.db.wishes.Wishes.userId
import klev.db.wishes.Wishes.visibility
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
        statement[occasion] = obj.occasion
        statement[status] = Status.OPEN
        statement[description] = obj.description
        statement[url] = obj.url
        statement[img] = obj.img
        statement[visibility] = obj.visibility
    }

    override suspend fun readMap(input: ResultRow): Wish =
        Wish(
            id = input[Wishes.id].value,
            userId = input[Wishes.userId],
            description = input[description],
            url = input[url],
            occasion = input[occasion],
            status = input[status],
            img = input[img],
            visibility = input[visibility],
        )

    override fun updateMap(
        update: UpdateStatement,
        obj: Wish,
    ) {
        update[userId] = obj.userId
        update[occasion] = obj.occasion
        update[status] = obj.status
        update[description] = obj.description
        update[url] = obj.url
        update[img] = obj.img
        update[visibility] = obj.visibility
        update[updated] = CurrentTimestamp()
    }

    override suspend fun allOwnedByUser(userId: Int?) = super.allOwnedByUser(userId).filterNot { it.status == Status.DELETED }

    override suspend fun publicPrivacyFilter(input: Wish) = input.visibility == WishVisibility.PUBLIC

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
