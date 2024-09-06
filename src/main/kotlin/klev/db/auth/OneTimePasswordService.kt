package klev.db.auth

import klev.db.CRUD
import klev.db.auth.OneTimePasswords.code
import klev.db.auth.OneTimePasswords.email
import klev.db.auth.OneTimePasswords.id
import klev.db.auth.OneTimePasswords.validUntil
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

class OneTimePasswordService(
    database: Database,
    private val emailService: OneTimePasswordEmailService,
) : CRUD<OneTimePassword>(database, OneTimePasswords) {
    override suspend fun readMap(input: ResultRow) =
        OneTimePassword(
            id = input[id].value,
            email = input[email],
            code = input[code],
            validUntil = input[validUntil],
        )

    override suspend fun publicPrivacyFilter(input: OneTimePassword) = false

    override fun createMap(
        statement: InsertStatement<Number>,
        obj: OneTimePassword,
    ) {
        statement[id] = obj.id
        statement[email] = obj.email
        statement[code] = obj.code
        statement[validUntil] = obj.validUntil
    }

    override fun updateMap(
        update: UpdateStatement,
        obj: OneTimePassword,
    ) = Unit

    private suspend fun deleteAllForEmail(email: String) =
        dbQuery {
            OneTimePasswords.deleteWhere { OneTimePasswords.email eq email }
        }

    suspend fun generateAndSendOTP(email: String) {
        val oneTimePassword = OneTimePassword(email = email)
        deleteAllForEmail(email)
        create(oneTimePassword)
        emailService.sendOneTimePassword(oneTimePassword.email, oneTimePassword.code)
    }

    suspend fun isValid(content: EmailLogin) = false
}
