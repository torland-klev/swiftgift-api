package klev

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import klev.db.groups.GroupService
import klev.db.groups.GroupsRoutes
import klev.db.groups.groupsToWishes.GroupsToWishesService
import klev.db.groups.invitations.InvitationService
import klev.db.groups.memberships.GroupMembershipRoutes
import klev.db.groups.memberships.GroupMembershipService
import klev.db.users.UserRoutes
import klev.db.users.UserService
import klev.db.users.google.GoogleUserService
import klev.db.wishes.WishesRoutes
import klev.db.wishes.WishesService
import klev.plugins.configureHTTP
import klev.plugins.configureRouting
import klev.plugins.configureSecurity
import klev.plugins.configureSerialization
import org.jetbrains.exposed.sql.Database
import java.util.UUID

val env = dotenv()

fun main() {
    embeddedServer(Netty, port = env["PORT"].toInt(), host = env["HOST"], module = Application::module)
        .start(wait = true)
}

val applicationHttpClient =
    HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

val database =
    Database.connect(
        url = env["DB_URL"],
        user = env["DB_USER"],
        driver = env["DB_DRIVER"],
        password = env["DB_PASSWORD"],
    )

private val invitationService = InvitationService(database = database)
private val googleUserService = GoogleUserService(database)
private val userService = UserService(database = database, httpClient = applicationHttpClient, googleUserService = googleUserService)
private val groupsToWishesService = GroupsToWishesService(database = database)
private val groupMembershipService = GroupMembershipService(database)
private val groupService = GroupService(database = database, groupMembershipService = groupMembershipService, userService = userService)
private val wishesService =
    WishesService(
        database = database,
        groupsToWishesService = groupsToWishesService,
        groupMembershipService = groupMembershipService,
        groupService = groupService,
    )

fun ApplicationCall.oauthUserId() =
    try {
        principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
    } catch (e: IllegalArgumentException) {
        null
    }

fun ApplicationCall.routeId(param: String = "id") =
    try {
        parameters[param]?.let { UUID.fromString(it) }
    } catch (e: IllegalArgumentException) {
        null
    }

fun Application.module() {
    configureSecurity(
        httpClient = applicationHttpClient,
        userService = userService,
    )
    configureHTTP()
    configureSerialization()
    configureRouting(
        groupsRoutes =
            GroupsRoutes(
                groupService = groupService,
                userService = userService,
                groupMembershipService = groupMembershipService,
                invitationService = invitationService,
            ),
        userRoutes = UserRoutes(userService = userService),
        wishesRoutes = WishesRoutes(wishesService = wishesService),
        groupMembershipRoutes =
            GroupMembershipRoutes(
                groupService = groupService,
                userService = userService,
                groupMembershipService = groupMembershipService,
            ),
    )
}
