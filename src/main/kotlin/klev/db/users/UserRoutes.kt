package klev.db.users

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import klev.oauthUserId
import klev.routeId

class UserRoutes(
    private val userService: UserService,
) {
    suspend fun get(call: ApplicationCall) {
        val id = call.oauthUserId()
        if (id != null) {
            userService.read(id)?.let {
                call.respond(HttpStatusCode.OK, it)
            } ?: call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    suspend fun getById(call: ApplicationCall) {
        val oauthId = call.oauthUserId()
        val id = call.routeId()
        if (id == null || id != oauthId) {
            userService.read(id)?.let {
                call.respond(HttpStatusCode.OK, it.copy(email = "", lastName = ""))
            } ?: call.respond(HttpStatusCode.NotFound)
        } else {
            userService.read(id)?.let {
                call.respond(HttpStatusCode.OK, it)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }

    suspend fun me(call: ApplicationCall) {
        userService.read(call.oauthUserId())?.let {
            call.respond(HttpStatusCode.OK, it)
        } ?: call.respond(HttpStatusCode.NotFound)
    }
}
