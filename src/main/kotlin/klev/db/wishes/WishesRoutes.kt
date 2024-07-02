package klev.db.wishes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class WishesRoutes(
    private val wishesService: WishesService,
) {
    suspend fun patch(call: ApplicationCall) {
        val id = call.parameters["id"]?.toIntOrNull()
        val userId = call.principal<UserIdPrincipal>()?.name?.toIntOrNull()
        val partialWish = call.receive<PartialWish>()
        val updated = wishesService.update(id, userId, partialWish)
        if (updated != null) {
            call.respond(HttpStatusCode.OK, updated)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    suspend fun delete(call: ApplicationCall) {
        val id = call.parameters["id"]?.toIntOrNull()
        val userId = call.principal<UserIdPrincipal>()?.name?.toIntOrNull()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized)
        } else if (id == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            val rowsDeleted = wishesService.delete(id, userId)
            if (rowsDeleted > 0) {
                call.respond(HttpStatusCode.OK, rowsDeleted)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

    suspend fun getId(call: ApplicationCall) {
        val found =
            wishesService.read(
                id = call.parameters["id"]?.toIntOrNull(),
                userId = call.principal<UserIdPrincipal>()?.name?.toIntOrNull(),
            )
        if (found != null) {
            call.respond(HttpStatusCode.OK, found)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    suspend fun post(call: ApplicationCall) {
        call.principal<UserIdPrincipal>()?.name?.toIntOrNull()?.let { userId ->
            val partialWish = call.receive<PartialWish>()
            val occasion = partialWish.occasion?.let { Occasion.valueOf(it.uppercase()) } ?: Occasion.NONE
            call.respond(
                wishesService.create(
                    Wish(
                        userId = userId,
                        occasion = occasion,
                        description = partialWish.description,
                        url = partialWish.url,
                        img = partialWish.img,
                    ),
                ),
            )
        }
    }

    suspend fun all(call: ApplicationCall) {
        call.respond(wishesService.all(call.principal<UserIdPrincipal>()?.name?.toIntOrNull()))
    }
}
