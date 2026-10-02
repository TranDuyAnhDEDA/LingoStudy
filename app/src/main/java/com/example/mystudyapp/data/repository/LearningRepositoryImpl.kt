package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.mapper.asFlow
import com.example.mystudyapp.data.mapper.toGrammar
import com.example.mystudyapp.data.mapper.toLesson
import com.example.mystudyapp.data.mapper.toLevel
import com.example.mystudyapp.data.mapper.toListening
import com.example.mystudyapp.data.mapper.toPronunciation
import com.example.mystudyapp.data.mapper.toQuiz
import com.example.mystudyapp.data.mapper.toUnitModel
import com.example.mystudyapp.data.mapper.toVocabulary
import com.example.mystudyapp.data.model.Grammar
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.Level
import com.example.mystudyapp.data.model.Listening
import com.example.mystudyapp.data.model.Pronunciation
import com.example.mystudyapp.data.model.Quiz
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.model.Vocabulary
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LearningRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LearningRepository {

    override fun getLevels(): Flow<Result<List<Level>>> {
        return firestore.collection("levels")
            .orderBy("order", Query.Direction.ASCENDING)
            .asFlow { it.toLevel() }
    }

    override fun getLevel(levelId: String): Flow<Result<Level?>> {
        if (levelId.isBlank()) return flowOf(Result.success(null))
        return firestore.collection("levels")
            .document(levelId)
            .asFlow { it.toLevel() }
    }

    override fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>> {
        if (levelId.isBlank()) return flowOf(Result.success(emptyList()))
        return firestore.collection("units")
            .whereEqualTo("levelId", levelId)
            .orderBy("order", Query.Direction.ASCENDING)
            .asFlow { it.toUnitModel() }
    }

    override fun getUnit(unitId: String): Flow<Result<Unit?>> {
        if (unitId.isBlank()) return flowOf(Result.success(null))
        return firestore.collection("units")
            .document(unitId)
            .asFlow { it.toUnitModel() }
    }

    override fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>> {
        if (unitId.isBlank()) return flowOf(Result.success(emptyList()))
        return firestore.collection("lessons")
            .whereEqualTo("unitId", unitId)
            .orderBy("order", Query.Direction.ASCENDING)
            .asFlow { it.toLesson() }
    }

    override fun getLesson(lessonId: String): Flow<Result<Lesson?>> {
        if (lessonId.isBlank()) return flowOf(Result.success(null))
        return firestore.collection("lessons")
            .document(lessonId)
            .asFlow { it.toLesson() }
    }

    override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> {
        if (lessonId.isBlank()) return flowOf(Result.success(emptyList()))
        return firestore.collection("vocabulary")
            .whereEqualTo("lessonId", lessonId)
            .asFlow { it.toVocabulary() }
    }

    override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> {
        if (lessonId.isBlank()) return flowOf(Result.success(emptyList()))
        return firestore.collection("grammar")
            .whereEqualTo("lessonId", lessonId)
            .asFlow { it.toGrammar() }
    }

    override fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>> {
        if (lessonId.isBlank()) return flowOf(Result.success(emptyList()))
        return firestore.collection("listening")
            .whereEqualTo("lessonId", lessonId)
            .asFlow { it.toListening() }
    }

    override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>> {
        if (lessonId.isBlank()) return flowOf(Result.success(emptyList()))
        return firestore.collection("pronunciation")
            .whereEqualTo("lessonId", lessonId)
            .asFlow { it.toPronunciation() }
    }

    override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>> {
        if (lessonId.isBlank()) return flowOf(Result.success(emptyList()))
        return firestore.collection("quizzes")
            .whereEqualTo("lessonId", lessonId)
            .asFlow { it.toQuiz() }
    }
}
