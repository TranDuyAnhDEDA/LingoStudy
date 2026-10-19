package com.example.mystudyapp

import com.example.mystudyapp.ui.screen.grammar.GrammarSlideGenerator
import com.example.mystudyapp.ui.screen.grammar.SlideCategory
import com.example.mystudyapp.viewmodel.GrammarUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GrammarSlideTest {

    @Test
    fun testGenerateSlidesReturnsExactlyFivePedagogicalSlides() {
        val grammar = GrammarUiModel(
            id = "g_test_1",
            title = "Lời chào hỏi tiếng Anh (Greetings)",
            explanation = "1. Chào hỏi buổi sáng: Good morning.\n2. Chào hỏi buổi chiều: Good afternoon.\n3. Chào hỏi buổi tối: Good evening.",
            examples = listOf("Good morning, teacher!", "How are you today?", "Nice to meet you!"),
            examplesVi = listOf("Chào buổi sáng cô giáo!", "Hôm nay bạn thế nào?", "Rất vui được gặp bạn!"),
            order = 1
        )

        val slides = GrammarSlideGenerator.generateSlides(grammar)

        assertEquals("Every grammar topic must generate exactly 5 slides", 5, slides.size)

        // Slide 1: OVERVIEW
        assertEquals(0, slides[0].slideIndex)
        assertEquals(SlideCategory.OVERVIEW, slides[0].category)
        assertTrue("Slide 1 must have overview text", slides[0].overviewText.isNotBlank())
        assertTrue("Slide 1 must have key points", slides[0].keyPoints.isNotEmpty())

        // Slide 2: FORMULA
        assertEquals(1, slides[1].slideIndex)
        assertEquals(SlideCategory.FORMULA, slides[1].category)
        assertTrue("Slide 2 must have formulas", slides[1].formulas.isNotEmpty())
        slides[1].formulas.forEach { formula ->
            assertTrue("Formula pattern must not be blank", formula.pattern.isNotBlank())
            assertTrue("Formula explanation must not be blank", formula.explanation.isNotBlank())
        }

        // Slide 3: USAGE
        assertEquals(2, slides[2].slideIndex)
        assertEquals(SlideCategory.USAGE, slides[2].category)
        assertTrue("Slide 3 must have usage items", slides[2].usageItems.isNotEmpty())
        slides[2].usageItems.forEach { usage ->
            assertTrue("Usage title must not be blank", usage.title.isNotBlank())
            assertTrue("Usage description must not be blank", usage.description.isNotBlank())
        }

        // Slide 4: EXAMPLES
        assertEquals(3, slides[3].slideIndex)
        assertEquals(SlideCategory.EXAMPLES, slides[3].category)
        assertEquals("Slide 4 should contain all examples", 3, slides[3].examples.size)
        assertEquals("Good morning, teacher!", slides[3].examples[0].english)
        assertEquals("Chào buổi sáng cô giáo!", slides[3].examples[0].vietnamese)

        // Slide 5: SUMMARY
        assertEquals(4, slides[4].slideIndex)
        assertEquals(SlideCategory.SUMMARY, slides[4].category)
        assertTrue("Slide 5 must have tips", slides[4].tips.isNotEmpty())
    }

    @Test
    fun testGenerateSlidesForVerbToBe() {
        val tobeGrammar = GrammarUiModel(
            id = "g_tobe",
            title = "Động từ To Be trong thì hiện tại",
            explanation = "Động từ to be gồm: am, is, are. Dùng để diễn tả trạng thái, tính chất hoặc danh tính của chủ ngữ.",
            examples = listOf("I am a student.", "She is kind.", "They are happy."),
            examplesVi = listOf("Tôi là học sinh.", "Cô ấy tốt bụng.", "Họ rất vui vẻ."),
            order = 1
        )

        val slides = GrammarSlideGenerator.generateSlides(tobeGrammar)
        assertEquals(5, slides.size)

        val formulaSlide = slides[1]
        assertTrue(formulaSlide.formulas.any { it.pattern.contains("am / is / are") })

        val exampleSlide = slides[3]
        assertEquals(3, exampleSlide.examples.size)
        assertEquals("I am a student.", exampleSlide.examples[0].english)
    }

    @Test
    fun testGenerateSlidesForGenericGrammarWithoutSections() {
        val genericGrammar = GrammarUiModel(
            id = "g_custom",
            title = "Mạo từ A, An, The",
            explanation = "Mạo từ đứng trước danh từ để chỉ đối tượng xác định hoặc bất định.",
            examples = emptyList(),
            examplesVi = emptyList(),
            order = 1
        )

        val slides = GrammarSlideGenerator.generateSlides(genericGrammar)
        assertEquals("Fallback must still produce exactly 5 slides", 5, slides.size)

        // Verifying all slides are populated
        for (i in 0 until 5) {
            val slide = slides[i]
            assertEquals(i, slide.slideIndex)
            assertNotNull(slide.title)
            assertNotNull(slide.category)
        }

        // Check fallback examples exist
        assertTrue("Slide 4 fallback must provide examples", slides[3].examples.isNotEmpty())
    }

    @Test
    fun testSlideBadgeTextsHaveNoEmojis() {
        SlideCategory.entries.forEach { category ->
            assertFalse(
                "Category ${category.name} badgeText should not contain emojis",
                category.badgeText.contains("🌱") ||
                    category.badgeText.contains("📐") ||
                    category.badgeText.contains("🎯") ||
                    category.badgeText.contains("💬") ||
                    category.badgeText.contains("💡")
            )
            assertTrue("Step label must follow 'Bước X / 5' pattern", category.stepLabel.startsWith("Bước"))
        }
    }
}
