package com.example.mystudyapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.mystudyapp.data.model.DailyStudyTime
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.UserProgress
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object ProgressLocalStore {
    private var prefs: SharedPreferences? = null
    private val memoryLessonProgress = ConcurrentHashMap<String, LessonProgress>()
    private val memoryStudySeconds = ConcurrentHashMap<String, Long>()
    private val memoryXp = ConcurrentHashMap<String, Long>()
    private val memoryStreak = ConcurrentHashMap<String, Int>()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = try {
                context.applicationContext.getSharedPreferences("lingo_progress_prefs", Context.MODE_PRIVATE)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun saveLessonProgress(uid: String, progress: LessonProgress) {
        val key = "${uid}_lesson_${progress.lessonId}"
        memoryLessonProgress[key] = progress

        val json = JSONObject().apply {
            put("lessonId", progress.lessonId)
            put("progress", progress.progress)
            put("completed", progress.completed)
            put("xpEarned", progress.xpEarned)
            put("vocabularyCompleted", progress.vocabularyCompleted)
            put("grammarCompleted", progress.grammarCompleted)
            put("listeningCompleted", progress.listeningCompleted)
            put("pronunciationCompleted", progress.pronunciationCompleted)
            put("quizCompleted", progress.quizCompleted)
            put("lastVocabularyIndex", progress.lastVocabularyIndex)
            put("lastGrammarIndex", progress.lastGrammarIndex)
            put("lastListeningIndex", progress.lastListeningIndex)
            put("lastPronunciationIndex", progress.lastPronunciationIndex)
            put("lastQuizIndex", progress.lastQuizIndex)
            put("quizScore", progress.quizScore)
        }.toString()

        prefs?.edit()?.putString(key, json)?.apply()
    }

    fun getLessonProgress(uid: String, lessonId: String): LessonProgress? {
        val key = "${uid}_lesson_${lessonId}"
        memoryLessonProgress[key]?.let { return it }

        val jsonStr = prefs?.getString(key, null) ?: return null
        return try {
            val json = JSONObject(jsonStr)
            val lp = LessonProgress(
                lessonId = json.getString("lessonId"),
                progress = json.optInt("progress", 0),
                completed = json.optBoolean("completed", false),
                xpEarned = json.optInt("xpEarned", 0),
                vocabularyCompleted = json.optBoolean("vocabularyCompleted", false),
                grammarCompleted = json.optBoolean("grammarCompleted", false),
                listeningCompleted = json.optBoolean("listeningCompleted", false),
                pronunciationCompleted = json.optBoolean("pronunciationCompleted", false),
                quizCompleted = json.optBoolean("quizCompleted", false),
                lastVocabularyIndex = json.optInt("lastVocabularyIndex", 0),
                lastGrammarIndex = json.optInt("lastGrammarIndex", 0),
                lastListeningIndex = json.optInt("lastListeningIndex", 0),
                lastPronunciationIndex = json.optInt("lastPronunciationIndex", 0),
                lastQuizIndex = json.optInt("lastQuizIndex", 0),
                quizScore = json.optInt("quizScore", 0)
            )
            memoryLessonProgress[key] = lp
            lp
        } catch (e: Exception) {
            null
        }
    }

    fun addXp(uid: String, amount: Long) {
        val key = "${uid}_xp"
        val current = memoryXp[key] ?: prefs?.getLong(key, 0L) ?: 0L
        val updated = current + amount
        memoryXp[key] = updated
        prefs?.edit()?.putLong(key, updated)?.apply()
    }

    fun getXp(uid: String): Long {
        val key = "${uid}_xp"
        return memoryXp[key] ?: prefs?.getLong(key, 0L) ?: 0L
    }

    fun setStreak(uid: String, streak: Int) {
        val key = "${uid}_streak"
        memoryStreak[key] = streak
        prefs?.edit()?.putInt(key, streak)?.apply()
    }

    fun getStreak(uid: String): Int {
        val key = "${uid}_streak"
        return memoryStreak[key] ?: prefs?.getInt(key, 1) ?: 1
    }

    fun recordStudyTime(uid: String, secondsToAdd: Long) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Calendar.getInstance().time)
        val key = "${uid}_study_${todayStr}"

        val current = memoryStudySeconds[key] ?: prefs?.getLong(key, 0L) ?: 0L
        val updated = current + secondsToAdd
        memoryStudySeconds[key] = updated
        prefs?.edit()?.putLong(key, updated)?.apply()

        // Total
        val totalKey = "${uid}_total_study_seconds"
        val currTotal = prefs?.getLong(totalKey, 0L) ?: 0L
        prefs?.edit()?.putLong(totalKey, currTotal + secondsToAdd)?.apply()
    }

    fun getTodayStudySeconds(uid: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Calendar.getInstance().time)
        val key = "${uid}_study_${todayStr}"
        return memoryStudySeconds[key] ?: prefs?.getLong(key, 0L) ?: 0L
    }

    fun getLast7DaysStudyTime(uid: String): List<DailyStudyTime> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val list = mutableListOf<DailyStudyTime>()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = sdf.format(cal.time)
            val key = "${uid}_study_${dateStr}"
            val secs = memoryStudySeconds[key] ?: prefs?.getLong(key, 0L) ?: 0L
            list.add(DailyStudyTime(date = dateStr, totalSeconds = secs))
        }
        return list
    }

    fun getAllTimeStudyTime(uid: String): Long {
        val totalKey = "${uid}_total_study_seconds"
        return prefs?.getLong(totalKey, 0L) ?: 0L
    }
}
