package com.malicious.scootertoolkit.api

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

data class Mac(val mac: String)
data class Update(val mac: String, val payed: Boolean)

class ApiClient {
    private val client = HttpClient(Apache) {
        install(JsonFeature)
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    private val IP = "192.168.1.103:8000"

    suspend fun createScooter(data: Mac) {
        val response = client.post<Unit> {
            url("http://$IP/api/ransom/")
            contentType(ContentType.Application.Json)
            body = data
        }

        println("Response: $response")
    }

    suspend fun updateScooterStatus(data: Update) {
        val response = client.put<Unit> {
            url("http://$IP/api/ransom/${data.mac}/")
            contentType(ContentType.Application.Json)
            body = data
        }

        println("Response: $response")
    }

    suspend fun getKey(data: Mac): String? {
        return client.get<String?> {
            url("http://$IP/api/ransom/${data.mac}/")
        }
    }

    suspend fun getBms(data: Mac): String? {
        return client.get<String?> {
            url("http://$IP/api/ransom/${data.mac}/")
        }
    }
}

suspend fun test() {
    val apiClient = ApiClient()

    apiClient.createScooter(Mac("B1-B1-B1-B1-B1"))

    println("Retrieved Task: ${apiClient.getKey(Mac("B1-B1-B1-B1-B1"))}")

    apiClient.updateScooterStatus(Update("B1-B1-B1-B1-B1", true))

    println("Retrieved Task: ${apiClient.getKey(Mac("B1-B1-B1-B1-B1"))}")
}