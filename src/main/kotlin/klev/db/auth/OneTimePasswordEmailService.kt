package klev.db.auth

import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.isSuccess
import io.ktor.http.path
import klev.env

class OneTimePasswordEmailService(
    private val httpClient: HttpClient,
) {
    suspend fun sendOneTimePassword(
        email: String,
        code: Int,
    ) {
        val response =
            httpClient.post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = env("EMAIL_API_HOST")
                    path("api/send")
                }
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(env("EMAIL_API_TOKEN"))
                }
                timeout {
                    requestTimeoutMillis = 20_000
                }
                setBody(
                    """{
                |"from": {"email":"mailtrap@swiftgift.no","name":"SwiftGift"},
                |"to":[{"email":"$email"}],
                |"template_uuid":"${env("EMAIL_API_TEMPLATE")}",
                |"template_variables": {"code":"$code"}
                |}
                    """.trimMargin(),
                )
            }

        require(response.status.isSuccess()) {
            "Failed to send email OTP. Received status code"
        }
    }
}
