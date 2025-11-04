package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.search.data.dto.TrackSearchRequest
import com.practicum.playlistmaker.search.data.dto.TrackSearchResponse
import com.practicum.playlistmaker.search.data.network.NetworkClient
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.domain.repository.TrackRepository
import java.io.IOException

class TrackRepositoryImpl(private val networkClient: NetworkClient) : TrackRepository {

    override fun search(query: String): List<Track> {
        return try {
            val response = networkClient.doRequest(TrackSearchRequest(query))

            // Проверяем код ответа
            when {
                response.resultCode == 200 && response is TrackSearchResponse -> {
                    // Успешный ответ
                    response.results.map { it.toDomain() }
                }
                response.resultCode == -1 -> {
                    // Сетевая ошибка - бросаем исключение
                    throw IOException("Network error")
                }
                response.resultCode in 400..599 -> {
                    // HTTP ошибки
                    throw IOException("HTTP error: ${response.resultCode}")
                }
                else -> {
                    // Другие случаи - пустой результат
                    emptyList()
                }
            }
        } catch (e: IOException) {
            // Пробрасываем сетевые ошибки дальше
            throw e
        } catch (e: Exception) {
            // Другие ошибки - пустой список
            emptyList()
        }
    }
}