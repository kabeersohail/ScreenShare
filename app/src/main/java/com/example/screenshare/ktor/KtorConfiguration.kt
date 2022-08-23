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
                    BasicAuthCredentials("AC9068f0f7e93b796fa721efe9497e9add","10ace9e068e2a764956b4f5e6b0c56ae")
                }
            }
        }
    }
}