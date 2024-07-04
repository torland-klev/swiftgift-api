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
import klev.db.groups.GroupMembershipService
import klev.db.groups.GroupService
import klev.db.groups.GroupsRoutes
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

private val googleUserService = GoogleUserService(database)
private val userService = UserService(database = database, httpClient = applicationHttpClient, googleUserService = googleUserService)
private val wishesService = WishesService(database = database)
private val groupMembershipService = GroupMembershipService(database)
private val groupService = GroupService(database = database, groupMembershipService = groupMembershipService, userService = userService)

fun ApplicationCall.oauthUserId() = principal<UserIdPrincipal>()?.name?.toIntOrNull()

fun ApplicationCall.routeId() = parameters["id"]?.toIntOrNull()

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
            ),
        userRoutes = UserRoutes(userService = userService),
        wishesRoutes = WishesRoutes(wishesService = wishesService),
    )
}
