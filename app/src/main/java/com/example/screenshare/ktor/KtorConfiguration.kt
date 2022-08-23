package com.example.screenshare.ktor

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*

class KtorConfiguration {
    val client = HttpClient(Android) {
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials("AC9068f0f7e93b796fa721efe9497e9add","cebd4436488a9fc2d44342a0ed0df74d")
                }
            }
        }
    }
}