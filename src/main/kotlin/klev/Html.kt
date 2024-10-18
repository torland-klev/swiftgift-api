package klev

import io.ktor.server.html.respondHtml
import io.ktor.server.routing.RoutingContext
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.img
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

suspend fun RoutingContext.mainHtml() =
    call.respondHtml {
        head {
            meta(charset = "UTF-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
            title("SwiftGift API")
            style {
                unsafe {
                    raw(
                        """
                        body {
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            margin: 0;
                            background-color: rgb(23, 25, 31);
                            font-family: Arial, sans-serif;
                        }
                        .login-container {
                            background: white;
                            padding: 2rem;
                            border-radius: 8px;
                            background-color: rgb(16, 18, 22);
                            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                        }
                        .login-button {
                            display: flex;
                            align-items: center;
                            flex-direction: row;
                            justify-content: center;
                            gap: 1rem;
                            padding: 1rem 4rem;
                            font-size: 1.1rem;
                            color: black;
                            background-color: rgb(255, 255, 255);
                            border: none;
                            border-radius: 4px;
                            text-decoration: none;
                            cursor: pointer;
                        }
                        .login-button:hover {
                            background-color: rgb(207, 208, 208);
                        }
                        img {
                            width: 28px;
                            height: 28px;
                            margin-left: -2.9rem;
                            margin-right: 2rem;
                        }
                        """.trimIndent(),
                    )
                }
            }
        }
        body {
            div(classes = "login-container") {
                p {
                    a(href = "/login", classes = "login-button") {
                        img(src = "https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg", alt = "Google logo")
                        +"Sign in with Google"
                    }
                }
            }
        }
    }
