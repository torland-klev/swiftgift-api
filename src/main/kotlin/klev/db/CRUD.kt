package klev.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

abstract class CRUD<T>(
    database: Database,
    private val table: IntIdTable,
) {
    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(table)
        }
    }

    protected abstract fun readMap(input: ResultRow): T

    protected abstract fun updateMap(
        update: UpdateStatement,
        obj: T,
    )

    protected abstract fun createMap(
        statement: InsertStatement<Number>,
        obj: T,
    )

    open suspend fun read(id: Int) =
        dbQuery {
            table
                .select { table.id eq id }
                .map(::readMap)
                .singleOrNull()
        }

    open suspend fun update(
        id: Int,
        obj: T,
    ): T? {
        dbQuery {
            table.update({
                table.id eq id
            }) {
                updateMap(it, obj)
            }
        }
        return read(id)
    }

    suspend fun create(obj: T) =
        read(
            dbQuery {
                table
                    .insert {
                        createMap(it, obj)
                    }[table.id]
                    .value
            },
        )!!

    open suspend fun delete(id: Int) =
        dbQuery {
            table.deleteWhere { table.id eq id } > 0
        }

    protected suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}
