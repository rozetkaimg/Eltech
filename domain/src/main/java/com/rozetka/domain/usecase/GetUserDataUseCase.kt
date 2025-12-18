package com.rozetka.domain.usecase

import com.rozetka.domain.repository.UserRepository

class GetUserDataUseCase(private val userRepository: UserRepository) {
    suspend fun execute(token: String) =
        userRepository.getUserData(token)
}