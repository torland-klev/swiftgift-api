package klev.plugins

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.auth.basic

fun Application.configureSecurity() {
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
    }
}

fun UserPasswordCredential.isValid() = name == dotenv()["ADMIN_USERNAME"] && password == dotenv()["ADMIN_PASSWORD"]
