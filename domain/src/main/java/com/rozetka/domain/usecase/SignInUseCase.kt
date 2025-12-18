package com.rozetka.domain.usecase

import com.rozetka.domain.repository.UserRepository

class SignInUseCase(private val userRepository: UserRepository) {
    suspend fun execute(login: String, password: String) =
        userRepository.signIn(login, password)
}