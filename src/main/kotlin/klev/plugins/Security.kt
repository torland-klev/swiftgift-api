package klev.plugins

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
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
import io.ktor.server.request.uri
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import klev.db.users.UserAndSession
import klev.db.users.UserProvider
import klev.db.users.UserService
import klev.db.users.UserSession

fun Application.configureSecurity(
    httpClient: HttpClient,
    userService: UserService,
) {
    val env = dotenv()
    install(Sessions) {
        cookie<UserAndSession>(env["SESSION_COOKIE_NAME"])
    }

    val redirects = mutableMapOf<String, String>()

    install(Authentication) {
        basic("auth-basic") {
            validate {
                if (it.isValid(env)) {
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
            urlProvider = { env["GOOGLE_CALLBACK_URL"] }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = env["GOOGLE_CLIENT_ID"],
                    clientSecret = env["GOOGLE_CLIENT_SECRET"],
                    defaultScopes =
                        listOf(
                            "https://www.googleapis.com/auth/userinfo.profile",
                            "https://www.googleapis.com/auth/userinfo.email",
                        ),
                    extraAuthParameters = listOf("access_type" to "offline"),
                    onStateCreated = { call, state ->
                        // saves new state with redirect url value
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = it
                        }
                    },
                )
            }
            client = httpClient
        }
    }
    routing {
        authenticate("auth-basic") {
            get("/basic") {
                call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")
            }
        }
        authenticate("auth-bearer") {
            get("/bearer") {
                call.respondText(
                    "Hello, ${call.principal<UserIdPrincipal>()?.name?.toIntOrNull()
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
                currentPrincipal?.let { principal ->
                    principal.state?.let { state ->
                        val session = UserSession(state, principal.accessToken)
                        val user = userService.createOrUpdate(session = session, provider = UserProvider.GOOGLE)
                        call.sessions.set(UserAndSession(user = user, session = session))
                        redirects[state]?.let { redirect ->
                            call.respondRedirect(redirect)
                            return@get
                        }
                    }
                }
                call.respondRedirect("/home")
            }
        }
    }
}

fun UserPasswordCredential.isValid(env: Dotenv) = name == env["ADMIN_USERNAME"] && password == env["ADMIN_PASSWORD"]

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
