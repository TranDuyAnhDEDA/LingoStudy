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
                    "titleVi" to (program.titleVi ?: ""),
                    "description" to program.description,
                    "descriptionVi" to (program.descriptionVi ?: ""),
                    "targetAudience" to program.targetAudience,
                    "targetAudienceVi" to (program.targetAudienceVi ?: ""),
                    "learningObjectives" to program.learningObjectives,
                    "learningObjectivesVi" to (program.learningObjectivesVi ?: emptyList()),
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
                    "nameVi" to (level.nameVi ?: ""),
                    "description" to level.description,
                    "descriptionVi" to (level.descriptionVi ?: ""),
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
                        "titleVi" to (u.titleVi ?: ""),
                        "description" to u.description,
                        "descriptionVi" to (u.descriptionVi ?: ""),
                        "order" to u.order,
                        "learningObjectives" to u.learningObjectives,
                        "learningObjectivesVi" to (u.learningObjectivesVi ?: emptyList()),
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
                        "titleVi" to (l.titleVi ?: ""),
                        "description" to l.description,
                        "descriptionVi" to (l.descriptionVi ?: ""),
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
                        "meaningVi" to (v.meaningVi ?: ""),
                        "partOfSpeech" to v.partOfSpeech,
                        "example" to v.example,
                        "exampleVi" to (v.exampleVi ?: ""),
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
                        "titleVi" to (g.titleVi ?: ""),
                        "explanation" to g.explanation,
                        "explanationVi" to (g.explanationVi ?: ""),
                        "examples" to g.examples,
                        "examplesVi" to (g.examplesVi ?: emptyList()),
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
                        "titleVi" to (lis.titleVi ?: ""),
                        "audioUrl" to (lis.audioUrl ?: ""),
                        "transcript" to lis.transcript,
                        "transcriptVi" to (lis.transcriptVi ?: ""),
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
                        "instructionVi" to (p.instructionVi ?: ""),
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
                        "questionVi" to (q.questionVi ?: ""),
                        "options" to q.options,
                        "correctAnswer" to q.correctAnswer,
                        "explanationVi" to (q.explanationVi ?: ""),
                        "xpReward" to q.xpReward,
                        "order" to q.order
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 10. Seed Kids Curriculum
            try {
                seedKidsCurriculumData(firestore)
            } catch (e: Exception) {
                // Non-blocking for sample data
            }

            Result.success(
                SeedSummary(
                    levelsCount = 2,
                    unitsCount = BeginnerCurriculumData.units.size + KidsCurriculumData.units.size,
                    lessonsCount = BeginnerCurriculumData.lessons.size + KidsCurriculumData.lessons.size,
                    vocabularyCount = BeginnerCurriculumData.sampleVocabularies.size + KidsCurriculumData.vocabularies.size,
                    grammarCount = BeginnerCurriculumData.sampleGrammars.size + KidsCurriculumData.grammars.size,
                    listeningCount = BeginnerCurriculumData.sampleListenings.size + KidsCurriculumData.listenings.size,
                    pronunciationCount = BeginnerCurriculumData.samplePronunciations.size + KidsCurriculumData.pronunciations.size,
                    quizzesCount = BeginnerCurriculumData.sampleQuizzes.size + KidsCurriculumData.quizzes.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedKidsCurriculumData(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<SeedSummary> {
        return try {
            // 1. Program
            val program = KidsCurriculumData.program
            firestore.collection("programs").document(program.id).set(
                mapOf(
                    "title" to program.title,
                    "titleVi" to (program.titleVi ?: ""),
                    "description" to program.description,
                    "descriptionVi" to (program.descriptionVi ?: ""),
                    "targetAudience" to program.targetAudience,
                    "targetAudienceVi" to (program.targetAudienceVi ?: ""),
                    "learningObjectives" to program.learningObjectives,
                    "learningObjectivesVi" to (program.learningObjectivesVi ?: emptyList()),
                    "order" to program.order,
                    "isPublished" to program.isPublished,
                    "iconEmoji" to program.iconEmoji,
                    "levelIds" to program.levelIds,
                    "isAvailable" to program.isAvailable
                ),
                SetOptions.merge()
            ).await()

            // 2. Level
            val level = KidsCurriculumData.level
            firestore.collection("levels").document(level.id).set(
                mapOf(
                    "programId" to level.programId,
                    "name" to level.name,
                    "nameVi" to (level.nameVi ?: ""),
                    "description" to level.description,
                    "descriptionVi" to (level.descriptionVi ?: ""),
                    "order" to level.order,
                    "xpRequired" to level.xpRequired
                ),
                SetOptions.merge()
            ).await()

            // 3. Units
            for (u in KidsCurriculumData.units) {
                firestore.collection("units").document(u.id).set(
                    mapOf(
                        "levelId" to u.levelId,
                        "programId" to u.programId,
                        "title" to u.title,
                        "titleVi" to (u.titleVi ?: ""),
                        "description" to u.description,
                        "descriptionVi" to (u.descriptionVi ?: ""),
                        "order" to u.order,
                        "learningObjectives" to u.learningObjectives,
                        "learningObjectivesVi" to (u.learningObjectivesVi ?: emptyList()),
                        "estimatedMinutes" to u.estimatedMinutes,
                        "isPublished" to u.isPublished
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 4. Lessons
            for (l in KidsCurriculumData.lessons) {
                firestore.collection("lessons").document(l.id).set(
                    mapOf(
                        "unitId" to l.unitId,
                        "programId" to l.programId,
                        "title" to l.title,
                        "titleVi" to (l.titleVi ?: ""),
                        "description" to l.description,
                        "descriptionVi" to (l.descriptionVi ?: ""),
                        "order" to l.order,
                        "estimatedMinutes" to l.estimatedMinutes,
                        "xpReward" to l.xpReward,
                        "isPublished" to l.isPublished
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 5. Vocabularies
            for (v in KidsCurriculumData.vocabularies) {
                firestore.collection("vocabulary").document(v.id).set(
                    mapOf(
                        "lessonId" to v.lessonId,
                        "word" to v.word,
                        "meaning" to v.meaning,
                        "meaningVi" to (v.meaningVi ?: ""),
                        "partOfSpeech" to v.partOfSpeech,
                        "example" to v.example,
                        "exampleVi" to (v.exampleVi ?: ""),
                        "translation" to v.translation,
                        "audioUrl" to (v.audioUrl ?: ""),
                        "imageUrl" to (v.imageUrl ?: ""),
                        "order" to v.order
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 6. Grammars
            for (g in KidsCurriculumData.grammars) {
                firestore.collection("grammar").document(g.id).set(
                    mapOf(
                        "lessonId" to g.lessonId,
                        "title" to g.title,
                        "titleVi" to (g.titleVi ?: ""),
                        "explanation" to g.explanation,
                        "explanationVi" to (g.explanationVi ?: ""),
                        "examples" to g.examples,
                        "examplesVi" to (g.examplesVi ?: emptyList()),
                        "order" to g.order
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 7. Listenings
            for (lis in KidsCurriculumData.listenings) {
                firestore.collection("listening").document(lis.id).set(
                    mapOf(
                        "lessonId" to lis.lessonId,
                        "title" to lis.title,
                        "titleVi" to (lis.titleVi ?: ""),
                        "audioUrl" to (lis.audioUrl ?: ""),
                        "transcript" to lis.transcript,
                        "transcriptVi" to (lis.transcriptVi ?: ""),
                        "difficulty" to lis.difficulty,
                        "order" to lis.order
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 8. Pronunciations
            for (p in KidsCurriculumData.pronunciations) {
                firestore.collection("pronunciation").document(p.id).set(
                    mapOf(
                        "lessonId" to p.lessonId,
                        "word" to p.word,
                        "phonetic" to p.phonetic,
                        "audioUrl" to (p.audioUrl ?: ""),
                        "targetScore" to p.targetScore,
                        "instructionVi" to (p.instructionVi ?: ""),
                        "order" to p.order
                    ),
                    SetOptions.merge()
                ).await()
            }

            // 9. Quizzes & Grammar Quick Quizzes
            val allQuizzes = KidsCurriculumData.quizzes + KidsCurriculumData.grammarQuickQuizzes
            for (q in allQuizzes) {
                firestore.collection("quizzes").document(q.id).set(
                    mapOf(
                        "lessonId" to q.lessonId,
                        "type" to q.type,
                        "question" to q.question,
                        "questionVi" to (q.questionVi ?: ""),
                        "options" to q.options,
                        "correctAnswer" to q.correctAnswer,
                        "explanationVi" to (q.explanationVi ?: ""),
                        "xpReward" to q.xpReward,
                        "order" to q.order
                    ),
                    SetOptions.merge()
                ).await()
            }

            Result.success(
                SeedSummary(
                    levelsCount = 1,
                    unitsCount = KidsCurriculumData.units.size,
                    lessonsCount = KidsCurriculumData.lessons.size,
                    vocabularyCount = KidsCurriculumData.vocabularies.size,
                    grammarCount = KidsCurriculumData.grammars.size,
                    listeningCount = KidsCurriculumData.listenings.size,
                    pronunciationCount = KidsCurriculumData.pronunciations.size,
                    quizzesCount = allQuizzes.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
