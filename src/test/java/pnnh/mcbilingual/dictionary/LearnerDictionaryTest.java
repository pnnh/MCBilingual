package pnnh.mcbilingual.dictionary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class LearnerDictionaryTest {
    @Test
    void findsGlossIgnoringCase() throws Exception {
        LearnerDictionary dictionary = LearnerDictionary.load(new ByteArrayInputStream("""
                # comment
                Settings\t设置
                apple\t苹果
                """.getBytes(StandardCharsets.UTF_8)));

        assertEquals(Optional.of("设置"), dictionary.lookup("settings"));
        assertEquals(Optional.of("设置"), dictionary.lookup("SETTINGS"));
        assertEquals(Optional.of("苹果"), dictionary.lookup("Apple"));
    }

    @Test
    void missingWordIsEmpty() throws Exception {
        LearnerDictionary dictionary = LearnerDictionary.load(new ByteArrayInputStream(
                "only\t仅\n".getBytes(StandardCharsets.UTF_8)));

        assertTrue(dictionary.lookup("missing").isEmpty());
        assertTrue(dictionary.lookup(" ").isEmpty());
    }
}
