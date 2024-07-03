package klev.db.groups

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

class GroupsRoutes(
    private val groupService: GroupService,
) {
    suspend fun allPublic(call: ApplicationCall) {
        call.respond(HttpStatusCode.OK, groupService.all())
    }

    suspend fun all(call: ApplicationCall) {
        val wishes = groupService.all() + groupService.all(call.principal<UserIdPrincipal>()?.name?.toIntOrNull())
        call.respond(wishes.toSet())
    }
}
