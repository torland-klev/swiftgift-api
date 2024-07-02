package klev.db.wishes

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class WishesService(
    database: Database,
) {
    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Wishes)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun read(id: Int) =
        dbQuery {
            Wishes
                .select { Wishes.id eq id }
                .map {
                    Wish(
                        id = id,
                        userId = it[Wishes.userId],
                        description = it[Wishes.description],
                        url = it[Wishes.url],
                        occasion = Occasion.valueOf(it[Wishes.occasion]),
                        status = Status.valueOf(it[Wishes.status]),
                    )
                }.singleOrNull()
        }

    suspend fun create(
        userId: Int,
        occasion: Occasion,
        description: String?,
        url: String?,
    ) = read(
        dbQuery {
            Wishes.insert {
                it[this.userId] = userId
                it[this.occasion] = occasion.name
                it[status] = Status.OPEN.name
                it[this.description] = description
                it[this.url] = url
            }[Wishes.id]
        },
    )!!

    suspend fun all(userId: Int?) =
        if (userId == null) {
            emptyList()
        } else {
            dbQuery {
                Wishes.select { Wishes.userId eq userId }.map {
                    Wish(
                        id = it[Wishes.id],
                        userId = userId,
                        description = it[Wishes.description],
                        url = it[Wishes.url],
                        occasion = Occasion.valueOf(it[Wishes.occasion]),
                        status = Status.valueOf(it[Wishes.status]),
                    )
                }
            }
        }
}
