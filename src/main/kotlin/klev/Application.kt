package klev

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
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

val googleUserService = GoogleUserService(database)
val userService = UserService(database = database, httpClient = applicationHttpClient, googleUserService = googleUserService)
val wishesService = WishesService(database = database)

fun Application.module() {
    configureSecurity(httpClient = applicationHttpClient, userService = userService)
    configureHTTP()
    configureSerialization()
    configureRouting(wishesRoutes = WishesRoutes(wishesService = wishesService))
}
