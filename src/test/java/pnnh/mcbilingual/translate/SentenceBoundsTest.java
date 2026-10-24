package pnnh.mcbilingual.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SentenceBoundsTest {
    @Test
    void middleSentenceOfThree() {
        String text = "First one. Second one. Third one.";
        int offset = text.indexOf("Second");
        assertEquals("Second one.", SentenceBounds.sentenceAt(text, offset));
    }

    @Test
    void noPunctuationReturnsWhole() {
        assertEquals("hello world", SentenceBounds.sentenceAt("hello world", 3));
    }

    @Test
    void versionNumberDoesNotSplit() {
        String text = "Minecraft 1.21 rocks.";
        assertEquals("Minecraft 1.21 rocks.", SentenceBounds.sentenceAt(text, 0));
    }

    @Test
    void chinesePeriodSplits() {
        String text = "第一句。第二句。";
        assertEquals("第二句。", SentenceBounds.sentenceAt(text, text.indexOf("二")));
    }
}
