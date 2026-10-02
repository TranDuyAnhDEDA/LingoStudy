package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.mapper.asFlow
import com.example.mystudyapp.data.mapper.mapFirestoreException
import com.example.mystudyapp.data.mapper.toFirestoreMap
import com.example.mystudyapp.data.mapper.toUserProfile
import com.example.mystudyapp.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : UserRepository {

    override fun getUserProfile(uid: String): Flow<Result<UserProfile?>> {
        if (uid.isBlank()) return flowOf(Result.success(null))
        return firestore.collection("users")
            .document(uid)
            .asFlow { it.toUserProfile() }
    }

    override suspend fun createUserProfile(user: UserProfile): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.uid)
                .set(user.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    override suspend fun updateUserProfile(user: UserProfile): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.uid)
                .set(user.toFirestoreMap(), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    override suspend fun updateDisplayName(uid: String, displayName: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "displayName" to displayName,
                "lastActiveAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users")
                .document(uid)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    override fun getCurrentUserProfile(): Flow<Result<UserProfile?>> {
        val currentUid = auth.currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            return flowOf(Result.failure(Exception("No authenticated user found.")))
        }
        return getUserProfile(currentUid)
    }
}
