package klev.db.groups.memberships

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import klev.db.groups.GroupService
import klev.db.users.UserService
import klev.oauthUserId
import klev.routeId

class GroupMembershipRoutes(
    private val groupService: GroupService,
    private val userService: UserService,
    private val groupMembershipService: GroupMembershipService,
) {
    suspend fun allByGroup(call: ApplicationCall) {
        val userId = call.oauthUserId()
        val groupId = call.routeId("groupId")
        val group = groupService.getIfCanAdmin(groupId = groupId, userId = userId)
        if (group == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            val memberships = groupMembershipService.allByGroup(groupId = group.id)
            val users = memberships.map { userService.read(it.userId) }
            call.respond(HttpStatusCode.OK, users)
        }
    }

    suspend fun deleteIfCanAdmin(call: ApplicationCall) {
        val userId = call.oauthUserId()
        val groupId = call.routeId("groupId")
        val membershipId = call.routeId("membershipId")
        val group = groupService.getIfCanAdmin(groupId = groupId, userId = userId)
        if (group == null || membershipId == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            val deleteSuccesses =
                groupMembershipService
                    .allByGroup(groupId = group.id)
                    .filter {
                        it.userId == membershipId && it.groupId == group.id && it.role != GroupMembershipRole.OWNER
                    }.map { groupMembershipService.delete(it.id) }
            if (deleteSuccesses.isEmpty() || deleteSuccesses.none { it }) {
                call.respond(HttpStatusCode.NotFound)
            } else if (deleteSuccesses.all { it }) {
                call.respond(HttpStatusCode.OK)
            } else if (deleteSuccesses.any { it }) {
                call.respond(HttpStatusCode.PartialContent)
            }
        }
    }

    suspend fun get(call: ApplicationCall) {
        val userId = call.oauthUserId()
        val groupId = call.routeId("groupId")
        val membershipId = call.routeId("membershipId")
        val group = groupService.getIfCanAdmin(groupId = groupId, userId = userId)
        if (group == null || membershipId == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            val membership = groupMembershipService.allByGroup(groupId = group.id).firstOrNull { it.userId == membershipId }
            if (membership == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK, membership)
            }
        }
    }
}
