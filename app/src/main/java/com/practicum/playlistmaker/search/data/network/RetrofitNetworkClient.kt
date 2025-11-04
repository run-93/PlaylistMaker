package com.practicum.playlistmaker.search.data.network

import com.practicum.playlistmaker.search.data.dto.Response
import com.practicum.playlistmaker.search.data.dto.TrackSearchRequest
import com.practicum.playlistmaker.search.data.dto.TrackSearchResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RetrofitNetworkClient : NetworkClient {

    private val iTunesBaseUrl = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val iTunesService = retrofit.create(iTunesApi::class.java)

    override fun doRequest(dto: Any): Response {
        return try {
            if (dto is TrackSearchRequest) {
                val resp = iTunesService.search(dto.query).execute()

                if (resp.isSuccessful) {
                    val body = resp.body() ?: Response()
                    body.apply { resultCode = resp.code() }
                } else {
                    // HTTP ошибка (4xx, 5xx)
                    Response().apply { resultCode = resp.code() }
                }
            } else {
                Response().apply { resultCode = 400 }
            }
        } catch (e: SocketTimeoutException) {
            // Таймаут соединения
            Response().apply { resultCode = -1 }
        } catch (e: UnknownHostException) {
            // Нет интернета или хост недоступен
            Response().apply { resultCode = -1 }
        } catch (e: IOException) {
            // Другие сетевые ошибки
            Response().apply { resultCode = -1 }
        } catch (e: Exception) {
            // Любые другие ошибки
            Response().apply { resultCode = -2 }
        }
    }
}