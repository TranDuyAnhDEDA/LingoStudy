package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.model.Grammar
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.Level
import com.example.mystudyapp.data.model.Listening
import com.example.mystudyapp.data.model.Pronunciation
import com.example.mystudyapp.data.model.Quiz
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.model.Vocabulary
import kotlinx.coroutines.flow.Flow

interface LearningRepository {
    fun getLevels(): Flow<Result<List<Level>>>
    fun getLevel(levelId: String): Flow<Result<Level?>>
    fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>>
    fun getUnit(unitId: String): Flow<Result<Unit?>>
    fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>>
    fun getLesson(lessonId: String): Flow<Result<Lesson?>>
    fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>>
    fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>>
    fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>>
    fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>>
    fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>>
}
