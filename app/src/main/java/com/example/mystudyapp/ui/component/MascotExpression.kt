package com.example.mystudyapp.ui.component

/**
 * Represents the visual expression state of the LingoFlow parrot mascot.
 *
 * All expressions currently resolve to the same base asset (mascot_parrot.png).
 * The enum is structured so that adding a new expression requires only:
 *   1. Adding a variant here.
 *   2. Adding the asset to drawable-nodpi/.
 *   3. Updating [MascotView.mascotDrawableFor].
 * No call-site changes are needed.
 *
 * Intended usage per screen:
 *   Home          -> HAPPY
 *   Vocabulary    -> HAPPY
 *   Grammar       -> THINKING
 *   Listening     -> HAPPY
 *   Pronunciation -> HAPPY
 *   Quiz          -> THINKING
 *   Result (pass) -> EXCITED
 *   Result (fail) -> NEUTRAL
 */
enum class MascotExpression {

    /** Default cheerful expression. Used on most screens. */
    HAPPY,

    /** Curious / pondering. Used on quiz and grammar screens. */
    THINKING,

    /** Celebrating / pumped. Used on correct-answer and result screens. */
    EXCITED,

    /** Calm / idle. Used on incorrect-answer or passive display. */
    NEUTRAL
}
