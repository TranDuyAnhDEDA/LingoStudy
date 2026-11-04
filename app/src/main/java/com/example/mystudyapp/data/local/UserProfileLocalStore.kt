package com.example.mystudyapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.mystudyapp.data.model.UserProfile
import java.util.concurrent.ConcurrentHashMap

object UserProfileLocalStore {
    private var prefs: SharedPreferences? = null
    private val memoryStore = ConcurrentHashMap<String, String>()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = try {
                context.applicationContext.getSharedPreferences("lingo_user_profile_prefs", Context.MODE_PRIVATE)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun saveAvatar(uid: String, avatarId: String, customAvatarPath: String? = null) {
        memoryStore["${uid}_avatarId"] = avatarId
        if (customAvatarPath != null) {
            memoryStore["${uid}_customAvatarPath"] = customAvatarPath
        } else {
            memoryStore.remove("${uid}_customAvatarPath")
        }

        prefs?.edit()?.apply {
            putString("${uid}_avatarId", avatarId)
            if (customAvatarPath != null) {
                putString("${uid}_customAvatarPath", customAvatarPath)
            } else {
                remove("${uid}_customAvatarPath")
            }
            apply()
        }
    }

    fun saveDisplayName(uid: String, displayName: String) {
        memoryStore["${uid}_displayName"] = displayName
        prefs?.edit()?.putString("${uid}_displayName", displayName)?.apply()
    }

    fun saveProfile(profile: UserProfile) {
        val uid = profile.uid
        saveDisplayName(uid, profile.displayName)
        saveAvatar(uid, profile.avatarId, profile.customAvatarPath)
    }

    fun getProfile(uid: String): UserProfile? {
        val name = memoryStore["${uid}_displayName"] ?: prefs?.getString("${uid}_displayName", null)
        val avatar = memoryStore["${uid}_avatarId"] ?: prefs?.getString("${uid}_avatarId", null)
        val custom = memoryStore["${uid}_customAvatarPath"] ?: prefs?.getString("${uid}_customAvatarPath", null)

        if (name == null && avatar == null && custom == null) return null

        return UserProfile(
            uid = uid,
            displayName = name.orEmpty(),
            avatarId = avatar ?: "boy_hoodie",
            customAvatarPath = custom
        )
    }
}
