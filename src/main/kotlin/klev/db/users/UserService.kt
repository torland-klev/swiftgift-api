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
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class UserService(
    database: Database,
    private val googleUserService: GoogleUserService,
    private val httpClient: HttpClient,
) {
    init {
        transaction(database) {
            SchemaUtils.create(Users)
            SchemaUtils.create(UsersToGoogleUsers)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }

    private suspend fun create(user: User): Int =
        dbQuery {
            Users.insert {
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[email] = user.email
            }[Users.id]
        }

    private suspend fun read(id: Int): User? =
        dbQuery {
            Users
                .select { Users.id eq id }
                .map { User(id = id, firstName = it[Users.firstName], lastName = it[Users.lastName], email = it[Users.email]) }
                .singleOrNull()
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
        getUserFromGoogleUser(this) ?: read(
            create(
                User(
                    id = 0,
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

    suspend fun createOrUpdate(
        session: UserSession,
        provider: UserProvider,
    ): User =
        when (provider) {
            UserProvider.GOOGLE -> {
                googleUserService.createOrUpdate(GoogleUser.fromSession(httpClient, session)).toUser(session.token)
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
