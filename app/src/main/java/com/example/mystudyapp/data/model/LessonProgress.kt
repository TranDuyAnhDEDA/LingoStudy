package com.example.mystudyapp.data.model

import com.google.firebase.Timestamp

data class LessonProgress(
    val lessonId: String,
    val progress: Int = 0,
    val completed: Boolean = false,
    val xpEarned: Int = 0,
    val lastAccessedAt: Timestamp? = null,
    val vocabularyCompleted: Boolean = false,
    val grammarCompleted: Boolean = false,
    val listeningCompleted: Boolean = false,
    val pronunciationCompleted: Boolean = false,
    val quizCompleted: Boolean = false,
    val lastVocabularyIndex: Int = 0,
    val lastGrammarIndex: Int = 0,
    val lastListeningIndex: Int = 0,
    val lastPronunciationIndex: Int = 0,
    val lastQuizIndex: Int = 0,
    val quizScore: Int = 0
) {
    val gameCompleted: Boolean
        get() = pronunciationCompleted
}
