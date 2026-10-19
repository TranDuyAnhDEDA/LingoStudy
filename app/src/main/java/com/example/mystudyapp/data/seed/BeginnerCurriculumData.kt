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
        titleVi = "Tiếng Anh cho người mới bắt đầu",
        description = "A structured, 10-unit curriculum taking you from foundational greetings to practical everyday communication.",
        descriptionVi = "Chương trình 10 bài học cấu trúc từ những câu chào hỏi cơ bản đến giao tiếp thực tế hàng ngày.",
        targetAudience = "Beginners & Elementary Learners",
        targetAudienceVi = "Người mới bắt đầu & Trình độ cơ bản",
        learningObjectives = listOf(
            "Master essential daily vocabulary and expressions",
            "Understand basic English grammar and sentence structures",
            "Develop practical listening skills for real-life scenarios",
            "Build clear pronunciation and speaking confidence"
        ),
        learningObjectivesVi = listOf(
            "Nắm vững từ vựng và câu diễn đạt hàng ngày",
            "Hiểu ngữ pháp và cấu trúc câu tiếng Anh cơ bản",
            "Phát triển kỹ năng nghe thực tế",
            "Xây dựng phát âm rõ ràng và tự tin khi nói"
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
        nameVi = "Beginner 1: Nền tảng & Giao tiếp",
        description = "Master everyday vocabulary, practical dialogues, and essential grammar across 10 structured units.",
        descriptionVi = "Thành thạo từ vựng hàng ngày, hội thoại thực tế và ngữ pháp thiết yếu qua 10 bài học.",
        order = 1,
        xpRequired = 200
    )

    val units = listOf(
        Unit(
            id = "unit_b1_1",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 1: Greetings & Introductions",
            titleVi = "Bài 1: Chào hỏi & Giới thiệu",
            description = "Learn to greet people, introduce yourself, and make friendly small talk.",
            descriptionVi = "Học cách chào hỏi, giới thiệu bản thân và trò chuyện thân mật.",
            order = 1,
            learningObjectives = listOf("Say hello and goodbye", "Introduce yourself", "Ask how someone is doing"),
            learningObjectivesVi = listOf("Nói xin chào và tạm biệt", "Giới thiệu bản thân", "Hỏi thăm sức khỏe"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_2",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 2: Personal Information & Countries",
            titleVi = "Bài 2: Thông tin cá nhân & Quốc gia",
            description = "Talk about where you are from, your nationality, age, and job.",
            descriptionVi = "Nói về nơi bạn đến, quốc tịch, tuổi và công việc.",
            order = 2,
            learningObjectives = listOf("State your country and nationality", "Describe occupations", "Share basic contact details"),
            learningObjectivesVi = listOf("Nói về quốc gia và quốc tịch", "Mô tả nghề nghiệp", "Chia sẻ thông tin liên lạc"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_3",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 3: Family & Relationships",
            titleVi = "Bài 3: Gia đình & Các mối quan hệ",
            description = "Describe your family members, friends, and personal relationships.",
            descriptionVi = "Mô tả các thành viên trong gia đình, bạn bè và các mối quan hệ.",
            order = 3,
            learningObjectives = listOf("Name family members", "Use possessive adjectives", "Describe appearance"),
            learningObjectivesVi = listOf("Kể tên thành viên gia đình", "Sử dụng tính từ sở hữu", "Mô tả ngoại hình"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_4",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 4: Daily Routines & Time",
            titleVi = "Bài 4: Thói quen hàng ngày & Thời gian",
            description = "Tell time and talk about your daily activities and schedules.",
            descriptionVi = "Nói về thời gian và các hoạt động, lịch trình hàng ngày.",
            order = 4,
            learningObjectives = listOf("Tell time in English", "Describe morning and evening habits", "Use frequency adverbs"),
            learningObjectivesVi = listOf("Nói giờ bằng tiếng Anh", "Mô tả thói quen buổi sáng và tối", "Sử dụng trạng từ chỉ tần suất"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_5",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 5: Food, Drinks & Dining",
            titleVi = "Bài 5: Đồ ăn, Thức uống & Ăn uống",
            description = "Order food at restaurants, name common dishes, and express food preferences.",
            descriptionVi = "Gọi món tại nhà hàng, gọi tên các món ăn thông dụng và bày tỏ sở thích ăn uống.",
            order = 5,
            learningObjectives = listOf("Name food and drinks", "Order politely at a cafe", "Express likes and dislikes"),
            learningObjectivesVi = listOf("Gọi tên đồ ăn và thức uống", "Gọi món lịch sự tại quán cafe", "Bày tỏ thích và không thích"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_6",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 6: Shopping & Money",
            titleVi = "Bài 6: Mua sắm & Tiền bạc",
            description = "Shop for clothes, ask about prices, and make payments in English.",
            descriptionVi = "Mua sắm quần áo, hỏi giá và thanh toán bằng tiếng Anh.",
            order = 6,
            learningObjectives = listOf("Name items of clothing", "Ask 'How much is this?'", "Understand prices and sizes"),
            learningObjectivesVi = listOf("Gọi tên trang phục", "Hỏi 'Cái này bao nhiêu tiền?'", "Hiểu giá cả và kích thước"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_7",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 7: Home & Living Spaces",
            titleVi = "Bài 7: Nhà cửa & Không gian sống",
            description = "Describe your home, rooms, furniture, and local neighborhood.",
            descriptionVi = "Mô tả nhà, phòng, đồ nội thất và khu phố của bạn.",
            order = 7,
            learningObjectives = listOf("Name rooms in a house", "Describe furniture using 'there is/are'", "Talk about local neighborhood places"),
            learningObjectivesVi = listOf("Gọi tên các phòng trong nhà", "Mô tả nội thất sử dụng 'there is/are'", "Nói về các địa điểm xung quanh"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_8",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 8: Places & Directions",
            titleVi = "Bài 8: Địa điểm & Phương hướng",
            description = "Ask for directions, navigate town, and understand prepositions of place.",
            descriptionVi = "Hỏi đường, tìm đường đi và hiểu các giới từ chỉ nơi chốn.",
            order = 8,
            learningObjectives = listOf("Ask for location of public places", "Understand directional phrases", "Use prepositions like next to, opposite"),
            learningObjectivesVi = listOf("Hỏi địa điểm công cộng", "Hiểu cụm từ chỉ phương hướng", "Sử dụng giới từ như next to, opposite"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_3_1",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 9: Travel & Tourism",
            titleVi = "Bài 9: Du lịch & Du lịch",
            description = "Essential English for airports, hotels, check-in, and tourism travel.",
            descriptionVi = "Tiếng Anh cần thiết tại sân bay, khách sạn, nhận phòng và đi du lịch.",
            order = 9,
            learningObjectives = listOf("Navigate airport check-in", "Check into a hotel", "Ask about transport schedules"),
            learningObjectivesVi = listOf("Thủ tục nhận phòng tại sân bay", "Nhận phòng khách sạn", "Hỏi về lịch trình phương tiện"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_b1_10",
            levelId = "level_beginner_1",
            programId = "program_beginner",
            title = "Unit 10: Social Plans & Course Review",
            titleVi = "Bài 10: Kế hoạch xã hội & Ôn tập",
            description = "Make social plans with friends, use polite requests, and consolidate your learning.",
            descriptionVi = "Lên kế hoạch gặp bạn bè, đưa ra yêu cầu lịch sự và củng cố kiến thức.",
            order = 10,
            learningObjectives = listOf("Make plans and invitations", "Use polite requests with 'would like'", "Complete comprehensive review"),
            learningObjectivesVi = listOf("Lên kế hoạch và mời gọi", "Sử dụng yêu cầu lịch sự với 'would like'", "Ôn tập toàn diện"),
            estimatedMinutes = 24
        )
    )

    val lessons = listOf(
        // Unit 1
        Lesson("lesson_b1_1_1", "unit_b1_1", "program_beginner", "Saying Hello & Goodbye", "Nói lời chào & tạm biệt", "Learn common formal and informal greetings and farewells.", "Học các câu chào hỏi và tạm biệt trang trọng và thông thường.", 1, 8, 30),
        Lesson("lesson_b1_1_2", "unit_b1_1", "program_beginner", "Introducing Yourself", "Giới thiệu bản thân", "State your name, age, and basic facts smoothly.", "Nêu tên, tuổi và thông tin cơ bản một cách tự nhiên.", 2, 8, 30),
        Lesson("lesson_b1_1_3", "unit_b1_1", "program_beginner", "Asking 'How Are You?'", "Hỏi thăm sức khỏe", "Inquire about someone's wellbeing and respond politely.", "Hỏi thăm sức khỏe và trả lời một cách lịch sự.", 3, 8, 30),

        // Unit 2
        Lesson("lesson_b1_2_1", "unit_b1_2", "program_beginner", "Where Are You From?", "Bạn đến từ đâu?", "Talk about your country of origin and nationality.", "Nói về quốc gia và quốc tịch của bạn.", 1, 8, 30),
        Lesson("lesson_b1_2_2", "unit_b1_2", "program_beginner", "Jobs & Occupations", "Nghề nghiệp", "Name common professions and talk about work.", "Kể tên các nghề thông dụng và nói về công việc.", 2, 8, 30),
        Lesson("lesson_b1_2_3", "unit_b1_2", "program_beginner", "Contact Details & Numbers", "Thông tin liên lạc & Số điện thoại", "Exchange phone numbers, email addresses, and personal details.", "Trao đổi số điện thoại, email và chi tiết cá nhân.", 3, 8, 30),

        // Unit 3
        Lesson("lesson_b1_3_1", "unit_b1_3", "program_beginner", "Family Members", "Thành viên gia đình", "Name immediate and extended family members.", "Kể tên các thành viên trong gia đình.", 1, 8, 30),
        Lesson("lesson_b1_3_2", "unit_b1_3", "program_beginner", "Describing Appearance", "Mô tả ngoại hình", "Use basic adjectives to describe height, hair, and style.", "Sử dụng tính từ cơ bản để mô tả chiều cao, mái tóc và phong cách.", 2, 8, 30),
        Lesson("lesson_b1_3_3", "unit_b1_3", "program_beginner", "Possessives & Relationships", "Sở hữu & Mối quan hệ", "Express ownership using 'my', 'your', 'his', 'her', and possessive 's.", "Thể hiện sự sở hữu sử dụng 'my', 'your', 'his', 'her', và 's.", 3, 8, 30),

        // Unit 4
        Lesson("lesson_b1_4_1", "unit_b1_4", "program_beginner", "Telling Time & Days", "Nói giờ & Ngày", "Ask and tell the time and days of the week.", "Hỏi và nói về thời gian và các ngày trong tuần.", 1, 8, 30),
        Lesson("lesson_b1_4_2", "unit_b1_4", "program_beginner", "Morning & Evening Habits", "Thói quen buổi sáng & tối", "Describe daily habits using simple present tense.", "Mô tả thói quen hàng ngày bằng thì hiện tại đơn.", 2, 8, 30),
        Lesson("lesson_b1_4_3", "unit_b1_4", "program_beginner", "Schedules & Frequency", "Lịch trình & Tần suất", "Talk about how often you do things using 'always', 'usually', 'never'.", "Nói về mức độ thường xuyên làm việc gì đó.", 3, 8, 30),

        // Unit 5
        Lesson("lesson_b1_5_1", "unit_b1_5", "program_beginner", "Food & Drink Basics", "Đồ ăn & Thức uống cơ bản", "Learn essential vocabulary for fruits, vegetables, and beverages.", "Học từ vựng cơ bản về trái cây, rau quả và thức uống.", 1, 8, 30),
        Lesson("lesson_b1_5_2", "unit_b1_5", "program_beginner", "Ordering at a Cafe", "Gọi món tại Cafe", "Politely order coffee, meals, and check the bill.", "Gọi đồ uống, đồ ăn và thanh toán hóa đơn một cách lịch sự.", 2, 8, 30),
        Lesson("lesson_b1_5_3", "unit_b1_5", "program_beginner", "Likes & Preferences", "Sở thích", "Express food preferences using 'like', 'love', and 'prefer'.", "Bày tỏ sở thích ăn uống sử dụng 'like', 'love', và 'prefer'.", 3, 8, 30),

        // Unit 6
        Lesson("lesson_b1_6_1", "unit_b1_6", "program_beginner", "Clothes & Colors", "Quần áo & Màu sắc", "Identify clothing items, colors, and styles.", "Nhận biết quần áo, màu sắc và phong cách.", 1, 8, 30),
        Lesson("lesson_b1_6_2", "unit_b1_6", "program_beginner", "Asking 'How Much Is This?'", "Hỏi giá cả", "Inquire about prices and store deals.", "Hỏi về giá cả và khuyến mãi.", 2, 8, 30),
        Lesson("lesson_b1_6_3", "unit_b1_6", "program_beginner", "Paying & Sizes", "Thanh toán & Kích cỡ", "Ask for small/medium/large sizes and pay cash or card.", "Hỏi kích cỡ và thanh toán bằng tiền mặt hoặc thẻ.", 3, 8, 30),

        // Unit 7
        Lesson("lesson_b1_7_1", "unit_b1_7", "program_beginner", "Rooms in a House", "Các phòng trong nhà", "Name living rooms, bedrooms, kitchens, and bathrooms.", "Gọi tên phòng khách, phòng ngủ, nhà bếp và phòng tắm.", 1, 8, 30),
        Lesson("lesson_b1_7_2", "unit_b1_7", "program_beginner", "Furniture & Objects", "Nội thất & Đồ vật", "Describe house objects using 'there is' and 'there are'.", "Mô tả các vật dụng trong nhà.", 2, 8, 30),
        Lesson("lesson_b1_7_3", "unit_b1_7", "program_beginner", "In the Neighborhood", "Trong khu phố", "Talk about local parks, supermarkets, and libraries.", "Nói về công viên, siêu thị và thư viện.", 3, 8, 30),

        // Unit 8
        Lesson("lesson_b1_8_1", "unit_b1_8", "program_beginner", "Prepositions of Place", "Giới từ chỉ nơi chốn", "Use 'next to', 'in front of', 'between', and 'behind'.", "Sử dụng các giới từ chỉ vị trí.", 1, 8, 30),
        Lesson("lesson_b1_8_2", "unit_b1_8", "program_beginner", "Asking for Directions", "Hỏi đường", "Ask polite questions when lost in a city.", "Hỏi đường một cách lịch sự khi bị lạc.", 2, 8, 30),
        Lesson("lesson_b1_8_3", "unit_b1_8", "program_beginner", "Giving Simple Directions", "Chỉ đường đơn giản", "Give simple instructions like 'turn left', 'go straight'.", "Chỉ đường đơn giản như 'rẽ trái', 'đi thẳng'.", 3, 8, 30),

        // Unit 9 (Preserving existing lesson IDs!)
        Lesson("lesson_3_1_1", "unit_3_1", "program_beginner", "At the Airport", "Tại Sân bay", "Learn useful English expressions used at the airport.", "Học các cụm từ tiếng Anh hữu ích tại sân bay.", 1, 8, 30),
        Lesson("lesson_3_1_2", "unit_3_1", "program_beginner", "At the Hotel", "Tại Khách sạn", "Learn English expressions for checking in and staying at a hotel.", "Học cách nhận phòng và lưu trú tại khách sạn.", 2, 8, 30),
        Lesson("lesson_3_1_3", "unit_3_1", "program_beginner", "Asking for Directions", "Hỏi đường", "Learn how to ask for and give directions during travel.", "Học cách hỏi và chỉ đường khi đi du lịch.", 3, 8, 30),

        // Unit 10
        Lesson("lesson_b1_10_1", "unit_b1_10", "program_beginner", "Making Social Plans", "Kế hoạch gặp gỡ", "Invite friends out using 'Let's...' and 'Do you want to...?'.", "Mời bạn bè đi chơi.", 1, 8, 30),
        Lesson("lesson_b1_10_2", "unit_b1_10", "program_beginner", "Polite Requests with 'Would Like'", "Yêu cầu lịch sự", "Use 'would like' to make requests smoothly.", "Sử dụng 'would like' để yêu cầu lịch sự.", 2, 8, 30),
        Lesson("lesson_b1_10_3", "unit_b1_10", "program_beginner", "Course Wrap-Up & Review", "Ôn tập tổng hợp", "Review essential vocabulary, grammar, and conversation skills.", "Ôn lại từ vựng, ngữ pháp và hội thoại.", 3, 8, 30)
    )

    // 10 Flashcard Vocabulary items for lesson_b1_1_1
    val sampleVocabularies = listOf(
        Vocabulary("v_1_1_1_1", "lesson_b1_1_1", "Hello", "xin chào", "xin chào", "interjection", "Hello! Nice to meet you.", "Xin chào! Rất vui được gặp bạn.", "Xin chào! Rất vui được gặp bạn.", "", "/həˈloʊ/", 1),
        Vocabulary("v_1_1_1_2", "lesson_b1_1_1", "Hi", "chào", "chào", "interjection", "Hi, how are you?", "Chào, bạn khỏe không?", "Chào, bạn khỏe không?", "", "/haɪ/", 2),
        Vocabulary("v_1_1_1_3", "lesson_b1_1_1", "Good morning", "chào buổi sáng", "chào buổi sáng", "phrase", "Good morning, class!", "Chào buổi sáng cả lớp!", "Chào buổi sáng cả lớp!", "", "/ɡʊd ˈmɔːrnɪŋ/", 3),
        Vocabulary("v_1_1_1_4", "lesson_b1_1_1", "Good afternoon", "chào buổi chiều", "chào buổi chiều", "phrase", "Good afternoon, sir!", "Chào buổi chiều, thưa ngài!", "Chào buổi chiều, thưa ngài!", "", "/ɡʊd ˌæftərˈnuːn/", 4),
        Vocabulary("v_1_1_1_5", "lesson_b1_1_1", "Good evening", "chào buổi tối", "chào buổi tối", "phrase", "Good evening! Welcome to our restaurant.", "Chào buổi tối! Chào mừng quý khách.", "Chào buổi tối! Chào mừng quý khách.", "", "/ɡʊd ˈiːvnɪŋ/", 5),
        Vocabulary("v_1_1_1_6", "lesson_b1_1_1", "Goodbye", "tạm biệt", "tạm biệt", "interjection", "Goodbye, see you tomorrow!", "Tạm biệt, hẹn gặp lại bạn ngày mai!", "Tạm biệt, hẹn gặp lại bạn ngày mai!", "", "/ɡʊdˈbaɪ/", 6),
        Vocabulary("v_1_1_1_7", "lesson_b1_1_1", "See you later", "hẹn gặp lại sau", "hẹn gặp lại sau", "phrase", "I have to go now. See you later!", "Tôi phải đi rồi. Hẹn gặp lại sau!", "Tôi phải đi rồi. Hẹn gặp lại sau!", "", "/siː juː ˈleɪtər/", 7),
        Vocabulary("v_1_1_1_8", "lesson_b1_1_1", "How are you?", "bạn khỏe không?", "bạn khỏe không?", "phrase", "Hello Sarah! How are you?", "Chào Sarah! Bạn khỏe không?", "Chào Sarah! Bạn khỏe không?", "", "/haʊ ɑːr juː/", 8),
        Vocabulary("v_1_1_1_9", "lesson_b1_1_1", "I'm fine, thank you.", "tôi khỏe, cảm ơn bạn", "tôi khỏe, cảm ơn bạn", "phrase", "I'm fine, thank you. And you?", "Tôi khỏe, cảm ơn bạn. Còn bạn thì sao?", "Tôi khỏe, cảm ơn bạn. Còn bạn thì sao?", "", "/aɪm faɪn θæŋk juː/", 9),
        Vocabulary("v_1_1_1_10", "lesson_b1_1_1", "Nice to meet you.", "rất vui được gặp bạn", "rất vui được gặp bạn", "phrase", "My name is David. Nice to meet you.", "Tên tôi là David. Rất vui được gặp bạn.", "Tên tôi là David. Rất vui được gặp bạn.", "", "/naɪs tə miːt juː/", 10)
    )

    // Grammar reading content for lesson_b1_1_1
    val sampleGrammars = listOf(
        Grammar(
            id = "g_1_1_1_1",
            lessonId = "lesson_b1_1_1",
            title = "Common Greeting Expressions",
            titleVi = "Các mẫu câu chào hỏi thông dụng",
            explanation = "Trong tiếng Anh, việc sử dụng lời chào phù hợp với từng thời điểm và đối tượng giao tiếp là rất quan trọng.",
            explanationVi = "1. CHÀO HỎI THEO THỜI ĐIỂM:\n- Good morning: Dùng từ sáng sớm đến 12h trưa (VD: Good morning, Sarah!).\n- Good afternoon: Dùng từ 12h trưa đến 6h tối (VD: Good afternoon, Mr. David!).\n- Good evening: Dùng từ 6h tối trở đi khi gặp mặt (VD: Good evening, everyone!).\n\n2. CHÀO HỎI THÂN MẬT:\n- Hello: Lời chào trang trọng và phổ biến nhất.\n- Hi: Lời chào thân mật dùng giữa bạn bè, người quen.\n\n3. HỎI THĂM SỨC KHỎE:\n- How are you?: Bạn khỏe không?\n- I'm fine, thank you.: Tôi khỏe, cảm ơn bạn.\n- And you?: Còn bạn thì sao?\n\n4. LÀM QUEN LẦN ĐẦU:\n- Nice to meet you.: Rất vui được gặp bạn.\n\n5. TẠM BIỆT:\n- Goodbye / Bye: Tạm biệt.\n- See you later: Hẹn gặp lại sau.",
            examples = listOf("Good morning, class!", "Hello! How are you?", "I'm fine, thank you. And you?", "Nice to meet you.", "Goodbye, see you later!"),
            examplesVi = listOf("Chào buổi sáng cả lớp!", "Xin chào! Bạn khỏe không?", "Tôi khỏe, cảm ơn bạn. Còn bạn?", "Rất vui được gặp bạn.", "Tạm biệt, hẹn gặp lại sau!"),
            order = 1
        )
    )

    // 10 Listening questions for lesson_b1_1_1
    val sampleListenings = listOf(
        Listening("l_1_1_1_1", "lesson_b1_1_1", "Hello", "Xin chào", "", "Hello! Nice to meet you.", "Xin chào! Rất vui được gặp bạn.", "beginner", 1),
        Listening("l_1_1_1_2", "lesson_b1_1_1", "Hi", "Chào", "", "Hi, how are you?", "Chào, bạn khỏe không?", "beginner", 2),
        Listening("l_1_1_1_3", "lesson_b1_1_1", "Good morning", "Chào buổi sáng", "", "Good morning, class!", "Chào buổi sáng cả lớp!", "beginner", 3),
        Listening("l_1_1_1_4", "lesson_b1_1_1", "Good afternoon", "Chào buổi chiều", "", "Good afternoon, sir!", "Chào buổi chiều, thưa ngài!", "beginner", 4),
        Listening("l_1_1_1_5", "lesson_b1_1_1", "Good evening", "Chào buổi tối", "", "Good evening, everyone!", "Chào buổi tối cả nhà!", "beginner", 5),
        Listening("l_1_1_1_6", "lesson_b1_1_1", "Goodbye", "Tạm biệt", "", "Goodbye, see you tomorrow!", "Tạm biệt, hẹn gặp lại bạn ngày mai!", "beginner", 6),
        Listening("l_1_1_1_7", "lesson_b1_1_1", "See you later", "Hẹn gặp lại sau", "", "I have to go now. See you later!", "Tôi phải đi rồi. Hẹn gặp lại sau!", "beginner", 7),
        Listening("l_1_1_1_8", "lesson_b1_1_1", "How are you?", "Bạn khỏe không?", "", "Hello Sarah! How are you?", "Chào Sarah! Bạn khỏe không?", "beginner", 8),
        Listening("l_1_1_1_9", "lesson_b1_1_1", "I'm fine, thank you.", "Tôi khỏe, cảm ơn bạn", "", "I'm fine, thank you. And you?", "Tôi khỏe, cảm ơn bạn. Còn bạn thì sao?", "beginner", 9),
        Listening("l_1_1_1_10", "lesson_b1_1_1", "Nice to meet you.", "Rất vui được gặp bạn", "", "My name is David. Nice to meet you.", "Tên tôi là David. Rất vui được gặp bạn.", "beginner", 10)
    )

    val samplePronunciations = listOf(
        Pronunciation("p_1_1_1_1", "lesson_b1_1_1", "hello", "/həˈloʊ/", "", 80, "Nghe và lặp lại từ 'hello'.", 1)
    )

    // 15 End-of-Lesson Quiz questions (8 Vocabulary + 7 Grammar)
    val sampleQuizzes = listOf(
        // Vocabulary Questions (1-8)
        Quiz("q_1_1_1_1", "lesson_b1_1_1", "multiple_choice", "Từ 'Hello' có nghĩa tiếng Việt là gì?", "Từ 'Hello' có nghĩa tiếng Việt là gì?", listOf("Xin chào", "Tạm biệt", "Cảm ơn", "Xin lỗi"), "Xin chào", "'Hello' là lời chào có nghĩa là 'Xin chào'.", 2, 1),
        Quiz("q_1_1_1_2", "lesson_b1_1_1", "multiple_choice", "Từ nào có nghĩa là 'Chào buổi sáng'?", "Từ nào có nghĩa là 'Chào buổi sáng'?", listOf("Good morning", "Good afternoon", "Good evening", "Good night"), "Good morning", "'Good morning' dùng để chào từ sáng sớm đến 12h trưa.", 2, 2),
        Quiz("q_1_1_1_3", "lesson_b1_1_1", "multiple_choice", "Từ nào có nghĩa là 'Chào buổi chiều'?", "Từ nào có nghĩa là 'Chào buổi chiều'?", listOf("Good afternoon", "Good morning", "Good evening", "Goodbye"), "Good afternoon", "'Good afternoon' dùng để chào từ 12h trưa đến 6h tối.", 2, 3),
        Quiz("q_1_1_1_4", "lesson_b1_1_1", "multiple_choice", "Cụm từ 'See you later' nghĩa là gì?", "Cụm từ 'See you later' nghĩa là gì?", listOf("Hẹn gặp lại sau", "Chào buổi sáng", "Bạn khỏe không?", "Tôi khỏe"), "Hẹn gặp lại sau", "'See you later' nghĩa là 'Hẹn gặp lại sau'.", 2, 4),
        Quiz("q_1_1_1_5", "lesson_b1_1_1", "multiple_choice", "Cụm từ nào dùng để hỏi thăm sức khỏe?", "Cụm từ nào dùng để hỏi thăm sức khỏe?", listOf("How are you?", "Nice to meet you", "Good evening", "Goodbye"), "How are you?", "'How are you?' có nghĩa là 'Bạn khỏe không?'.", 2, 5),
        Quiz("q_1_1_1_6", "lesson_b1_1_1", "multiple_choice", "Từ 'Goodbye' có nghĩa là gì?", "Từ 'Goodbye' có nghĩa là gì?", listOf("Tạm biệt", "Xin chào", "Cảm ơn", "Chào buổi chiều"), "Tạm biệt", "'Goodbye' có nghĩa là 'Tạm biệt'.", 2, 6),
        Quiz("q_1_1_1_7", "lesson_b1_1_1", "multiple_choice", "Cụm từ 'Nice to meet you.' dùng trong tình huống nào?", "Cụm từ 'Nice to meet you.' dùng trong tình huống nào?", listOf("Lần đầu làm quen", "Chào tạm biệt", "Hỏi đường", "Đi ngủ"), "Lần đầu làm quen", "'Nice to meet you' nghĩa là 'Rất vui được gặp bạn', dùng khi làm quen.", 2, 7),
        Quiz("q_1_1_1_8", "lesson_b1_1_1", "multiple_choice", "Cách trả lời 'Tôi khỏe, cảm ơn bạn' trong tiếng Anh là gì?", "Cách trả lời 'Tôi khỏe, cảm ơn bạn' trong tiếng Anh là gì?", listOf("I'm fine, thank you.", "Good morning.", "See you later.", "Nice to meet you."), "I'm fine, thank you.", "'I'm fine, thank you.' nghĩa là 'Tôi khỏe, cảm ơn bạn.'", 2, 8),

        // Grammar Questions (9-15)
        Quiz("q_1_1_1_9", "lesson_b1_1_1", "multiple_choice", "Khi gặp bạn bè lúc 8:00 sáng, lời chào phù hợp nhất là gì?", "Khi gặp bạn bè lúc 8:00 sáng, lời chào phù hợp nhất là gì?", listOf("Good morning", "Good afternoon", "Good evening", "Good night"), "Good morning", "'Good morning' dùng cho buổi sáng trước 12h trưa.", 2, 9),
        Quiz("q_1_1_1_10", "lesson_b1_1_1", "multiple_choice", "Khi gặp ai đó lúc 7:00 tối, bạn nên sử dụng câu chào nào?", "Khi gặp ai đó lúc 7:00 tối, bạn nên sử dụng câu chào nào?", listOf("Good evening", "Good morning", "Good afternoon", "Goodbye"), "Good evening", "'Good evening' dùng chào hỏi buổi tối từ 6h tối.", 2, 10),
        Quiz("q_1_1_1_11", "lesson_b1_1_1", "multiple_choice", "Khi người khác nói 'How are you?', phản hồi lịch sự nhất là gì?", "Khi người khác nói 'How are you?', phản hồi lịch sự nhất là gì?", listOf("I'm fine, thank you. And you?", "Goodbye!", "Good morning!", "Nice to meet you."), "I'm fine, thank you. And you?", "'I'm fine, thank you. And you?' vừa trả lời vừa hỏi lại lịch sự.", 2, 11),
        Quiz("q_1_1_1_12", "lesson_b1_1_1", "multiple_choice", "Khi vừa giới thiệu bản thân với một người bạn mới, bạn nên nói tiếp câu gì?", "Khi vừa giới thiệu bản thân với một người bạn mới, bạn nên nói tiếp câu gì?", listOf("Nice to meet you.", "Good evening.", "Goodbye.", "See you later."), "Nice to meet you.", "'Nice to meet you.' thể hiện sự thân thiện khi làm quen.", 2, 12),
        Quiz("q_1_1_1_13", "lesson_b1_1_1", "multiple_choice", "Lời chào 'Hi' khác với 'Hello' ở điểm nào?", "Lời chào 'Hi' khác với 'Hello' ở điểm nào?", listOf("Hi' thân mật hơn giữa bạn bè", "'Hi' chỉ dùng buổi sáng", "'Hi' chỉ dùng khi tạm biệt", "'Hi' dùng cho người lớn tuổi"), "Hi' thân mật hơn giữa bạn bè", "'Hi' là lời chào tự nhiên, thân mật giữa bạn bè.", 2, 13),
        Quiz("q_1_1_1_14", "lesson_b1_1_1", "multiple_choice", "Khi muốn kết thúc cuộc trò chuyện và hẹn gặp lại sau, bạn nên nói gì?", "Khi muốn kết thúc cuộc trò chuyện và hẹn gặp lại sau, bạn nên nói gì?", listOf("Goodbye, see you later!", "Good morning!", "How are you?", "Nice to meet you!"), "Goodbye, see you later!", "'Goodbye, see you later!' nghĩa là 'Tạm biệt, hẹn gặp lại sau!'", 2, 14),
        Quiz("q_1_1_1_15", "lesson_b1_1_1", "multiple_choice", "Khi ai đó nói 'Nice to meet you.', câu đáp lại chuẩn nhất là gì?", "Khi ai đó nói 'Nice to meet you.', câu đáp lại chuẩn nhất là gì?", listOf("Nice to meet you, too.", "Good afternoon.", "I'm fine, thank you.", "See you later."), "Nice to meet you, too.", "'Nice to meet you, too.' nghĩa là 'Tôi cũng rất vui được gặp bạn.'", 2, 15)
    )

    // 5 Grammar Quick Quiz Questions (for Grammar Reading Section)
    val sampleGrammarQuickQuizzes = listOf(
        Quiz("g_q_1", "lesson_b1_1_1", "grammar_quick", "Khi gặp người khác vào 8:00 sáng, câu chào đúng là gì?", "Khi gặp người khác vào 8:00 sáng, câu chào đúng là gì?", listOf("Good morning", "Good afternoon", "Good evening", "Goodbye"), "Good morning", "Good morning dùng để chào từ sáng sớm đến 12 giờ trưa.", 2, 1),
        Quiz("g_q_2", "lesson_b1_1_1", "grammar_quick", "Khi ai đó hỏi 'How are you?', câu trả lời lịch sự là gì?", "Khi ai đó hỏi 'How are you?', câu trả lời lịch sự là gì?", listOf("I'm fine, thank you.", "Goodbye!", "Good evening.", "Nice to meet you."), "I'm fine, thank you.", "I'm fine, thank you có nghĩa là 'Tôi khỏe, cảm ơn bạn'.", 2, 2),
        Quiz("g_q_3", "lesson_b1_1_1", "grammar_quick", "Khi lần đầu tiên gặp mặt người khác, bạn nên nói gì?", "Khi lần đầu tiên gặp mặt người khác, bạn nên nói gì?", listOf("Nice to meet you.", "Good night.", "See you later.", "Goodbye."), "Nice to meet you.", "Nice to meet you dùng khi làm quen lần đầu.", 2, 3),
        Quiz("g_q_4", "lesson_b1_1_1", "grammar_quick", "Câu chào nào thân mật thường dùng giữa bạn bè?", "Câu chào nào thân mật thường dùng giữa bạn bè?", listOf("Hi", "Good afternoon", "Good evening", "Good morning"), "Hi", "'Hi' là lời chào thân mật, tự nhiên giữa bạn bè.", 2, 4),
        Quiz("g_q_5", "lesson_b1_1_1", "grammar_quick", "Khi muốn chào tạm biệt và hẹn gặp lại sau, bạn nói gì?", "Khi muốn chào tạm biệt và hẹn gặp lại sau, bạn nói gì?", listOf("See you later", "Good morning", "How are you?", "Nice to meet you"), "See you later", "'See you later' nghĩa là 'Hẹn gặp lại sau'.", 2, 5)
    )
}
