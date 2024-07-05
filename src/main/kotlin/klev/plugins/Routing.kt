package klev.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import klev.db.groups.GroupsRoutes
import klev.db.groups.memberships.GroupMembershipRoutes
import klev.db.users.UserRoutes
import klev.db.wishes.Occasion
import klev.db.wishes.Status
import klev.db.wishes.WishesRoutes
import klev.mainHtml

fun Application.configureRouting(
    wishesRoutes: WishesRoutes,
    groupsRoutes: GroupsRoutes,
    groupMembershipRoutes: GroupMembershipRoutes,
    userRoutes: UserRoutes,
) {
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
            route("/groups") {
                get {
                    groupsRoutes.all(call)
                }
                post {
                    groupsRoutes.post(call)
                }
                route("/{groupId}") {
                    get {
                        groupsRoutes.get(call)
                    }
                    delete {
                        groupsRoutes.deleteIfOwner(call)
                    }
                    route("/memberships") {
                        get {
                            groupMembershipRoutes.allByGroup(call)
                        }
                        get("/{membershipId}") {
                            groupMembershipRoutes.get(call)
                        }
                        delete("/{membershipId}") {
                            groupMembershipRoutes.deleteIfCanAdmin(call)
                        }
                    }
                }
            }
            route("/users") {
                get {
                    userRoutes.get(call)
                }
                route("/{id}") {
                    get {
                        userRoutes.getById(call)
                    }
                }
            }
            route("/wishes") {
                get {
                    wishesRoutes.allUserHasReadAccessTo(call)
                }
                post {
                    wishesRoutes.post(call)
                }
                route("/{id}") {
                    get {
                        wishesRoutes.getId(call)
                    }
                    delete {
                        wishesRoutes.delete(call)
                    }
                    patch {
                        wishesRoutes.patch(call)
                    }
                }
            }
        }
    }
}
