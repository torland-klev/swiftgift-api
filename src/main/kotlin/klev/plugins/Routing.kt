package klev.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import klev.db.wishes.Occasion
import klev.db.wishes.PartialWish
import klev.db.wishes.Status
import klev.db.wishes.WishesService
import klev.mainHtml

fun Application.configureRouting(wishesService: WishesService) {
    routing {
        get("/") {
            mainHtml()
        }
        get("/home") {
            val userSession = getSession(call)
            if (userSession != null) {
                call.respondText(userSession.session.token)
            }
        }
        get("/wishes/status") {
            call.respond(Status.entries.map { it.name })
        }
        get("/wishes/occasion") {
            call.respond(Occasion.entries.map { it.name })
        }
        authenticate("auth-bearer") {
            route("/wishes") {
                get {
                    call.respond(wishesService.all(call.principal<UserIdPrincipal>()?.name?.toIntOrNull()))
                }
                post {
                    call.principal<UserIdPrincipal>()?.name?.toIntOrNull()?.let { userId ->
                        val partialWish = call.receive<PartialWish>()
                        val occasion = partialWish.occasion?.let { Occasion.valueOf(it.uppercase()) } ?: Occasion.NONE
                        call.respond(
                            wishesService.create(
                                userId = userId,
                                occasion = occasion,
                                description = partialWish.description,
                                url = partialWish.url,
                            ),
                        )
                    }
                }
            }
        }
    }
}
