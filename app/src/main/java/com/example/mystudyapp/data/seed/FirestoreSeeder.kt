package com.example.mystudyapp.data.seed

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class SeedSummary(
    val levelsCount: Int,
    val unitsCount: Int,
    val lessonsCount: Int,
    val vocabularyCount: Int,
    val grammarCount: Int,
    val listeningCount: Int,
    val pronunciationCount: Int,
    val quizzesCount: Int
)

object FirestoreSeeder {

    suspend fun seedSampleData(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<SeedSummary> {
        return try {
            // 1. Program
            val program = BeginnerCurriculumData.program
            firestore.collection("programs").document(program.id).set(
                mapOf(
                    "title" to program.title,
                    "description" to program.description,
                    "targetAudience" to program.targetAudience,
                    "learningObjectives" to program.learningObjectives,
                    "order" to program.order,
                    "isPublished" to program.isPublished,
                    "iconEmoji" to program.iconEmoji,
                    "levelIds" to program.levelIds,
                    "isAvailable" to program.isAvailable
                ),
                SetOptions.merge()
            ).await()

            // 2. Level
            val level = BeginnerCurriculumData.level
            firestore.collection("levels").document(level.id).set(
                mapOf(
                    "programId" to level.programId,
                    "name" to level.name,
                    "description" to level.description,
                    "order" to level.order,
                    "xpRequired" to level.xpRequired
                ),
                SetOptions.merge()
            ).await()

            // 3. Units
            for (u in BeginnerCurriculumData.units) {
                firestore.collection("units").document(u.id).set(
                    mapOf(
                        "levelId" to u.levelId,
                        "programId" to u.programId,
                        "title" to u.title,
                        "description" to u.description,
                        "order" to u.order,
                        "learningObjectives" to u.learningObjectives,
                        "estimatedMinutes" to u.estimatedMinutes,
                        "isPublished" to u.isPublished
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 4. Lessons
            for (l in BeginnerCurriculumData.lessons) {
                firestore.collection("lessons").document(l.id).set(
                    mapOf(
                        "unitId" to l.unitId,
                        "programId" to l.programId,
                        "title" to l.title,
                        "description" to l.description,
                        "order" to l.order,
                        "estimatedMinutes" to l.estimatedMinutes,
                        "xpReward" to l.xpReward,
                        "isPublished" to l.isPublished
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 5. Vocabularies
            for (v in BeginnerCurriculumData.sampleVocabularies) {
                firestore.collection("vocabulary").document(v.id).set(
                    mapOf(
                        "lessonId" to v.lessonId,
                        "word" to v.word,
                        "meaning" to v.meaning,
                        "partOfSpeech" to v.partOfSpeech,
                        "example" to v.example,
                        "translation" to v.translation,
                        "audioUrl" to (v.audioUrl ?: ""),
                        "imageUrl" to (v.imageUrl ?: ""),
                        "order" to v.order
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 6. Grammars
            for (g in BeginnerCurriculumData.sampleGrammars) {
                firestore.collection("grammar").document(g.id).set(
                    mapOf(
                        "lessonId" to g.lessonId,
                        "title" to g.title,
                        "explanation" to g.explanation,
                        "examples" to g.examples,
                        "order" to g.order
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 7. Listenings
            for (lis in BeginnerCurriculumData.sampleListenings) {
                firestore.collection("listening").document(lis.id).set(
                    mapOf(
                        "lessonId" to lis.lessonId,
                        "title" to lis.title,
                        "audioUrl" to (lis.audioUrl ?: ""),
                        "transcript" to lis.transcript,
                        "difficulty" to lis.difficulty,
                        "order" to lis.order
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 8. Pronunciations
            for (p in BeginnerCurriculumData.samplePronunciations) {
                firestore.collection("pronunciation").document(p.id).set(
                    mapOf(
                        "lessonId" to p.lessonId,
                        "word" to p.word,
                        "phonetic" to p.phonetic,
                        "audioUrl" to (p.audioUrl ?: ""),
                        "targetScore" to p.targetScore,
                        "order" to p.order
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 9. Quizzes
            for (q in BeginnerCurriculumData.sampleQuizzes) {
                firestore.collection("quizzes").document(q.id).set(
                    mapOf(
                        "lessonId" to q.lessonId,
                        "type" to q.type,
                        "question" to q.question,
                        "options" to q.options,
                        "correctAnswer" to q.correctAnswer,
                        "xpReward" to q.xpReward,
                        "order" to q.order
                    ),
                    SetOptions.merge()
                ).await()
            }

            Result.success(
                SeedSummary(
                    levelsCount = 1,
                    unitsCount = BeginnerCurriculumData.units.size,
                    lessonsCount = BeginnerCurriculumData.lessons.size,
                    vocabularyCount = BeginnerCurriculumData.sampleVocabularies.size,
                    grammarCount = BeginnerCurriculumData.sampleGrammars.size,
                    listeningCount = BeginnerCurriculumData.sampleListenings.size,
                    pronunciationCount = BeginnerCurriculumData.samplePronunciations.size,
                    quizzesCount = BeginnerCurriculumData.sampleQuizzes.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
