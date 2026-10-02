package com.example.mystudyapp.data.seed

import com.example.mystudyapp.data.model.Grammar
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.Level
import com.example.mystudyapp.data.model.Listening
import com.example.mystudyapp.data.model.Pronunciation
import com.example.mystudyapp.data.model.Quiz
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.model.Vocabulary
import com.google.firebase.firestore.FirebaseFirestore
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
            // 1. Level
            val level = Level(
                id = "level_3",
                name = "Explorer",
                description = "Build confidence in everyday English.",
                order = 3,
                xpRequired = 600
            )
            firestore.collection("levels").document(level.id).set(
                mapOf(
                    "name" to level.name,
                    "description" to level.description,
                    "order" to level.order,
                    "xpRequired" to level.xpRequired
                )
            ).await()

            // 2. Unit
            val unit = Unit(
                id = "unit_3_1",
                levelId = "level_3",
                title = "Travel and Tourism",
                description = "Learn useful English for traveling and tourism.",
                order = 1,
                imageUrl = ""
            )
            firestore.collection("units").document(unit.id).set(
                mapOf(
                    "levelId" to unit.levelId,
                    "title" to unit.title,
                    "description" to unit.description,
                    "order" to unit.order,
                    "imageUrl" to (unit.imageUrl ?: "")
                )
            ).await()

            // 3. Lessons
            val lessons = listOf(
                Lesson(
                    id = "lesson_3_1_1",
                    unitId = "unit_3_1",
                    title = "At the Airport",
                    description = "Learn useful English expressions used at the airport.",
                    order = 1,
                    estimatedMinutes = 8,
                    xpReward = 30
                ),
                Lesson(
                    id = "lesson_3_1_2",
                    unitId = "unit_3_1",
                    title = "At the Hotel",
                    description = "Learn useful English expressions for checking in and staying at a hotel.",
                    order = 2,
                    estimatedMinutes = 8,
                    xpReward = 30
                ),
                Lesson(
                    id = "lesson_3_1_3",
                    unitId = "unit_3_1",
                    title = "Asking for Directions",
                    description = "Learn how to ask for and give directions.",
                    order = 3,
                    estimatedMinutes = 8,
                    xpReward = 30
                )
            )

            for (l in lessons) {
                firestore.collection("lessons").document(l.id).set(
                    mapOf(
                        "unitId" to l.unitId,
                        "title" to l.title,
                        "description" to l.description,
                        "order" to l.order,
                        "estimatedMinutes" to l.estimatedMinutes,
                        "xpReward" to l.xpReward
                    )
                ).await()
            }

            // 4. Vocabulary
            val vocabularyList = listOf(
                // Lesson 3.1.1
                Vocabulary("vocab_3_1_1_1", "lesson_3_1_1", "boarding pass", "thẻ lên máy bay", "noun", "Please show me your boarding pass.", "Vui lòng cho tôi xem thẻ lên máy bay.", "", ""),
                Vocabulary("vocab_3_1_1_2", "lesson_3_1_1", "passport", "hộ chiếu", "noun", "I need to show my passport.", "Tôi cần xuất trình hộ chiếu.", "", ""),
                Vocabulary("vocab_3_1_1_3", "lesson_3_1_1", "luggage", "hành lý", "noun", "Where can I collect my luggage?", "Tôi có thể lấy hành lý ở đâu?", "", ""),
                Vocabulary("vocab_3_1_1_4", "lesson_3_1_1", "departure", "sự khởi hành", "noun", "The departure time is 8 a.m.", "Thời gian khởi hành là 8 giờ sáng.", "", ""),
                Vocabulary("vocab_3_1_1_5", "lesson_3_1_1", "gate", "cổng", "noun", "Your gate is number 12.", "Cổng của bạn là số 12.", "", ""),

                // Lesson 3.1.2
                Vocabulary("vocab_3_1_2_1", "lesson_3_1_2", "reservation", "sự đặt chỗ trước", "noun", "I have a reservation under the name Smith.", "Tôi có một phòng đặt trước dưới tên Smith.", "", ""),
                Vocabulary("vocab_3_1_2_2", "lesson_3_1_2", "reception", "bàn lễ tân", "noun", "Please ask at the reception desk.", "Vui lòng hỏi tại bàn lễ tân.", "", ""),
                Vocabulary("vocab_3_1_2_3", "lesson_3_1_2", "check-in", "làm thủ tục nhận phòng", "noun", "Check-in time is at 2 p.m.", "Thời gian nhận phòng là 2 giờ chiều.", "", ""),

                // Lesson 3.1.3
                Vocabulary("vocab_3_1_3_1", "lesson_3_1_3", "straight", "thẳng", "adverb", "Go straight for two blocks.", "Đi thẳng hai dãy nhà.", "", ""),
                Vocabulary("vocab_3_1_3_2", "lesson_3_1_3", "left", "bên trái", "noun", "Turn left at the traffic light.", "Rẽ trái ở đèn giao thông.", "", ""),
                Vocabulary("vocab_3_1_3_3", "lesson_3_1_3", "right", "bên phải", "noun", "The bank is on the right.", "Ngân hàng nằm ở bên phải.", "", "")
            )

            for (v in vocabularyList) {
                firestore.collection("vocabulary").document(v.id).set(
                    mapOf(
                        "lessonId" to v.lessonId,
                        "word" to v.word,
                        "meaning" to v.meaning,
                        "partOfSpeech" to v.partOfSpeech,
                        "example" to v.example,
                        "translation" to v.translation,
                        "audioUrl" to (v.audioUrl ?: ""),
                        "imageUrl" to (v.imageUrl ?: "")
                    )
                ).await()
            }

            // 5. Grammar
            val grammarList = listOf(
                Grammar("grammar_3_1_1_1", "lesson_3_1_1", "Present Perfect", "Used to talk about experiences and actions connected to the present.", listOf("I have visited Japan.", "She has never flown before."), 1),
                Grammar("grammar_3_1_2_1", "lesson_3_1_2", "Polite Requests with 'Would like'", "Use 'would like' to make polite requests when checking in or ordering.", listOf("I would like to check in, please.", "Would you like a room with a view?"), 1),
                Grammar("grammar_3_1_3_1", "lesson_3_1_3", "Giving Directions with Imperatives", "Use base verbs (imperatives) to give clear directions.", listOf("Turn left at the corner.", "Go straight ahead."), 1)
            )

            for (g in grammarList) {
                firestore.collection("grammar").document(g.id).set(
                    mapOf(
                        "lessonId" to g.lessonId,
                        "title" to g.title,
                        "explanation" to g.explanation,
                        "examples" to g.examples,
                        "order" to g.order
                    )
                ).await()
            }

            // 6. Listening
            val listeningList = listOf(
                Listening("listening_3_1_1_1", "lesson_3_1_1", "At the Airport", "", "Good morning. May I see your passport? Here is my passport. Thank you. What is your destination?", "medium", 1)
            )

            for (lis in listeningList) {
                firestore.collection("listening").document(lis.id).set(
                    mapOf(
                        "lessonId" to lis.lessonId,
                        "title" to lis.title,
                        "audioUrl" to (lis.audioUrl ?: ""),
                        "transcript" to lis.transcript,
                        "difficulty" to lis.difficulty,
                        "order" to lis.order
                    )
                ).await()
            }

            // 7. Pronunciation
            val pronunciationList = listOf(
                Pronunciation("pron_3_1_1_1", "lesson_3_1_1", "airport", "/ˈeəpɔːt/", "", 80),
                Pronunciation("pron_3_1_1_2", "lesson_3_1_1", "boarding", "/ˈbɔːdɪŋ/", "", 80),
                Pronunciation("pron_3_1_1_3", "lesson_3_1_1", "passport", "/ˈpɑːspɔːt/", "", 80)
            )

            for (p in pronunciationList) {
                firestore.collection("pronunciation").document(p.id).set(
                    mapOf(
                        "lessonId" to p.lessonId,
                        "word" to p.word,
                        "phonetic" to p.phonetic,
                        "audioUrl" to (p.audioUrl ?: ""),
                        "targetScore" to p.targetScore
                    )
                ).await()
            }

            // 8. Quizzes
            val quizList = listOf(
                // Lesson 3.1.1
                Quiz("quiz_3_1_1_1", "lesson_3_1_1", "multiple_choice", "What do you need before boarding the airplane?", listOf("Boarding pass", "Hotel key", "Taxi ticket", "Map"), "Boarding pass", 10),
                Quiz("quiz_3_1_1_2", "lesson_3_1_1", "multiple_choice", "Where do you collect your bags after a flight?", listOf("Luggage area", "Departure gate", "Hotel reception", "Security check"), "Luggage area", 10),
                Quiz("quiz_3_1_1_3", "lesson_3_1_1", "multiple_choice", "Which sentence uses the Present Perfect correctly?", listOf("I have visited Japan.", "I visited Japan tomorrow.", "I visiting Japan now.", "I has visited Japan."), "I have visited Japan.", 10),
                Quiz("quiz_3_1_1_4", "lesson_3_1_1", "multiple_choice", "What document proves your identity when traveling internationally?", listOf("Passport", "Library card", "Business card", "Receipt"), "Passport", 10),
                Quiz("quiz_3_1_1_5", "lesson_3_1_1", "multiple_choice", "What does 'departure' mean?", listOf("The act of leaving/starting a journey", "Checking into a hotel", "Buying food", "Landing at home"), "The act of leaving/starting a journey", 10),

                // Lesson 3.1.2
                Quiz("quiz_3_1_2_1", "lesson_3_1_2", "multiple_choice", "What do you do at the hotel desk when you arrive?", listOf("Check-in", "Boarding", "Departure", "Security check"), "Check-in", 10),

                // Lesson 3.1.3
                Quiz("quiz_3_1_3_1", "lesson_3_1_3", "multiple_choice", "How do you tell someone to not turn, but keep going?", listOf("Go straight", "Turn right", "Turn left", "Stop here"), "Go straight", 10)
            )

            for (q in quizList) {
                firestore.collection("quizzes").document(q.id).set(
                    mapOf(
                        "lessonId" to q.lessonId,
                        "type" to q.type,
                        "question" to q.question,
                        "options" to q.options,
                        "correctAnswer" to q.correctAnswer,
                        "xpReward" to q.xpReward
                    )
                ).await()
            }

            Result.success(
                SeedSummary(
                    levelsCount = 1,
                    unitsCount = 1,
                    lessonsCount = lessons.size,
                    vocabularyCount = vocabularyList.size,
                    grammarCount = grammarList.size,
                    listeningCount = listeningList.size,
                    pronunciationCount = pronunciationList.size,
                    quizzesCount = quizList.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
