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
        // 1. Emit cached data immediately if exists
        val cachedData = storage.getSchedule(groupTitle)
        if (cachedData != null) {
            emit(Result.success(cachedData))
        }

        // 2. Fetch from network and update cache
        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                val networkResponse = networkApi.getScheduleByGroup(groupTitle)
                storage.saveSchedule(networkResponse)
                emit(Result.success(networkResponse))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w("ScheduleRepository", "Network fetch failed", e)
                // If we didn't have cache, emit error. Otherwise, we already emitted cache.
                if (cachedData == null) {
                    emit(Result.failure(Exception("Ошибка сети. Кэш пуст.", e)))
                }
            }
        } else if (cachedData == null) {
            emit(Result.failure(Exception("Нет подключения к сети и кэш пуст.")))
        }
    }

    fun getSessionSchedule(groupTitle: String): Flow<Result<ScheduleModel>> = flow {
        // 1. Emit cached data immediately if exists
        val cachedData = storage.getSchedule(groupTitle) // Note: getSchedule in storage can handle session flag if we set it
        if (cachedData != null && cachedData.isSession == true) {
            emit(Result.success(cachedData))
        }

        // 2. Fetch from network and update cache
        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                val networkResponse = networkApi.getSessionSchedule(groupTitle)
                val responseWithFlag = networkResponse.copy(isSession = true)
                storage.saveSchedule(responseWithFlag)
                emit(Result.success(responseWithFlag))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w("ScheduleRepository", "Session network fetch failed", e)
                if (cachedData == null || cachedData.isSession != true) {
                    emit(Result.failure(Exception("Ошибка сети. Кэш сессии пуст.", e)))
                }
            }
        } else if (cachedData == null || cachedData.isSession != true) {
            emit(Result.failure(Exception("Нет подключения к сети и кэш сессии пуст.")))
        }
    }

    fun getSessionScheduleOnlyNetwork(groupTitle: String): Flow<Result<ScheduleModel>> = getSessionSchedule(groupTitle)

    fun getScheduleOnlyNetwork(groupTitle: String): Flow<Result<ScheduleModel>> = getSchedule(groupTitle)

}