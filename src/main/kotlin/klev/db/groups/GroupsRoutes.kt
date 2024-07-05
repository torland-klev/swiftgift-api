package klev.db.groups

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import klev.db.groups.memberships.GroupMembershipService
import klev.db.users.UserService
import klev.oauthUserId
import klev.routeId

class GroupsRoutes(
    private val groupService: GroupService,
    private val userService: UserService,
    private val groupMembershipService: GroupMembershipService,
) {
    suspend fun all(call: ApplicationCall) {
        val groups =
            groupService.allPublic() +
                groupService.allCreatedByUser(call.oauthUserId()) +
                groupService.allUserIsMemberOf(call.oauthUserId())
        call.respond(groups.toSet())
    }

    suspend fun post(call: ApplicationCall) {
        call.oauthUserId()?.let { userId ->
            val user = userService.read(userId)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized)
            } else {
                val partialGroup = call.receive<PartialGroup>()
                if (partialGroup.name == null) {
                    call.respond(HttpStatusCode.BadRequest, "Group name is required")
                } else if (partialGroup.visibility == null) {
                    call.respond(HttpStatusCode.BadRequest, "Group visibility is required")
                } else if (GroupVisibility.entries.none { it.name == partialGroup.visibility.uppercase() }) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Group visibility ${partialGroup.visibility} not supported. Supported values are ${GroupVisibility.entries}",
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Created,
                        groupService.create(
                            Group(
                                name = partialGroup.name,
                                createdBy = user,
                                visibility = GroupVisibility.valueOf(partialGroup.visibility.uppercase()),
                            ),
                        ),
                    )
                }
            }
        } ?: call.respond(HttpStatusCode.Unauthorized)
    }

    suspend fun get(call: ApplicationCall) {
        val groupId = call.routeId("groupId")
        val group = groupService.getIfHasReadAccess(userId = call.oauthUserId(), groupId = groupId)
        if (group == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(HttpStatusCode.OK, group)
        }
    }

    suspend fun deleteIfOwner(call: ApplicationCall) {
        val groupId = call.routeId("groupId")
        val userId = call.oauthUserId()
        if (groupId == null || userId == null) {
            call.respond(HttpStatusCode.NotFound)
        } else if (groupMembershipService.isOwner(userId, groupId)) {
            if (groupService.delete(groupId)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotModified)
            }
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Only group owners can delete a group")
        }
    }
}
