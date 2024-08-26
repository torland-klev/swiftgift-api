package klev.db.users

import io.ktor.client.HttpClient
import io.ktor.server.auth.BearerTokenCredential
import klev.db.users.apple.AppleUser
import klev.db.users.apple.AppleUserDTO
import klev.db.users.apple.AppleUserService
import klev.db.users.apple.UsersToAppleUsers
import klev.db.users.google.GoogleAppUser
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
    private val appleUserService: AppleUserService,
    private val googleUserService: GoogleUserService,
    private val httpClient: HttpClient,
) {
    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Users, UsersToGoogleUsers, UsersToAppleUsers)
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

    private suspend fun getUserFromAppleUser(appleUser: AppleUser): User? {
        val userId =
            dbQuery {
                UsersToAppleUsers
                    .select {
                        UsersToAppleUsers.appleUserId eq appleUser.userIdentifier
                    }.singleOrNull()
                    ?.getOrNull(UsersToAppleUsers.userId)
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

    private suspend fun AppleUser.toUser() =
        getUserFromAppleUser(this) ?: read(
            create(
                User(firstName = this.givenName, lastName = this.familyName, email = this.email),
            ).also { uid ->
                dbQuery {
                    UsersToAppleUsers.insert {
                        it[userId] = uid
                        it[appleUserId] = this@toUser.userIdentifier
                        it[authToken] = this@toUser.identityToken
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

    suspend fun createOrUpdate(session: UserSession) =
        googleUserService.createOrUpdate(googleUser = GoogleUser.fromSession(httpClient, session.token)).toUser(session.token)

    suspend fun getUserByToken(tokenCredential: BearerTokenCredential) =
        dbQuery {
            UsersToGoogleUsers
                .select { UsersToGoogleUsers.authToken eq tokenCredential.token }
                .singleOrNull()
                ?.get(UsersToGoogleUsers.userId)
        }?.let { read(it) }

    suspend fun createOrUpdate(appleUser: AppleUser) = appleUserService.createOrUpdate(appleUser).toUser()

    suspend fun createOrUpdate(googleAppUser: GoogleAppUser) =
        googleUserService
            .createOrUpdate(
                googleUser = GoogleUser.fromSession(httpClient, googleAppUser.accessToken),
            ).toUser(googleAppUser.accessToken)

    suspend fun read(appleUserDTO: AppleUserDTO) = appleUserService.read(appleUserDTO = appleUserDTO)?.toUser()
}
