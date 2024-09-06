package klev.plugins

import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.auth.bearer
import io.ktor.server.auth.oauth
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import klev.db.auth.EmailLogin
import klev.db.auth.OneTimePasswordService
import klev.db.groups.invitations.InvitationService
import klev.db.users.InviteData
import klev.db.users.UserAndSession
import klev.db.users.UserService
import klev.db.users.UserSession
import klev.db.users.apple.AppleUserDTO
import klev.db.users.google.GoogleAppUser
import klev.env
import klev.oauthUserId
import java.util.UUID

fun Application.configureSecurity(
    httpClient: HttpClient,
    userService: UserService,
    invitationService: InvitationService,
    otpService: OneTimePasswordService,
) {
    install(Sessions) {
        cookie<UserAndSession>(env("SESSION_COOKIE_NAME"))
        cookie<InviteData>("invite_data")
    }

    install(Authentication) {
        basic("auth-basic") {
            validate {
                if (it.isValid()) {
                    UserIdPrincipal(it.name)
                } else {
                    null
                }
            }
        }
        bearer("auth-bearer") {
            authenticate { tokenCredential ->
                val user = userService.getUserByToken(tokenCredential)
                if (user != null) {
                    UserIdPrincipal(user.id.toString())
                } else {
                    null
                }
            }
        }
        oauth("auth-oauth-google") {
            urlProvider = { env("GOOGLE_CALLBACK_URL") }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = env("GOOGLE_CLIENT_ID"),
                    clientSecret = env("GOOGLE_CLIENT_SECRET"),
                    defaultScopes =
                        listOf(
                            "https://www.googleapis.com/auth/userinfo.profile",
                            "https://www.googleapis.com/auth/userinfo.email",
                        ),
                    extraAuthParameters = listOf("access_type" to "offline"),
                )
            }
            client = httpClient
        }
    }
    routing {
        route("/app/login/apple") {
            post {
                val appUserDto = call.receive<AppleUserDTO>()
                val user =
                    if (appUserDto.isFirstTimeLogin()) {
                        userService.createOrUpdate(
                            appleUser = appUserDto.toAppUser(),
                        )
                    } else {
                        userService.read(appleUserDTO = appUserDto)
                    }
                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                } else {
                    call.respond(HttpStatusCode.OK, user)
                }
            }
        }
        route("/app/login") {
            post("/google") {
                val appUser = call.receive<GoogleAppUser>()
                val user = userService.createOrUpdate(googleAppUser = appUser)
                call.respond(HttpStatusCode.OK, user)
            }
            post("/email") {
                val content = call.receive<EmailLogin>()
                if (content.code == null) {
                    otpService.generateAndSendOTP(content.email)
                    call.respond(HttpStatusCode.OK)
                } else if (otpService.isValid(content)) {
                    val user = userService.getUserByEmail(content.email)
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
        get("/confirmInvite/{inviteId}") {
            try {
                val uuid = UUID.fromString(call.parameters["inviteId"])
                call.sessions.set(InviteData(inviteId = uuid))
            } catch (e: IllegalArgumentException) {
                // Do nothing
            }
            call.respondRedirect("/login")
        }
        authenticate("auth-basic") {
            get("/basic") {
                call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")
            }
        }
        authenticate("auth-bearer") {
            get("/bearer") {
                call.respondText(
                    "Hello, ${call.oauthUserId()
                        ?.let { userService.read(it) }?.email}!",
                )
            }
        }
        authenticate("auth-oauth-google") {
            get("/login") {
                // Redirects to 'authorizeUrl' automatically
            }
            get("/callback") {
                val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                // redirects home if the url is not found before authorization
                val user =
                    currentPrincipal?.let { principal ->
                        principal.state?.let { state ->
                            val session = UserSession(state, principal.accessToken)
                            val user = userService.createOrUpdate(session = session)
                            call.sessions.set(UserAndSession(user = user, session = session))
                            user
                        }
                    }

                call.sessions.get<InviteData>()?.inviteId?.let { inviteId ->
                    user?.id?.let { userId -> invitationService.completeInvitation(inviteId, userId) }
                }
                call.respondRedirect("/token")
            }
        }
    }
}

fun UserPasswordCredential.isValid() = name == env("ADMIN_USERNAME") && password == env("ADMIN_PASSWORD")

suspend fun getSession(call: ApplicationCall): UserAndSession? {
    val userSession: UserAndSession? = call.sessions.get()
    if (userSession == null) {
        val redirectUrl =
            URLBuilder("http://0.0.0.0:8080/login").run {
                parameters.append("redirectUrl", call.request.uri)
                build()
            }
        call.respondRedirect(redirectUrl)
        return null
    }
    return userSession
}
