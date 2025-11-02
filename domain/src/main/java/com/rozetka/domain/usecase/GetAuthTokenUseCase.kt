package com.rozetka.domain.usecase

import com.rozetka.domain.repository.UserRepository

class GetAuthTokenUseCase(private val userRepository: UserRepository) {
    fun execute(): String? = userRepository.getAuthToken()
}