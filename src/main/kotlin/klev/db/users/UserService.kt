package klev.db.users

import io.ktor.client.HttpClient
import io.ktor.server.auth.BearerTokenCredential
import klev.db.users.google.GoogleUser
import klev.db.users.google.GoogleUserService
import klev.db.users.google.UsersToGoogleUsers
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class UserService(
    private val database: Database,
    private val googleUserService: GoogleUserService,
    private val httpClient: HttpClient,
) {
    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Users, UsersToGoogleUsers)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO, database) { block() }

    private suspend fun create(user: User): UUID =
        dbQuery {
            Users
                .insert {
                    it[firstName] = user.firstName
                    it[lastName] = user.lastName
                    it[email] = user.email
                }[Users.id]
                .value
        }

    suspend fun read(id: UUID?): User? =
        if (id == null) {
            null
        } else {
            dbQuery {
                Users
                    .select { Users.id eq id }
                    .map { User(id = id, firstName = it[Users.firstName], lastName = it[Users.lastName], email = it[Users.email]) }
                    .singleOrNull()
            }
        }

    private suspend fun getUserFromGoogleUser(googleUser: GoogleUser): User? {
        val userId =
            dbQuery {
                UsersToGoogleUsers
                    .select {
                        UsersToGoogleUsers.googleUserId eq googleUser.id
                    }.singleOrNull()
                    ?.getOrNull(UsersToGoogleUsers.userId)
            }
        return if (userId != null) {
            read(
                dbQuery {
                    userId
                },
            )
        } else {
            null
        }
    }

    private suspend fun GoogleUser.toUser(token: String) =
        getUserFromGoogleUser(this)?.also {
            updateAuthToken(this, token)
        } ?: read(
            create(
                User(
                    firstName = givenName,
                    lastName = familyName,
                    email = email,
                ),
            ).also { uid ->
                dbQuery {
                    UsersToGoogleUsers.insert {
                        it[userId] = uid
                        it[googleUserId] = this@toUser.id
                        it[authToken] = token
                    }
                }
            },
        )!!

    private suspend fun updateAuthToken(
        googleUser: GoogleUser,
        token: String,
    ) {
        dbQuery {
            UsersToGoogleUsers.update({ UsersToGoogleUsers.googleUserId eq googleUser.id }) {
                it[authToken] = token
                it[updated] = CurrentTimestamp()
            }
        }
    }

    suspend fun createOrUpdate(
        session: UserSession,
        provider: UserProvider,
    ) = createOrUpdate(token = session.token, provider = provider)

    suspend fun createOrUpdate(
        token: String,
        provider: UserProvider,
    ): User =
        when (provider) {
            UserProvider.GOOGLE -> {
                googleUserService.createOrUpdate(googleUser = GoogleUser.fromSession(httpClient, token)).toUser(token)
            }
        }

    suspend fun getUserByToken(tokenCredential: BearerTokenCredential) =
        dbQuery {
            UsersToGoogleUsers
                .select { UsersToGoogleUsers.authToken eq tokenCredential.token }
                .singleOrNull()
                ?.get(UsersToGoogleUsers.userId)
        }?.let { read(it) }
}
