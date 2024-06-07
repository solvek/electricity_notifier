package com.solvek.electricitynotifier

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters

class TelegramBot(apiKey: String) {
    private val client = HttpClient(CIO)
    private val url = "https://api.telegram.org/bot$apiKey/sendMessage"
    suspend fun send(to: String, message: String){
        Log.d("Telegram", "Sending message: $message")
        val response = client.post(url){
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("chat_id", to)
                        append("text", message)
                    }
                )
            )
//            contentType(ContentType.Application.Json)
//            setBody("{'chat_id': $TELEGRAM_CHAT_ID, 'text': '$message'}")
        }
        Log.d("Telegram", "Response: $response")
    }
}