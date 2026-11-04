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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : UserRepository {

    override fun getUserProfile(uid: String): Flow<Result<UserProfile?>> {
        if (uid.isBlank()) return flowOf(Result.success(null))
        val localProfile = com.example.mystudyapp.data.local.UserProfileLocalStore.getProfile(uid)
        val authUser = auth.currentUser

        return firestore.collection("users")
            .document(uid)
            .asFlow { it.toUserProfile() }
            .map { result ->
                val firestoreProfile = result.getOrNull()
                val effectiveDisplayName = if (!firestoreProfile?.displayName.isNullOrBlank()) {
                    firestoreProfile!!.displayName
                } else if (!localProfile?.displayName.isNullOrBlank()) {
                    localProfile!!.displayName
                } else if (!authUser?.displayName.isNullOrBlank()) {
                    authUser!!.displayName!!
                } else {
                    "Bạn học viên"
                }

                val effectiveAvatarId = if (!firestoreProfile?.avatarId.isNullOrBlank()) {
                    firestoreProfile!!.avatarId
                } else if (!localProfile?.avatarId.isNullOrBlank()) {
                    localProfile!!.avatarId
                } else {
                    "boy_hoodie"
                }

                val effectiveCustomPath = firestoreProfile?.customAvatarPath ?: localProfile?.customAvatarPath

                val base = firestoreProfile ?: localProfile ?: UserProfile(uid = uid)
                val effectiveProfile = base.copy(
                    displayName = effectiveDisplayName,
                    avatarId = effectiveAvatarId,
                    customAvatarPath = effectiveCustomPath
                )
                Result.success(effectiveProfile)
            }
    }

    override suspend fun createUserProfile(user: UserProfile): Result<Unit> {
        com.example.mystudyapp.data.local.UserProfileLocalStore.saveProfile(user)
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
        com.example.mystudyapp.data.local.UserProfileLocalStore.saveProfile(user)
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
        com.example.mystudyapp.data.local.UserProfileLocalStore.saveDisplayName(uid, displayName)
        return try {
            val updates = mapOf(
                "displayName" to displayName,
                "lastActiveAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users")
                .document(uid)
                .set(updates, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    override suspend fun updateAvatar(uid: String, avatarId: String, customAvatarPath: String?): Result<Unit> {
        com.example.mystudyapp.data.local.UserProfileLocalStore.saveAvatar(uid, avatarId, customAvatarPath)
        return try {
            val updates = mutableMapOf<String, Any?>(
                "avatarId" to avatarId,
                "customAvatarPath" to customAvatarPath,
                "lastActiveAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users")
                .document(uid)
                .set(updates, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    override suspend fun updateProfileDetails(
        uid: String,
        displayName: String,
        phoneNumber: String,
        dailyGoalMinutes: Int,
        levelId: String,
        levelName: String
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "displayName" to displayName,
                "phoneNumber" to phoneNumber,
                "dailyGoalMinutes" to dailyGoalMinutes,
                "lastActiveAt" to FieldValue.serverTimestamp()
            )
            if (levelId.isNotBlank()) updates["levelId"] = levelId
            if (levelName.isNotBlank()) updates["levelName"] = levelName

            firestore.collection("users")
                .document(uid)
                .set(updates, SetOptions.merge())
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
