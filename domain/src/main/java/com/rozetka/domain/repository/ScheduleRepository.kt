package com.rozetka.domain.repository

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.rozetka.domain.util.NetworkUtils
import com.rozetka.domain.util.StringObject
import com.rozetka.model.ScheduleModel
import com.rozetka.network.MospolytechMethods
import com.rozetka.storage.database.schedule.ScheduleStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ScheduleRepository(
    private val networkApi: MospolytechMethods,
    private val storage: ScheduleStorage,
    private val projectActivityRepository: ProjectActivityRepository,
    private val context: Context
) {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getSchedule(groupTitle: String): Flow<Result<ScheduleModel>> = flow {
        val userGroup = StringObject.groupName.trim()
        val isUserOwnGroup = userGroup.isNotEmpty() && groupTitle.trim().equals(userGroup, ignoreCase = true)

        val cachedData = storage.getSchedule(groupTitle)
        if (cachedData != null) {
            val token = StringObject.ApiToken
            val updatedCache = if (isUserOwnGroup && token.isNotBlank()) {
                runCatching {
                    projectActivityRepository.replacePDDiscipline(cachedData, token)
                }.getOrDefault(cachedData)
            } else {
                cachedData
            }
            emit(Result.success(updatedCache))
        }

        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                val networkResponse = networkApi.getScheduleByGroup(groupTitle)
                val token = StringObject.ApiToken
                val updatedNetwork = if (isUserOwnGroup && token.isNotBlank()) {
                    runCatching {
                        projectActivityRepository.replacePDDiscipline(networkResponse, token)
                    }.getOrDefault(networkResponse)
                } else {
                    networkResponse
                }

                storage.saveSchedule(updatedNetwork)
                emit(Result.success(updatedNetwork))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w("ScheduleRepository", "Network fetch failed", e)
                if (cachedData == null) {
                    emit(Result.failure(Exception("Ошибка сети. Кэш пуст.", e)))
                }
            }
        } else if (cachedData == null) {
            emit(Result.failure(Exception("Нет подключения к сети и кэш пуст.")))
        }
    }

    fun getSessionSchedule(groupTitle: String): Flow<Result<ScheduleModel>> = flow {
        val cachedData = storage.getSchedule(groupTitle)
        if (cachedData != null && cachedData.isSession == true) {
            emit(Result.success(cachedData))
        }

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
