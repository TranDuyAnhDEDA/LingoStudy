package com.example.mystudyapp.data.seed

import com.example.mystudyapp.data.model.Grammar
import com.example.mystudyapp.data.model.LearningProgram
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.Level
import com.example.mystudyapp.data.model.Listening
import com.example.mystudyapp.data.model.Pronunciation
import com.example.mystudyapp.data.model.Quiz
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.model.Vocabulary

object BeginnerCurriculumData {

    val program = LearningProgram(
        id = "program_beginner",
        title = "Beginner English",
        description = "A structured, 10-unit curriculum taking you from foundational greetings to practical everyday communication.",
        targetAudience = "Beginners & Elementary Learners",
        learningObjectives = listOf(
            "Master essential daily vocabulary and expressions",
            "Understand basic English grammar and sentence structures",
            "Develop practical listening skills for real-life scenarios",
            "Build clear pronunciation and speaking confidence"
        ),
        order = 1,
        isPublished = true,
        iconEmoji = "🌱",
        levelIds = listOf("level_beginner_1"),
        isAvailable = true
    )

    val level = Level(
        id = "level_beginner_1",
        programId = "program_beginner",
        name = "Beginner 1: Foundations & Communication",
        description = "Master everyday vocabulary, practical dialogues, and essential grammar across 10 structured units.",
        order = 1,
        xpRequired = 900
    )

    val units = listOf(
        Unit(
            id = "unit_b1_1",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 1: Greetings & Introductions",
            description = "Learn to greet people, introduce yourself, and make friendly small talk.",
            order = 1,
            learningObjectives = listOf("Say hello and goodbye", "Introduce yourself", "Ask how someone is doing"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_2",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 2: Personal Information & Countries",
            description = "Talk about where you are from, your nationality, age, and job.",
            order = 2,
            learningObjectives = listOf("State your country and nationality", "Describe occupations", "Share basic contact details"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_3",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 3: Family & Relationships",
            description = "Describe your family members, friends, and personal relationships.",
            order = 3,
            learningObjectives = listOf("Name family members", "Use possessive adjectives", "Describe appearance"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_4",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 4: Daily Routines & Time",
            description = "Tell time and talk about your daily activities and schedules.",
            order = 4,
            learningObjectives = listOf("Tell time in English", "Describe morning and evening habits", "Use frequency adverbs"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_5",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 5: Food, Drinks & Dining",
            description = "Order food at restaurants, name common dishes, and express food preferences.",
            order = 5,
            learningObjectives = listOf("Name food and drinks", "Order politely at a cafe", "Express likes and dislikes"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_6",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 6: Shopping & Money",
            description = "Shop for clothes, ask about prices, and make payments in English.",
            order = 6,
            learningObjectives = listOf("Name items of clothing", "Ask 'How much is this?'", "Understand prices and sizes"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_7",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 7: Home & Living Spaces",
            description = "Describe your home, rooms, furniture, and local neighborhood.",
            order = 7,
            learningObjectives = listOf("Name rooms in a house", "Describe furniture using 'there is/are'", "Talk about local neighborhood places"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_8",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 8: Places & Directions",
            description = "Ask for directions, navigate town, and understand prepositions of place.",
            order = 8,
            learningObjectives = listOf("Ask for location of public places", "Understand directional phrases", "Use prepositions like next to, opposite"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_3_1", // Preserving existing Travel unit ID for backwards compatibility!
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 9: Travel & Tourism",
            description = "Essential English for airports, hotels, check-in, and tourism travel.",
            order = 9,
            learningObjectives = listOf("Navigate airport check-in", "Check into a hotel", "Ask about transport schedules"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_10",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 10: Social Plans & Course Review",
            description = "Make social plans with friends, use polite requests, and consolidate your learning.",
            order = 10,
            learningObjectives = listOf("Make plans and invitations", "Use polite requests with 'would like'", "Complete comprehensive review"),
            estimatedMinutes = 24
        )
    )

    val lessons = listOf(
        // Unit 1
        Lesson("lesson_b1_1_1", "unit_b1_1", "program_beginner", "Saying Hello & Goodbye", "Learn common formal and informal greetings and farewells.", 1, 8, 30),
        Lesson("lesson_b1_1_2", "unit_b1_1", "program_beginner", "Introducing Yourself", "State your name, age, and basic facts smoothly.", 2, 8, 30),
        Lesson("lesson_b1_1_3", "unit_b1_1", "program_beginner", "Asking 'How Are You?'", "Inquire about someone's wellbeing and respond politely.", 3, 8, 30),

        // Unit 2
        Lesson("lesson_b1_2_1", "unit_b1_2", "program_beginner", "Where Are You From?", "Talk about your country of origin and nationality.", 1, 8, 30),
        Lesson("lesson_b1_2_2", "unit_b1_2", "program_beginner", "Jobs & Occupations", "Name common professions and talk about work.", 2, 8, 30),
        Lesson("lesson_b1_2_3", "unit_b1_2", "program_beginner", "Contact Details & Numbers", "Exchange phone numbers, email addresses, and personal details.", 3, 8, 30),

        // Unit 3
        Lesson("lesson_b1_3_1", "unit_b1_3", "program_beginner", "Family Members", "Name immediate and extended family members.", 1, 8, 30),
        Lesson("lesson_b1_3_2", "unit_b1_3", "program_beginner", "Describing Appearance", "Use basic adjectives to describe height, hair, and style.", 2, 8, 30),
        Lesson("lesson_b1_3_3", "unit_b1_3", "program_beginner", "Possessives & Relationships", "Express ownership using 'my', 'your', 'his', 'her', and possessive 's.", 3, 8, 30),

        // Unit 4
        Lesson("lesson_b1_4_1", "unit_b1_4", "program_beginner", "Telling Time & Days", "Ask and tell the time and days of the week.", 1, 8, 30),
        Lesson("lesson_b1_4_2", "unit_b1_4", "program_beginner", "Morning & Evening Habits", "Describe daily habits using simple present tense.", 2, 8, 30),
        Lesson("lesson_b1_4_3", "unit_b1_4", "program_beginner", "Schedules & Frequency", "Talk about how often you do things using 'always', 'usually', 'never'.", 3, 8, 30),

        // Unit 5
        Lesson("lesson_b1_5_1", "unit_b1_5", "program_beginner", "Food & Drink Basics", "Learn essential vocabulary for fruits, vegetables, and beverages.", 1, 8, 30),
        Lesson("lesson_b1_5_2", "unit_b1_5", "program_beginner", "Ordering at a Cafe", "Politely order coffee, meals, and check the bill.", 2, 8, 30),
        Lesson("lesson_b1_5_3", "unit_b1_5", "program_beginner", "Likes & Preferences", "Express food preferences using 'like', 'love', and 'prefer'.", 3, 8, 30),

        // Unit 6
        Lesson("lesson_b1_6_1", "unit_b1_6", "program_beginner", "Clothes & Colors", "Identify clothing items, colors, and styles.", 1, 8, 30),
        Lesson("lesson_b1_6_2", "unit_b1_6", "program_beginner", "Asking 'How Much Is This?'", "Inquire about prices and store deals.", 2, 8, 30),
        Lesson("lesson_b1_6_3", "unit_b1_6", "program_beginner", "Paying & Sizes", "Ask for small/medium/large sizes and pay cash or card.", 3, 8, 30),

        // Unit 7
        Lesson("lesson_b1_7_1", "unit_b1_7", "program_beginner", "Rooms in a House", "Name living rooms, bedrooms, kitchens, and bathrooms.", 1, 8, 30),
        Lesson("lesson_b1_7_2", "unit_b1_7", "program_beginner", "Furniture & Objects", "Describe house objects using 'there is' and 'there are'.", 2, 8, 30),
        Lesson("lesson_b1_7_3", "unit_b1_7", "program_beginner", "In the Neighborhood", "Talk about local parks, supermarkets, and libraries.", 3, 8, 30),

        // Unit 8
        Lesson("lesson_b1_8_1", "unit_b1_8", "program_beginner", "Prepositions of Place", "Use 'next to', 'in front of', 'between', and 'behind'.", 1, 8, 30),
        Lesson("lesson_b1_8_2", "unit_b1_8", "program_beginner", "Asking for Directions", "Ask polite questions when lost in a city.", 2, 8, 30),
        Lesson("lesson_b1_8_3", "unit_b1_8", "program_beginner", "Giving Simple Directions", "Give simple instructions like 'turn left', 'go straight'.", 3, 8, 30),

        // Unit 9 (Preserving existing lesson IDs!)
        Lesson("lesson_3_1_1", "unit_3_1", "program_beginner", "At the Airport", "Learn useful English expressions used at the airport.", 1, 8, 30),
        Lesson("lesson_3_1_2", "unit_3_1", "program_beginner", "At the Hotel", "Learn English expressions for checking in and staying at a hotel.", 2, 8, 30),
        Lesson("lesson_3_1_3", "unit_3_1", "program_beginner", "Asking for Directions", "Learn how to ask for and give directions during travel.", 3, 8, 30),

        // Unit 10
        Lesson("lesson_b1_10_1", "unit_b1_10", "program_beginner", "Making Social Plans", "Invite friends out using 'Let's...' and 'Do you want to...?'.", 1, 8, 30),
        Lesson("lesson_b1_10_2", "unit_b1_10", "program_beginner", "Polite Requests with 'Would Like'", "Use 'would like' to make requests smoothly.", 2, 8, 30),
        Lesson("lesson_b1_10_3", "unit_b1_10", "program_beginner", "Course Wrap-Up & Review", "Review essential vocabulary, grammar, and conversation skills.", 3, 8, 30)
    )

    val sampleVocabularies = listOf(
        // Lesson b1_1_1
        Vocabulary("v_1_1_1_1", "lesson_b1_1_1", "hello", "xin chào", "interjection", "Hello! Nice to meet you.", "Xin chào! Rất vui được gặp bạn.", "", "", 1),
        Vocabulary("v_1_1_1_2", "lesson_b1_1_1", "goodbye", "tạm biệt", "interjection", "Goodbye, see you tomorrow!", "Tạm biệt, gặp lại bạn ngày mai!", "", "", 2),
        Vocabulary("v_1_1_1_3", "lesson_b1_1_1", "morning", "buổi sáng", "noun", "Good morning, class!", "Chào buổi sáng cả lớp!", "", "", 3),

        // Lesson b1_1_2
        Vocabulary("v_1_1_2_1", "lesson_b1_1_2", "name", "tên", "noun", "My name is Sarah.", "Tên tôi là Sarah.", "", "", 1),
        Vocabulary("v_1_1_2_2", "lesson_b1_1_2", "student", "học sinh", "noun", "I am an English student.", "Tôi là một học sinh tiếng Anh.", "", "", 2),

        // Lesson b1_2_1
        Vocabulary("v_1_2_1_1", "lesson_b1_2_1", "country", "đất nước", "noun", "Vietnam is a beautiful country.", "Việt Nam là một đất nước đẹp.", "", "", 1),
        Vocabulary("v_1_2_1_2", "lesson_b1_2_1", "nationality", "quốc tịch", "noun", "What is your nationality?", "Quốc tịch của bạn là gì?", "", "", 2)
    )

    val sampleGrammars = listOf(
        // Lesson b1_1_1
        Grammar("g_1_1_1_1", "lesson_b1_1_1", "Verb 'To Be' with Greetings", "Use 'I am' or 'My name is' when greeting others.", listOf("I am Alex.", "My name is John."), 1),
        // Lesson b1_2_1
        Grammar("g_1_2_1_1", "lesson_b1_2_1", "Preposition 'From' for Origin", "Use 'from' to state your hometown or origin country.", listOf("I am from Vietnam.", "She is from Japan."), 1)
    )

    val sampleListenings = listOf(
        Listening("l_1_1_1_1", "lesson_b1_1_1", "First Meeting Dialogue", "", "A: Hello! I am Alex.\nB: Hi Alex! I am Maria. Nice to meet you.", "beginner", 1),
        Listening("l_1_2_1_1", "lesson_b1_2_1", "Talking About Hometowns", "", "A: Where are you from?\nB: I am from Hanoi, Vietnam. And you?", "beginner", 1)
    )

    val samplePronunciations = listOf(
        Pronunciation("p_1_1_1_1", "lesson_b1_1_1", "hello", "/həˈloʊ/", "", 80, 1),
        Pronunciation("p_1_1_1_2", "lesson_b1_1_1", "welcome", "/ˈwel.kəm/", "", 80, 2)
    )

    val sampleQuizzes = listOf(
        Quiz("q_1_1_1_1", "lesson_b1_1_1", "multiple_choice", "How do you greet someone in the morning?", listOf("Good morning", "Good night", "Goodbye", "See you"), "Good morning", 10, 1),
        Quiz("q_1_1_1_2", "lesson_b1_1_1", "multiple_choice", "What do you say when leaving?", listOf("Goodbye", "Hello", "Welcome", "Good morning"), "Goodbye", 10, 2)
    )
}
