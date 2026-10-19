package com.example.mystudyapp.ui.screen.grammar

import com.example.mystudyapp.viewmodel.GrammarUiModel

enum class SlideCategory(
    val badgeText: String,
    val stepLabel: String,
    val iconKey: String
) {
    OVERVIEW("KHỞI ĐỘNG", "Bước 1 / 5", "overview"),
    FORMULA("CẤU TRÚC", "Bước 2 / 5", "formula"),
    USAGE("CÁCH DÙNG", "Bước 3 / 5", "usage"),
    EXAMPLES("VÍ DỤ & PHÁT ÂM", "Bước 4 / 5", "examples"),
    SUMMARY("TỔNG KẾT & MẸO", "Bước 5 / 5", "summary")
}

data class GrammarSlide(
    val slideIndex: Int, // 0..4
    val category: SlideCategory,
    val title: String,
    val subtitle: String,
    val overviewText: String = "",
    val keyPoints: List<String> = emptyList(),
    val formulas: List<GrammarFormulaItem> = emptyList(),
    val usageItems: List<GrammarUsageItem> = emptyList(),
    val examples: List<GrammarExampleItem> = emptyList(),
    val tips: List<String> = emptyList(),
    val commonMistakes: List<Pair<String, String>> = emptyList()
)

data class GrammarFormulaItem(
    val name: String,
    val pattern: String,
    val explanation: String
)

data class GrammarUsageItem(
    val title: String,
    val description: String,
    val bulletPoints: List<String> = emptyList()
)

data class GrammarExampleItem(
    val english: String,
    val vietnamese: String,
    val note: String? = null
)

object GrammarSlideGenerator {

    fun generateSlides(grammar: GrammarUiModel): List<GrammarSlide> {
        val title = grammar.title
        val rawExplanation = grammar.explanation
        val rawExamples = grammar.examples
        val rawExamplesVi = grammar.examplesVi

        val parsedSections = parseSections(rawExplanation)
        val formulas = extractFormulas(grammar, parsedSections)
        val usageItems = extractUsageItems(grammar, parsedSections)
        val exampleItems = buildExampleItems(rawExamples, rawExamplesVi)
        val (tips, mistakes) = extractTipsAndMistakes(grammar)

        // SLIDE 1: Khởi động & Khái niệm cốt lõi (Overview)
        val slide1 = GrammarSlide(
            slideIndex = 0,
            category = SlideCategory.OVERVIEW,
            title = title,
            subtitle = "Khái niệm cốt lõi & Ngữ cảnh sử dụng",
            overviewText = extractOverviewText(rawExplanation, title),
            keyPoints = listOf(
                "Nền tảng giao tiếp tiếng Anh quan trọng và thông dụng nhất.",
                "Giúp bạn tự tin diễn đạt chuẩn xác, tự nhiên như người bản xứ.",
                "Dễ học, trực quan và có thể ứng dụng ngay vào cuộc sống hàng ngày."
            )
        )

        // SLIDE 2: Cấu trúc & Công thức (Formulas & Patterns)
        val slide2 = GrammarSlide(
            slideIndex = 1,
            category = SlideCategory.FORMULA,
            title = "Cấu trúc & Mẫu câu chuẩn",
            subtitle = "Quy tắc ngữ pháp và cách thành lập câu",
            formulas = formulas
        )

        // SLIDE 3: Chi tiết cách dùng & Ngữ cảnh (Usage in Depth)
        val slide3 = GrammarSlide(
            slideIndex = 2,
            category = SlideCategory.USAGE,
            title = "Chi tiết cách dùng & Ngữ cảnh",
            subtitle = "Áp dụng chính xác vào từng tình huống thực tế",
            usageItems = usageItems
        )

        // SLIDE 4: Mẫu câu ví dụ & Luyện nghe (Examples & Audio)
        val slide4 = GrammarSlide(
            slideIndex = 3,
            category = SlideCategory.EXAMPLES,
            title = "Mẫu câu thực tế & Luyện nghe",
            subtitle = "Nghe phát âm chuẩn và quan sát cách dùng trong ngữ cảnh",
            examples = exampleItems
        )

        // SLIDE 5: Tổng kết, Mẹo nhớ & Sẵn sàng kiểm tra (Summary & Pro Tips)
        val slide5 = GrammarSlide(
            slideIndex = 4,
            category = SlideCategory.SUMMARY,
            title = "Ghi nhớ nhanh & Lưu ý quan trọng",
            subtitle = "Nắm chắc lý thuyết và sẵn sàng làm bài kiểm tra",
            tips = tips,
            commonMistakes = mistakes
        )

        return listOf(slide1, slide2, slide3, slide4, slide5)
    }

    private fun extractOverviewText(rawText: String, title: String): String {
        val firstPart = rawText.split(Regex("(?m)^\\s*1\\.\\s+")).firstOrNull()?.trim().orEmpty()
        return if (firstPart.isNotBlank() && firstPart.length > 20) {
            firstPart
        } else {
            "Trong tiếng Anh, việc sử dụng $title phù hợp với từng đối tượng và hoàn cảnh giao tiếp là cực kỳ quan trọng để tạo ấn tượng tự tin, thân thiện và lịch sự."
        }
    }

    private fun parseSections(rawText: String): List<Pair<String, String>> {
        val pattern = Regex("(?m)^(\\d+\\.[^\\n]+)")
        val matches = pattern.findAll(rawText).toList()
        if (matches.isEmpty()) return emptyList()

        val sections = mutableListOf<Pair<String, String>>()
        for (i in matches.indices) {
            val currentMatch = matches[i]
            val sectionHeader = currentMatch.value.trim()
            val startIdx = currentMatch.range.last + 1
            val endIdx = if (i + 1 < matches.size) matches[i + 1].range.first else rawText.length
            val content = rawText.substring(startIdx, endIdx).trim()
            sections.add(Pair(sectionHeader, content))
        }
        return sections
    }

    private fun extractFormulas(grammar: GrammarUiModel, sections: List<Pair<String, String>>): List<GrammarFormulaItem> {
        val titleLower = grammar.title.lowercase()
        return when {
            titleLower.contains("chào") || titleLower.contains("greet") -> {
                listOf(
                    GrammarFormulaItem(
                        name = "Chào hỏi theo buổi trong ngày",
                        pattern = "Good + [Morning / Afternoon / Evening]!",
                        explanation = "Dùng theo từng khung giờ: Morning (sáng - 12h trưa), Afternoon (12h - 18h tối), Evening (sau 18h tối)."
                    ),
                    GrammarFormulaItem(
                        name = "Hỏi thăm sức khỏe & Đáp lại",
                        pattern = "How are you? ➔ I am / I'm + [cảm xúc / tính từ], thank you.",
                        explanation = "Ví dụ: I am fine, thank you (Tôi khỏe, cảm ơn bạn). Thêm 'And you?' để hỏi lại lịch sự."
                    ),
                    GrammarFormulaItem(
                        name = "Làm quen lần đầu",
                        pattern = "Nice to meet you! ➔ Nice to meet you, too!",
                        explanation = "Dùng khi lần đầu gặp gỡ bạn mới hoặc đối tác. Nhớ thêm 'too' ở câu đáp lời."
                    )
                )
            }
            titleLower.contains("tên") || titleLower.contains("name") || titleLower.contains("giới thiệu") -> {
                listOf(
                    GrammarFormulaItem(
                        name = "Hỏi tên đối phương",
                        pattern = "What is your name? (What's your name?)",
                        explanation = "Câu hỏi tên thông dụng và lịch sự nhất trong tiếng Anh."
                    ),
                    GrammarFormulaItem(
                        name = "Tự giới thiệu tên mình",
                        pattern = "My name is + [Tên] / I am + [Tên]",
                        explanation = "Ví dụ: My name is Ben / I'm Anna. Cả 2 cách đều chính xác và tự nhiên."
                    ),
                    GrammarFormulaItem(
                        name = "Chào đón & Làm quen",
                        pattern = "Nice to meet you, [Tên]!",
                        explanation = "Rất vui được làm quen với bạn."
                    )
                )
            }
            titleLower.contains("to be") || titleLower.contains("động từ to be") -> {
                listOf(
                    GrammarFormulaItem(
                        name = "Thể khẳng định (+)",
                        pattern = "S + am / is / are + Danh từ / Tính từ",
                        explanation = "I đi với am; He/She/It đi với is; You/We/They đi với are."
                    ),
                    GrammarFormulaItem(
                        name = "Thể phủ định (-)",
                        pattern = "S + am / is / are + not + ...",
                        explanation = "Thêm 'not' ngay sau to be. Viết tắt: isn't, aren't."
                    ),
                    GrammarFormulaItem(
                        name = "Thể nghi vấn (?)",
                        pattern = "Am / Is / Are + S + ...?",
                        explanation = "Đảo động từ to be lên đầu câu. Trả lời: Yes, S + to be / No, S + to be + not."
                    )
                )
            }
            titleLower.contains("hiện tại đơn") || titleLower.contains("present simple") -> {
                listOf(
                    GrammarFormulaItem(
                        name = "Khẳng định (+)",
                        pattern = "S + V(s/es) + O",
                        explanation = "Chủ ngữ số ít (He/She/It) thì động từ thêm -s hoặc -es."
                    ),
                    GrammarFormulaItem(
                        name = "Phủ định (-)",
                        pattern = "S + do not / does not + V(nguyên thể)",
                        explanation = "I/You/We/They dùng don't; He/She/It dùng doesn't."
                    ),
                    GrammarFormulaItem(
                        name = "Nghi vấn (?)",
                        pattern = "Do / Does + S + V(nguyên thể)?",
                        explanation = "Đưa Do/Does lên đầu câu để hỏi. Động từ chính về dạng nguyên mẫu."
                    )
                )
            }
            else -> {
                // Synthesize from sections or default patterns
                if (sections.isNotEmpty()) {
                    sections.take(3).map { (secTitle, secContent) ->
                        val cleanTitle = secTitle.replace(Regex("^\\d+\\.\\s*"), "").trim()
                        val firstLine = secContent.lines().firstOrNull { it.isNotBlank() } ?: secContent
                        GrammarFormulaItem(
                            name = cleanTitle,
                            pattern = firstLine.replace(Regex("^-\\s*"), "").take(50),
                            explanation = secContent.take(120)
                        )
                    }
                } else {
                    listOf(
                        GrammarFormulaItem(
                            name = "Mẫu câu chuẩn",
                            pattern = grammar.examples.firstOrNull() ?: grammar.title,
                            explanation = grammar.explanation.take(100)
                        )
                    )
                }
            }
        }
    }

    private fun extractUsageItems(grammar: GrammarUiModel, sections: List<Pair<String, String>>): List<GrammarUsageItem> {
        if (sections.isNotEmpty()) {
            return sections.map { (secTitle, secContent) ->
                val cleanTitle = secTitle.replace(Regex("^\\d+\\.\\s*"), "").trim()
                val bullets = secContent.lines()
                    .map { it.trim() }
                    .filter { it.startsWith("-") || it.startsWith("•") || it.startsWith("*") }
                    .map { it.replace(Regex("^[-•*]\\s*"), "") }

                val desc = secContent.lines()
                    .filter { !it.trim().startsWith("-") && !it.trim().startsWith("•") && it.isNotBlank() }
                    .joinToString(" ")
                    .ifBlank { "Áp dụng theo từng tình huống cụ thể dưới đây:" }

                GrammarUsageItem(
                    title = cleanTitle,
                    description = desc,
                    bulletPoints = if (bullets.isNotEmpty()) bullets else listOf(secContent)
                )
            }
        }

        // Fallback usage items
        return listOf(
            GrammarUsageItem(
                title = "Giao tiếp hàng ngày (Daily Speaking)",
                description = "Sử dụng tự nhiên trong các cuộc hội thoại thân mật với bạn bè, người thân hoặc đồng nghiệp.",
                bulletPoints = listOf("Tạo cảm giác gần gũi, thoải mái", "Phù hợp với các tình huống giao tiếp đời thường")
            ),
            GrammarUsageItem(
                title = "Giao tiếp trang trọng & Lịch sự (Formal)",
                description = "Áp dụng trong lớp học, gặp gỡ đối tác, người lớn tuổi hoặc người lần đầu gặp mặt.",
                bulletPoints = listOf("Thể hiện sự tôn trọng và chuyên nghiệp", "Dùng từ ngữ đầy đủ, hạn chế viết tắt quá nhiều")
            )
        )
    }

    private fun buildExampleItems(examples: List<String>, examplesVi: List<String>): List<GrammarExampleItem> {
        if (examples.isEmpty()) {
            return listOf(
                GrammarExampleItem("Hello, how are you?", "Xin chào, bạn khỏe không?", "Mẫu câu chào hỏi cơ bản"),
                GrammarExampleItem("I'm fine, thank you. And you?", "Tôi khỏe, cảm ơn bạn. Còn bạn thì sao?", "Cách đáp lời lịch sự")
            )
        }

        return examples.mapIndexed { idx, en ->
            val vi = examplesVi.getOrNull(idx) ?: "Ví dụ minh họa ngữ pháp"
            GrammarExampleItem(english = en, vietnamese = vi)
        }
    }

    private fun extractTipsAndMistakes(grammar: GrammarUiModel): Pair<List<String>, List<Pair<String, String>>> {
        val titleLower = grammar.title.lowercase()
        return when {
            titleLower.contains("chào") || titleLower.contains("greet") -> {
                val tips = listOf(
                    "Luôn nhớ quy tắc mốc thời gian: Morning (sáng - 12h), Afternoon (12h - 18h), Evening (sau 18h).",
                    "Khi chào bạn bè thân thiết, có thể dùng 'Hi' hoặc 'Hey' ngắn gọn.",
                    "Một nụ cười thân thiện khi chào sẽ giúp cuộc trò chuyện trở nên ấm áp hơn rất nhiều!"
                )
                val mistakes = listOf(
                    Pair(
                        "Dùng 'Good night' để chào nhau khi gặp mặt vào ban đêm.",
                        "Chỉ dùng 'Good night' để chúc ngủ ngon hoặc chào tạm biệt khi ra về. Khi gặp mặt ban đêm hãy dùng 'Good evening'."
                    ),
                    Pair(
                        "Quên từ 'too' khi người khác nói 'Nice to meet you'.",
                        "Nên đáp lại đầy đủ: 'Nice to meet you, too!' để thể hiện phép lịch sự."
                    )
                )
                Pair(tips, mistakes)
            }
            titleLower.contains("tên") || titleLower.contains("name") -> {
                val tips = listOf(
                    "Trong tiếng Anh, bạn có thể nói 'I'm + Tên' hoặc 'My name is + Tên' đều hoàn toàn tự nhiên.",
                    "Lắng nghe kỹ tên của đối phương và phát âm lại tên bạn đó khi nói 'Nice to meet you, [Tên]!'."
                )
                val mistakes = listOf(
                    Pair(
                        "Nói 'My name Ben' (thiếu động từ to be).",
                        "Phải luôn có động từ: 'My name IS Ben' hoặc 'I AM Ben'."
                    ),
                    Pair(
                        "Hỏi 'What your name?' (thiếu 'is').",
                        "Phải là: 'What is your name?' hoặc viết tắt 'What's your name?'."
                    )
                )
                Pair(tips, mistakes)
            }
            else -> {
                val tips = listOf(
                    "Đọc to các câu ví dụ từ 2-3 lần để khẩu hình miệng và tai quen với ngữ điệu chuẩn.",
                    "Liên hệ điểm ngữ pháp này với một tình huống thực tế của chính bạn để nhớ lâu hơn.",
                    "Tự tin bước vào bài kiểm tra 5 câu: nếu chưa chắc chắn, bạn luôn có thể quay lại xem slide!"
                )
                val mistakes = listOf(
                    Pair(
                        "Dịch từng từ (word-by-word) từ tiếng Việt sang tiếng Anh.",
                        "Hãy học theo cả cụm mẫu câu chuẩn để tránh bị gượng gạo trong cách diễn đạt."
                    )
                )
                Pair(tips, mistakes)
            }
        }
    }
}
