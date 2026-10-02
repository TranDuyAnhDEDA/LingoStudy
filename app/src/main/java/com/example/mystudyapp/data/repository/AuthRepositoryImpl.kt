package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.model.AuthState
import com.example.mystudyapp.data.model.AuthUser
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val authState: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                trySend(AuthState.Authenticated(firebaseUser.toAuthUser()))
            } else {
                trySend(AuthState.Unauthenticated)
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Failed to create account. Please try again."))

            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            }

            val updatedUser = firebaseAuth.currentUser ?: firebaseUser
            Result.success(updatedUser.toAuthUser())
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthException(e)))
        }
    }

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Login failed. Please try again."))

            Result.success(firebaseUser.toAuthUser())
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthException(e)))
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): AuthUser? {
        return firebaseAuth.currentUser?.toAuthUser()
    }

    override fun observeAuthState(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            displayName = displayName
        )
    }

    private fun mapAuthException(e: Throwable): String {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password. Please check your credentials."
            is FirebaseAuthInvalidUserException -> "No account found with this email address."
            is FirebaseAuthUserCollisionException -> "An account with this email address already exists."
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters."
            is FirebaseNetworkException -> "Network error. Please check your internet connection and try again."
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email address."
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email address already exists."
                    "ERROR_WEAK_PASSWORD" -> "Password is too weak. Please use a stronger password."
                    "ERROR_USER_DISABLED" -> "This user account has been disabled."
                    else -> "Authentication failed: ${e.localizedMessage ?: "Please try again."}"
                }
            }
            else -> e.localizedMessage ?: "An unexpected authentication error occurred."
        }
    }
}
