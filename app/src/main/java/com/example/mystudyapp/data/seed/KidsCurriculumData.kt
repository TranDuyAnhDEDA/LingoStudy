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

object KidsCurriculumData {

    val program = LearningProgram(
        id = "program_kids",
        title = "English for Kids",
        titleVi = "Tiếng Anh cho trẻ em",
        description = "A colorful, interactive 10-unit English journey designed for kids with playful vocabulary, simple grammar, and fun activities.",
        descriptionVi = "Chương trình 10 bài học sinh động, trực quan dành riêng cho trẻ em với từ vựng gần gũi, ngữ pháp dễ hiểu và bài tập vui nhộn.",
        targetAudience = "Kids & Young Beginners (6-12 years old)",
        targetAudienceVi = "Trẻ em & Học sinh tiểu học mới bắt đầu",
        learningObjectives = listOf(
            "Build foundational English vocabulary for everyday life",
            "Master basic child-friendly sentence patterns",
            "Develop listening and speaking confidence through playful phonics",
            "Form natural English communication habits early"
        ),
        learningObjectivesVi = listOf(
            "Xây dựng vốn từ vựng cơ bản về các chủ đề gần gũi",
            "Nắm vững các mẫu câu đơn giản, dễ nhớ phù hợp lứa tuổi",
            "Rèn luyện kỹ năng nghe và phản xạ tiếng Anh tự tin",
            "Hình thành thói quen giao tiếp tiếng Anh tự nhiên ngay từ nhỏ"
        ),
        order = 2,
        isPublished = true,
        iconEmoji = "🧸",
        levelIds = listOf("level_kids_1"),
        isAvailable = true
    )

    val level = Level(
        id = "level_kids_1",
        programId = "program_kids",
        name = "Kids Starter: Foundations & Discovery",
        nameVi = "Khởi động: Nền tảng & Khám phá",
        description = "Step-by-step English journey for young learners across 10 exciting units.",
        descriptionVi = "Hành trình học tiếng Anh từng bước hào hứng dành cho trẻ em qua 10 Unit lý thú.",
        order = 1,
        xpRequired = 200
    )

    val units = listOf(
        Unit(
            id = "unit_kids_1",
            levelId = "level_kids_1",
            programId = "program_kids",
            title = "Unit 1: Greetings & Hello",
            titleVi = "Bài 1: Chào hỏi & Làm quen",
            description = "Say hello, introduce your name, and make new friendly friends.",
            descriptionVi = "Chào hỏi thân thiện, giới thiệu tên và làm quen với bạn bè mới.",
            order = 1,
            learningObjectives = listOf("Say hello and goodbye", "Introduce your name", "Ask how someone is"),
            learningObjectivesVi = listOf("Nói xin chào và tạm biệt", "Giới thiệu tên bản thân", "Hỏi thăm sức khỏe bạn bè"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_kids_2",
            levelId = "level_kids_1",
            programId = "program_kids",
            title = "Unit 2: Alphabet & Phonics",
            titleVi = "Bài 2: Bảng chữ cái & Đánh vần",
            description = "Learn letters A to Z, phonics sounds, and spelling fun words.",
            descriptionVi = "Làm quen với các chữ cái từ A đến Z, ngữ âm và đánh vần các từ ngữ vui nhộn.",
            order = 2,
            learningObjectives = listOf("Recognize letters A-Z", "Pronounce English phonics sounds", "Spell simple names and words"),
            learningObjectivesVi = listOf("Nhận diện các chữ cái A-Z", "Phát âm đúng ngữ âm tiếng Anh", "Đánh vần tên và từ đơn giản"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_kids_3",
            levelId = "level_kids_1",
            programId = "program_kids",
            title = "Unit 3: Numbers & Age",
            titleVi = "Bài 3: Số đếm, Tuổi & Sinh nhật",
            description = "Count 1 to 10, ask and tell your age, and celebrate birthdays.",
            descriptionVi = "Đếm số từ 1 đến 10, hỏi và nói về số tuổi, và đón sinh nhật thật vui.",
            order = 3,
            learningObjectives = listOf("Count numbers 1 to 10", "Ask and answer 'How old are you?'", "Use birthday expressions"),
            learningObjectivesVi = listOf("Đếm các số từ 1 đến 10", "Hỏi và trả lời tuổi của mình", "Sử dụng câu chúc mừng sinh nhật"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_kids_4",
            levelId = "level_kids_1",
            programId = "program_kids",
            title = "Unit 4: Colors & Shapes",
            titleVi = "Bài 4: Màu sắc & Hình khối",
            description = "Discover vivid colors, identify shapes, and describe colorful objects.",
            descriptionVi = "Khám phá thế giới màu sắc rực rỡ, nhận biết các hình khối và miêu tả đồ vật.",
            order = 4,
            learningObjectives = listOf("Identify basic and vibrant colors", "Name common geometric shapes", "Describe items with color adjectives"),
            learningObjectivesVi = listOf("Nhận biết các màu sắc cơ bản và sinh động", "Gọi tên các hình khối quen thuộc", "Miêu tả đồ vật kết hợp màu sắc"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_kids_5",
            levelId = "level_kids_1",
            programId = "program_kids",
            title = "Unit 5: My Sweet Family",
            titleVi = "Bài 5: Gia đình yêu thương",
            description = "Talk about your beloved family members, pets, and home.",
            descriptionVi = "Nói về các thành viên thân yêu trong gia đình, thú cưng và mái ấm.",
            order = 5,
            learningObjectives = listOf("Name immediate and extended family members", "Use 'This is my...' to introduce relatives", "Use personal pronouns He and She"),
            learningObjectivesVi = listOf("Gọi tên các thành viên trong gia đình", "Dùng 'This is my...' để giới thiệu người thân", "Sử dụng đại từ nhân xưng He và She"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_kids_6",
            levelId = "level_kids_1",
            programId = "program_kids",
            title = "Unit 6: Classroom & School",
            titleVi = "Bài 6: Lớp học & Đồ dùng học tập",
            description = "Discover classroom objects, follow teacher commands, and enjoy crafting.",
            descriptionVi = "Khám phá các đồ dùng học tập quen thuộc, nghe hiệu lệnh lớp học và giờ thủ công vui nhộn.",
            order = 6,
            learningObjectives = listOf("Identify school supplies", "Follow simple classroom instructions", "Use 'This is' and 'That is' for objects"),
            learningObjectivesVi = listOf("Nhận biết các đồ dùng học tập", "Lắng nghe và làm theo hiệu lệnh trong lớp", "Sử dụng 'This is' và 'That is' để chỉ đồ vật"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_kids_7",
            levelId = "level_kids_1",
            programId = "program_kids",
            title = "Unit 7: Animals & Nature",
            titleVi = "Bài 7: Động vật & Thiên nhiên",
            description = "Meet friendly farm animals, wild creatures, and discover the beauty of nature.",
            descriptionVi = "Làm quen với các bạn thú nuôi nông trại, động vật hoang dã và vẻ đẹp thiên nhiên.",
            order = 7,
            learningObjectives = listOf("Name domestic and farm animals", "Describe wild animals and their abilities", "Talk about natural elements like sun, sky, trees"),
            learningObjectivesVi = listOf("Gọi tên các con vật nuôi và nông trại", "Miêu tả muông thú hoang dã và khả năng của chúng", "Nói về các cảnh sắc thiên nhiên như mặt trời, mây, cây cối"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_kids_8",
            levelId = "level_kids_1",
            programId = "program_kids",
            title = "Unit 8: Food, Drinks & Likes",
            titleVi = "Bài 8: Đồ ăn, Thức uống & Sở thích",
            description = "Learn yummy fruits, everyday foods and drinks, and express what you like.",
            descriptionVi = "Khám phá các loại trái cây thơm ngon, món ăn đồ uống hằng ngày và nói món bé thích.",
            order = 8,
            learningObjectives = listOf("Identify yummy fruits and vegetables", "Talk about daily meals and drinks", "Use 'I like' and 'Do you like...?'"),
            learningObjectivesVi = listOf("Gọi tên các loại hoa quả thơm ngon", "Nói về đồ ăn và thức uống hằng ngày", "Bày tỏ sở thích với 'I like' và hỏi bạn bè 'Do you like...?'"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_kids_9",
            levelId = "level_kids_1",
            programId = "program_kids",
            title = "Unit 9: Daily Fun & Actions",
            titleVi = "Bài 9: Hoạt động hằng ngày & Giờ giấc",
            description = "Learn action verbs, morning routines, telling time, and bedtime habits.",
            descriptionVi = "Học các động từ vận động, thói quen buổi sáng, giờ giấc và thói quen trước khi đi ngủ.",
            order = 9,
            learningObjectives = listOf("Use dynamic action verbs", "Describe daily morning routines", "Tell basic time and say goodnight"),
            learningObjectivesVi = listOf("Sử dụng các động từ chỉ hành động vận động", "Miêu tả các thói quen buổi sáng hằng ngày", "Nói giờ giấc đơn giản và chúc ngủ ngon"),
            estimatedMinutes = 24
        ),
        Unit(
            id = "unit_kids_10",
            levelId = "level_kids_1",
            programId = "program_kids",
            title = "Unit 10: Little English Star",
            titleVi = "Bài 10: Giao tiếp tự tin & Ôn tập",
            description = "Use polite magic words, prepositions of place, and celebrate English success.",
            descriptionVi = "Sử dụng những từ ngữ phép thuật lịch sự, giới từ chỉ nơi chốn và tỏa sáng tự tin.",
            order = 10,
            learningObjectives = listOf("Master polite communication magic words", "Understand in, on, and under positions", "Review and confidently speak basic English"),
            learningObjectivesVi = listOf("Thành thạo những lời nói phép thuật lịch sự", "Hiểu và dùng đúng giới từ in, on, under", "Tự tin giao tiếp và trở thành Ngôi sao tiếng Anh nhí"),
            estimatedMinutes = 24
        )
    )

    val lessons = listOf(
        // Bài 1: Chào hỏi & Làm quen
        // Bài 2: Bảng chữ cái & Đánh vần
        // Bài 3: Số đếm, Tuổi & Sinh nhật
        // Bài 4: Màu sắc & Hình khối
        // Bài 5: Gia đình yêu thương
        // Bài 6: Lớp học & Đồ dùng học tập
        // Bài 7: Động vật & Thiên nhiên
        // Bài 8: Đồ ăn, Thức uống & Sở thích
        // Bài 9: Hoạt động hằng ngày & Giờ giấc
        // Bài 10: Giao tiếp tự tin & Ôn tập
        Lesson(
            id = "lesson_kids_1_1",
            unitId = "unit_kids_1",
            programId = "program_kids",
            title = "Friendly Greetings",
            titleVi = "Chào hỏi thân thiện",
            description = "Learn to say hello and goodbye at different times of the day.",
            descriptionVi = "Học cách chào hỏi và tạm biệt vào các thời điểm trong ngày.",
            order = 1,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_1_2",
            unitId = "unit_kids_1",
            programId = "program_kids",
            title = "What's Your Name?",
            titleVi = "Bạn tên là gì?",
            description = "Learn to state your name and ask your friends their names.",
            descriptionVi = "Học cách giới thiệu tên của mình và hỏi tên bạn mới.",
            order = 2,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_1_3",
            unitId = "unit_kids_1",
            programId = "program_kids",
            title = "How Are You Today?",
            titleVi = "Hôm nay bạn thế nào?",
            description = "Ask about wellbeing, express feelings, and say thank you.",
            descriptionVi = "Hỏi thăm sức khỏe, thể hiện cảm xúc vui vẻ và nói lời cảm ơn.",
            order = 3,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_2_1",
            unitId = "unit_kids_2",
            programId = "program_kids",
            title = "Letters A to G",
            titleVi = "Chữ cái từ A đến G",
            description = "Explore the first letters of the alphabet with adorable animal and object words.",
            descriptionVi = "Khám phá những chữ cái đầu tiên cùng các từ vựng đồ vật và con vật ngộ nghĩnh.",
            order = 1,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_2_2",
            unitId = "unit_kids_2",
            programId = "program_kids",
            title = "Letters H to N",
            titleVi = "Chữ cái từ H đến N",
            description = "Discover letters H through N and learn how to spell words.",
            descriptionVi = "Khám phá các chữ cái từ H đến N và học cách đánh vần từng chữ.",
            order = 2,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_2_3",
            unitId = "unit_kids_2",
            programId = "program_kids",
            title = "Letters O to Z",
            titleVi = "Chữ cái từ O đến Z",
            description = "Complete the alphabet with letters O to Z and celebrate your mastery.",
            descriptionVi = "Hoàn thành bảng chữ cái với các chữ cái từ O đến Z và chúc mừng bé.",
            order = 3,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_3_1",
            unitId = "unit_kids_3",
            programId = "program_kids",
            title = "Numbers 1 to 5",
            titleVi = "Số đếm từ 1 đến 5",
            description = "Learn numbers one to five and practice counting fingers and stars.",
            descriptionVi = "Làm quen với các số từ một đến năm và tập đếm ngón tay, ngôi sao.",
            order = 1,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_3_2",
            unitId = "unit_kids_3",
            programId = "program_kids",
            title = "Numbers 6 to 10",
            titleVi = "Số đếm từ 6 đến 10",
            description = "Count up to ten and learn to say how old you are.",
            descriptionVi = "Đếm tiếp các số lên đến mười và học cách nói tuổi của mình.",
            order = 2,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_3_3",
            unitId = "unit_kids_3",
            programId = "program_kids",
            title = "Happy Birthday Party!",
            titleVi = "Bữa tiệc sinh nhật vui vẻ",
            description = "Sing birthday songs, unwrap presents, and share tasty cake.",
            descriptionVi = "Hát bài hát chúc mừng sinh nhật, mở quà và chia sẻ bánh kem thơm ngon.",
            order = 3,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_4_1",
            unitId = "unit_kids_4",
            programId = "program_kids",
            title = "Bright Primary Colors",
            titleVi = "Những màu sắc tươi sáng",
            description = "Learn primary colors like red, blue, yellow, and green.",
            descriptionVi = "Làm quen với các màu cơ bản: đỏ, xanh da trời, vàng và xanh lá cây.",
            order = 1,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_4_2",
            unitId = "unit_kids_4",
            programId = "program_kids",
            title = "Rainbow Colors",
            titleVi = "Màu sắc cầu vồng",
            description = "Expand color words with pink, purple, orange, brown, black, and white.",
            descriptionVi = "Mở rộng thêm các màu sắc: hồng, tím, cam, nâu, đen và trắng tinh khôi.",
            order = 2,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_4_3",
            unitId = "unit_kids_4",
            programId = "program_kids",
            title = "Fun Geometric Shapes",
            titleVi = "Các hình khối vui nhộn",
            description = "Learn circle, square, triangle, star, and heart shapes in everyday life.",
            descriptionVi = "Làm quen với hình tròn, hình vuông, hình tam giác, ngôi sao và trái tim.",
            order = 3,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_5_1",
            unitId = "unit_kids_5",
            programId = "program_kids",
            title = "Mom, Dad and Me",
            titleVi = "Bố, mẹ và bé",
            description = "Introduce parents and express love for your sweet home.",
            descriptionVi = "Giới thiệu bố mẹ và bày tỏ tình yêu thương đối với mái ấm gia đình.",
            order = 1,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_5_2",
            unitId = "unit_kids_5",
            programId = "program_kids",
            title = "Brothers and Sisters",
            titleVi = "Anh, chị và em",
            description = "Learn words for siblings, playing together, and sharing joy.",
            descriptionVi = "Học từ vựng về anh chị em, cùng chơi đùa và chia sẻ niềm vui bên nhau.",
            order = 2,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_5_3",
            unitId = "unit_kids_5",
            programId = "program_kids",
            title = "Grandparents and Pets",
            titleVi = "Ông bà & Thú cưng",
            description = "Visit sweet grandparents and introduce beloved puppies and kittens.",
            descriptionVi = "Đến thăm ông bà hiền hậu và giới thiệu các bạn thú cưng cún con, mèo con.",
            order = 3,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_6_1",
            unitId = "unit_kids_6",
            programId = "program_kids",
            title = "In My Schoolbag",
            titleVi = "Trong cặp sách của bé",
            description = "Learn names of everyday school supplies like book, pen, pencil, and ruler.",
            descriptionVi = "Gọi tên các đồ dùng học tập hằng ngày như sách, bút, bút chì, thước kẻ.",
            order = 1,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_6_2",
            unitId = "unit_kids_6",
            programId = "program_kids",
            title = "Fun in the Classroom",
            titleVi = "Vui học trong lớp",
            description = "Meet the teacher, look at the board, and follow polite commands.",
            descriptionVi = "Gặp gỡ thầy cô, nhìn lên bảng và làm theo các hiệu lệnh lịch sự trong lớp.",
            order = 2,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_6_3",
            unitId = "unit_kids_6",
            programId = "program_kids",
            title = "Arts, Crafts and Fun",
            titleVi = "Giờ thủ công sáng tạo",
            description = "Cut with scissors, glue colorful paper, and create wonderful art.",
            descriptionVi = "Dùng kéo cắt giấy, dán hồ thủ công và sáng tạo nên những tác phẩm nghệ thuật.",
            order = 3,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_7_1",
            unitId = "unit_kids_7",
            programId = "program_kids",
            title = "On the Old Farm",
            titleVi = "Trên nông trại vui vẻ",
            description = "Meet friendly farm animals like ducks, cows, pigs, and sheep.",
            descriptionVi = "Gặp gỡ các con vật nông trại gần gũi: vịt, bò sữa, lợn con và chú cừu bông.",
            order = 1,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_7_2",
            unitId = "unit_kids_7",
            programId = "program_kids",
            title = "Wild Safari Animals",
            titleVi = "Thế giới muông thú hoang dã",
            description = "Meet lions, tigers, elephants, and monkeys and learn what they can do.",
            descriptionVi = "Gặp gỡ chúa sơn lâm sư tử, hổ, voi to lớn và khỉ tinh nghịch xem chúng biết làm gì.",
            order = 2,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_7_3",
            unitId = "unit_kids_7",
            programId = "program_kids",
            title = "Beautiful Nature",
            titleVi = "Thiên nhiên tươi đẹp quanh ta",
            description = "Admire blooming flowers, the shining sun, blue rivers, and twinkling stars.",
            descriptionVi = "Ngắm nhìn những bông hoa đua nở, mặt trời ấm áp, dòng sông xanh và ngàn vì sao.",
            order = 3,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_8_1",
            unitId = "unit_kids_8",
            programId = "program_kids",
            title = "Yummy Sweet Fruits",
            titleVi = "Trái cây ngọt lành",
            description = "Taste crunchy apples, sweet bananas, oranges, and juicy strawberries.",
            descriptionVi = "Thưởng thức những quả táo giòn, chuối ngọt, cam mọng nước và dâu tây đỏ tươi.",
            order = 1,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_8_2",
            unitId = "unit_kids_8",
            programId = "program_kids",
            title = "Healthy Meals and Drinks",
            titleVi = "Bữa ăn bổ dưỡng và đồ uống",
            description = "Learn words for milk, water, bread, rice, eggs, and chicken.",
            descriptionVi = "Làm quen với các món ăn thức uống quen thuộc: sữa, nước lọc, bánh mì, cơm, trứng và thịt gà.",
            order = 2,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_8_3",
            unitId = "unit_kids_8",
            programId = "program_kids",
            title = "Sweet Treats & Yummy Snacks",
            titleVi = "Món ngọt & Đồ ăn vặt yêu thích",
            description = "Enjoy treats like ice cream, cookies, cake, and learn to politely ask with 'please'.",
            descriptionVi = "Thưởng thức que kem mát lạnh, bánh quy giòn và học cách xin phép lịch sự với 'please'.",
            order = 3,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_9_1",
            unitId = "unit_kids_9",
            programId = "program_kids",
            title = "Action Verbs & Movement",
            titleVi = "Vận động cơ thể vui nhộn",
            description = "Run, jump, dance, sing, and move around with joyful energy.",
            descriptionVi = "Chạy nhảy, múa ca, bơi lội và vận động cơ thể tràn đầy năng lượng tươi vui.",
            order = 1,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_9_2",
            unitId = "unit_kids_9",
            programId = "program_kids",
            title = "Good Morning Habits",
            titleVi = "Thói quen buổi sáng tốt lành",
            description = "Wake up early, wash your face, brush your teeth, and get ready for school.",
            descriptionVi = "Thức dậy sớm, rửa mặt, đánh răng thơm tho và sẵn sàng đến trường.",
            order = 2,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_9_3",
            unitId = "unit_kids_9",
            programId = "program_kids",
            title = "Sweet Bedtime Dreams",
            titleVi = "Giờ đi ngủ & Giấc mơ ngọt ngào",
            description = "Eat dinner, take a warm bath, look at the clock, and wish everyone goodnight.",
            descriptionVi = "Ăn tối cùng gia đình, tắm nước ấm, nhìn đồng hồ và chúc mọi người ngủ ngon.",
            order = 3,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_10_1",
            unitId = "unit_kids_10",
            programId = "program_kids",
            title = "Polite Magic Words",
            titleVi = "Những lời nói phép thuật",
            description = "Say please, thank you, sorry, excuse me, and you're welcome with good manners.",
            descriptionVi = "Nói làm ơn, cảm ơn, xin lỗi, thứ lỗi và không có chi với sự lễ phép, lịch thiệp.",
            order = 1,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_10_2",
            unitId = "unit_kids_10",
            programId = "program_kids",
            title = "Where Is It?",
            titleVi = "Đồ vật ở đâu thế?",
            description = "Learn prepositions of place: in, on, and under to find things easily.",
            descriptionVi = "Học các giới từ chỉ vị trí quen thuộc: ở trong, ở trên và ở dưới để tìm đồ vật.",
            order = 2,
            estimatedMinutes = 8,
            xpReward = 30
        ),
        Lesson(
            id = "lesson_kids_10_3",
            unitId = "unit_kids_10",
            programId = "program_kids",
            title = "Little English Superstar!",
            titleVi = "Bé là Ngôi sao Tiếng Anh nhí!",
            description = "Speak English confidently, celebrate your achievements, and become a superstar.",
            descriptionVi = "Tự tin nói tiếng Anh, ăn mừng thành quả học tập và trở thành một ngôi sao sáng.",
            order = 3,
            estimatedMinutes = 8,
            xpReward = 30
        )
    )

    val vocabularies: List<Vocabulary> by lazy { KidsUnits1To5Data.vocabularies + KidsUnits6To10Data.vocabularies }
    val grammars: List<Grammar> by lazy { KidsUnits1To5Data.grammars + KidsUnits6To10Data.grammars }
    val grammarQuickQuizzes: List<Quiz> by lazy { KidsUnits1To5Data.grammarQuickQuizzes + KidsUnits6To10Data.grammarQuickQuizzes }
    val listenings: List<Listening> by lazy { KidsUnits1To5Data.listenings + KidsUnits6To10Data.listenings }
    val pronunciations: List<Pronunciation> by lazy { KidsUnits1To5Data.pronunciations + KidsUnits6To10Data.pronunciations }
    val quizzes: List<Quiz> by lazy { KidsUnits1To5Data.quizzes + KidsUnits6To10Data.quizzes }

    fun getQuickQuizzesByLesson(lessonId: String): List<Quiz> {
        return grammarQuickQuizzes.filter { it.lessonId == lessonId }
    }
}
