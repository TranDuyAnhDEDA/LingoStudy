package com.example.mystudyapp.data.model

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
}
