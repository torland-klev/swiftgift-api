package klev.db.wishes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import klev.oauthUserId
import klev.routeId

class WishesRoutes(
    private val wishesService: WishesService,
) {
    suspend fun patch(call: ApplicationCall) {
        val id = call.routeId()
        val userId = call.oauthUserId()
        val partialWish = call.receive<PartialWish>()
        val updated = wishesService.update(id, userId, partialWish)
        if (updated != null) {
            call.respond(HttpStatusCode.OK, updated)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    suspend fun delete(call: ApplicationCall) {
        val id = call.routeId()
        val userId = call.oauthUserId()
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
                id = call.routeId(),
                userId = call.oauthUserId(),
            )
        if (found != null) {
            call.respond(HttpStatusCode.OK, found)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    suspend fun post(call: ApplicationCall) {
        call.oauthUserId()?.let { userId ->
            val partialWish = call.receive<PartialWish>()
            if (partialWish.title == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing required field title")
            } else {
                call.respond(
                    HttpStatusCode.Created,
                    wishesService.createByPartial(partial = partialWish, userId = userId),
                )
            }
        } ?: call.respond(HttpStatusCode.Unauthorized)
    }

    suspend fun allUserHasReadAccessTo(call: ApplicationCall) {
        val wishes =
            wishesService.allPublic() +
                wishesService.allOwnedByUser(call.oauthUserId()) +
                wishesService.allUserHasGroupAccessTo(call.oauthUserId())
        call.respond(wishes.toSet())
    }
}
