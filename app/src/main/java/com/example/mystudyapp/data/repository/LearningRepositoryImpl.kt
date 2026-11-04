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
import com.example.mystudyapp.data.model.LearningProgram
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.Level
import com.example.mystudyapp.data.model.Listening
import com.example.mystudyapp.data.model.Pronunciation
import com.example.mystudyapp.data.model.Quiz
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.model.Vocabulary
import com.example.mystudyapp.data.seed.BeginnerCurriculumData
import com.example.mystudyapp.data.seed.KidsCurriculumData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class LearningRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LearningRepository {

    private val defaultPrograms = listOf(
        LearningProgram(
            id = "program_beginner",
            title = "Beginner English",
            titleVi = "Tiếng Anh cho người mới bắt đầu",
            description = "Build a strong foundation with practical everyday situations, travel, and basic conversations.",
            descriptionVi = "Học từ những lời chào cơ bản đến giao tiếp trong các tình huống quen thuộc hằng ngày.",
            targetAudience = "Beginners & Intermediate Learners",
            targetAudienceVi = "Người mới bắt đầu & Trình độ cơ bản",
            iconEmoji = "🌱",
            levelIds = listOf("level_beginner_1"),
            isAvailable = true
        ),
        LearningProgram(
            id = "program_kids",
            title = "English for Kids",
            titleVi = "Tiếng Anh cho trẻ em",
            description = "A colorful, interactive 10-unit English journey designed for kids with playful vocabulary, simple grammar, and fun activities.",
            descriptionVi = "Học tiếng Anh qua trò chơi, bài hát, câu chuyện và những từ vựng gần gũi.",
            targetAudience = "Children & Young Learners",
            targetAudienceVi = "Trẻ em & Mầm non",
            iconEmoji = "🧸",
            levelIds = listOf("level_kids_1"),
            isAvailable = true
        ),
        LearningProgram(
            id = "program_adults",
            title = "English for Adults",
            titleVi = "Tiếng Anh cho người đi làm",
            description = "Professional and conversational English for workplace communication, networking, and social interactions.",
            descriptionVi = "Phát triển kỹ năng giao tiếp công việc, hội họp, phỏng vấn và trao đổi chuyên nghiệp.",
            targetAudience = "Adult Professionals & University Students",
            targetAudienceVi = "Người đi làm & Sinh viên",
            iconEmoji = "💼",
            levelIds = emptyList(),
            isAvailable = false
        )
    )

    override fun getPrograms(): Flow<Result<List<LearningProgram>>> {
        return flowOf(Result.success(defaultPrograms))
    }

    override fun getProgram(programId: String): Flow<Result<LearningProgram?>> {
        val program = defaultPrograms.firstOrNull { it.id == programId }
        return flowOf(Result.success(program))
    }

    override fun getLevelsByProgram(programId: String): Flow<Result<List<Level>>> {
        if (programId.isBlank()) return flowOf(Result.success(emptyList()))
        val seedLevels = if (programId == "program_beginner") {
            listOf(BeginnerCurriculumData.level)
        } else if (programId == "program_kids") {
            listOf(KidsCurriculumData.level)
        } else {
            emptyList()
        }

        return firestore.collection("levels")
            .whereEqualTo("programId", programId)
            .asFlow { it.toLevel() }
            .map { result ->
                val list = result.getOrElse { emptyList() }
                val combined = if (list.isEmpty()) seedLevels else list.sortedBy { it.order }
                Result.success(combined)
            }
    }

    override fun getLevels(): Flow<Result<List<Level>>> {
        return firestore.collection("levels")
            .asFlow { it.toLevel() }
            .map { result ->
                val list = result.getOrElse { emptyList() }
                val combined = if (list.isEmpty()) {
                    listOf(BeginnerCurriculumData.level, KidsCurriculumData.level)
                } else {
                    list.sortedBy { it.order }
                }
                Result.success(combined)
            }
    }

    override fun getLevel(levelId: String): Flow<Result<Level?>> {
        if (levelId.isBlank()) return flowOf(Result.success(null))
        val seedLevel = if (levelId == "level_beginner_1") BeginnerCurriculumData.level
        else if (levelId == "level_kids_1") KidsCurriculumData.level
        else null

        return firestore.collection("levels")
            .document(levelId)
            .asFlow { it.toLevel() }
            .map { result ->
                val level = result.getOrNull() ?: seedLevel
                Result.success(level)
            }
    }

    override fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>> {
        if (levelId.isBlank()) return flowOf(Result.success(emptyList()))
        val seedUnits = if (levelId == "level_kids_1") KidsCurriculumData.units else BeginnerCurriculumData.units

        return firestore.collection("units")
            .whereEqualTo("levelId", levelId)
            .asFlow { it.toUnitModel() }
            .map { result ->
                val list = result.getOrElse { emptyList() }
                val combined = (list + seedUnits).distinctBy { it.id }.sortedBy { it.order }
                Result.success(combined)
            }
    }

    override fun getUnit(unitId: String): Flow<Result<Unit?>> {
        if (unitId.isBlank()) return flowOf(Result.success(null))
        val seedUnit = BeginnerCurriculumData.units.firstOrNull { it.id == unitId }
            ?: KidsCurriculumData.units.firstOrNull { it.id == unitId }

        return firestore.collection("units")
            .document(unitId)
            .asFlow { it.toUnitModel() }
            .map { result ->
                val unit = result.getOrNull() ?: seedUnit
                Result.success(unit)
            }
    }

    override fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>> {
        if (unitId.isBlank()) return flowOf(Result.success(emptyList()))
        val seedLessons = (BeginnerCurriculumData.lessons + KidsCurriculumData.lessons).filter { it.unitId == unitId }

        return firestore.collection("lessons")
            .whereEqualTo("unitId", unitId)
            .asFlow { it.toLesson() }
            .map { result ->
                val list = result.getOrElse { emptyList() }
                val combined = (list + seedLessons).distinctBy { it.id }.sortedBy { it.order }
                Result.success(combined)
            }
    }

    override fun getLesson(lessonId: String): Flow<Result<Lesson?>> {
        if (lessonId.isBlank()) return flowOf(Result.success(null))
        val seedLesson = BeginnerCurriculumData.lessons.firstOrNull { it.id == lessonId }
            ?: KidsCurriculumData.lessons.firstOrNull { it.id == lessonId }

        return firestore.collection("lessons")
            .document(lessonId)
            .asFlow { it.toLesson() }
            .map { result ->
                val lesson = result.getOrNull() ?: seedLesson
                Result.success(lesson)
            }
    }

    override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> {
        if (lessonId.isBlank()) return flowOf(Result.success(emptyList()))
        val seedVocabs = (BeginnerCurriculumData.sampleVocabularies + KidsCurriculumData.vocabularies).filter { it.lessonId == lessonId }

        return firestore.collection("vocabulary")
            .whereEqualTo("lessonId", lessonId)
            .asFlow { it.toVocabulary() }
            .map { result ->
                val list = result.getOrElse { emptyList() }
                val combined = (list + seedVocabs).distinctBy { it.id }.sortedBy { it.order }
                Result.success(combined)
            }
    }

    override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> {
        if (lessonId.isBlank()) return flowOf(Result.success(emptyList()))
        val seedGrammars = (BeginnerCurriculumData.sampleGrammars + KidsCurriculumData.grammars).filter { it.lessonId == lessonId }

        return firestore.collection("grammar")
            .whereEqualTo("lessonId", lessonId)
            .asFlow { it.toGrammar() }
            .map { result ->
                val list = result.getOrElse { emptyList() }
                val combined = (list + seedGrammars).distinctBy { it.id }.sortedBy { it.order }
                Result.success(combined)
            }
    }

    override fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>> {
        if (lessonId.isBlank()) return flowOf(Result.success(emptyList()))
        val seedListenings = (BeginnerCurriculumData.sampleListenings + KidsCurriculumData.listenings).filter { it.lessonId == lessonId }

        return firestore.collection("listening")
            .whereEqualTo("lessonId", lessonId)
            .asFlow { it.toListening() }
            .map { result ->
                val list = result.getOrElse { emptyList() }
                val combined = (list + seedListenings).distinctBy { it.id }.sortedBy { it.order }
                Result.success(combined)
            }
    }

    override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>> {
        if (lessonId.isBlank()) return flowOf(Result.success(emptyList()))
        val seedPronuns = (BeginnerCurriculumData.samplePronunciations + KidsCurriculumData.pronunciations).filter { it.lessonId == lessonId }

        return firestore.collection("pronunciation")
            .whereEqualTo("lessonId", lessonId)
            .asFlow { it.toPronunciation() }
            .map { result ->
                val list = result.getOrElse { emptyList() }
                val combined = (list + seedPronuns).distinctBy { it.id }.sortedBy { it.order }
                Result.success(combined)
            }
    }

    override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>> {
        if (lessonId.isBlank()) return flowOf(Result.success(emptyList()))
        val seedQuizzes = (BeginnerCurriculumData.sampleQuizzes + KidsCurriculumData.quizzes).filter { it.lessonId == lessonId }

        return firestore.collection("quizzes")
            .whereEqualTo("lessonId", lessonId)
            .asFlow { it.toQuiz() }
            .map { result ->
                val list = result.getOrElse { emptyList() }
                val combined = (list + seedQuizzes).distinctBy { it.id }.sortedBy { it.order }
                Result.success(combined)
            }
    }
}
