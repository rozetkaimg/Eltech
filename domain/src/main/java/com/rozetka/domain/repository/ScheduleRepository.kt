package com.rozetka.domain.repository

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.rozetka.domain.util.NetworkUtils
import com.rozetka.model.ScheduleModel
import com.rozetka.network.MospolytechMethods
import com.rozetka.storage.database.schedule.ScheduleStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.lang.Exception

class ScheduleRepository(
    private val networkApi: MospolytechMethods,
    private val storage: ScheduleStorage,
    private val context: Context
) {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getSchedule(groupTitle: String): Flow<Result<ScheduleModel>> = flow {
        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                val networkResponse = networkApi.getScheduleByGroup(groupTitle)
                storage.saveSchedule(networkResponse)
                emit(Result.success(networkResponse))

            } catch (e: Exception) {
                if (e is CancellationException) throw e

                Log.w("ScheduleRepository", "Network fetch failed, trying cache", e)
                val cachedData = storage.getSchedule(groupTitle)
                if (cachedData != null) {
                    emit(Result.success(cachedData))
                } else {
                    emit(Result.failure(Exception("Ошибка сети. Кэш пуст.", e)))
                }
            }
        } else {
            Log.d("ScheduleRepository", "No network, fetching from cache")
            val cachedData = storage.getSchedule(groupTitle)
            if (cachedData != null) {
                emit(Result.success(cachedData))
            } else {
                emit(Result.failure(Exception("Нет подключения к сети и кэш пуст.")))
            }
        }
    }
    fun getSessionScheduleOnlyNetwork(groupTitle: String): Flow<Result<ScheduleModel>> = flow {
        try {
            val networkResponse = networkApi.getSessionSchedule(groupTitle)

            emit(Result.success(networkResponse))
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            val errorMessage = if (e is IOException) {
                "Ошибка сети. Не удалось обновить данные."
            } else {
                e.message ?: "Неизвестная ошибка"
            }
            Log.e("ScheduleRepository", "Network-only fetch failed", e)
            emit(Result.failure(Exception(errorMessage, e)))
        }
    }

    fun getScheduleOnlyNetwork(groupTitle: String): Flow<Result<ScheduleModel>> = flow {
        try {
            val networkResponse = networkApi.getScheduleByGroup(groupTitle)
            storage.saveSchedule(networkResponse)
            emit(Result.success(networkResponse))
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            val errorMessage = if (e is IOException) {
                "Ошибка сети. Не удалось обновить данные."
            } else {
                e.message ?: "Неизвестная ошибка"
            }
            Log.e("ScheduleRepository", "Network-only fetch failed", e)
            emit(Result.failure(Exception(errorMessage, e)))
        }
    }
}