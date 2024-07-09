package klev.db.users.google

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.prepareRequest
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.headers
import io.ktor.http.path
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleUser(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("verified_email") val verifiedEmail: Boolean,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val picture: String,
) {
    companion object {
        suspend fun fromSession(
            httpClient: HttpClient,
            token: String,
        ): GoogleUser {
            val request =
                httpClient.prepareRequest {
                    method = HttpMethod.Get
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "www.googleapis.com"
                        path("oauth2/v2/userinfo")
                    }
                    headers {
                        bearerAuth(token)
                    }
                }

            val response = request.execute()

            return response.body()
        }
    }
}
