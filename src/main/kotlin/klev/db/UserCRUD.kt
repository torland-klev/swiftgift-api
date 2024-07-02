package klev.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

abstract class UserCRUD<T>(
    database: Database,
    private val table: UserTable,
) : CRUD<T>(database, table) {
    open suspend fun all(userId: Int?) =
        if (userId == null) {
            emptyList()
        } else {
            dbQuery {
                table.select { table.userId eq userId }.map(::readMap)
            }
        }

    override suspend fun delete(id: Int) = false

    override suspend fun update(
        id: Int,
        obj: T,
    ) = null

    suspend fun read(
        id: Int?,
        userId: Int?,
    ) = if (id == null || userId == null) {
        null
    } else {
        dbQuery {
            table.select { (table.userId eq userId) and (table.id eq id) }.map(::readMap).singleOrNull()
        }
    }

    open suspend fun delete(
        id: Int?,
        userId: Int?,
    ) = if (id == null || userId == null) {
        0
    } else {
        dbQuery {
            table.deleteWhere { (table.userId eq userId) and (table.id eq id) }
        }
    }

    suspend fun update(
        id: Int?,
        userId: Int?,
        obj: T,
    ) = if (id == null || userId == null) {
        null
    } else {
        dbQuery {
            table.update({ (table.userId eq userId) and (table.id eq id) }) { updateMap(it, obj) }
        }
        read(id, userId)
    }

    suspend fun deleteAll(userId: Int?) =
        if (userId == null) {
            0
        } else {
            dbQuery {
                table.deleteWhere { table.userId eq userId }
            }
        }
}
