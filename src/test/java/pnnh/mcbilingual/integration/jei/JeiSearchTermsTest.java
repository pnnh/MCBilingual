package pnnh.mcbilingual.integration.jei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;

class JeiSearchTermsTest {
    @Test
    void emptyExtraKeepsTheOriginalList() {
        List<String> names = List.of("apple");

        assertSame(names, JeiSearchTerms.merge(names, List.of()));
        assertSame(names, JeiSearchTerms.merge(names, null));
    }

    @Test
    void newTermsFollowExistingNames() {
        List<String> names = List.of("apple");

        assertEquals(List.of("apple", "苹果", "pingguo", "pg"), JeiSearchTerms.merge(names, List.of("苹果", "pingguo", "pg")));
    }

    @Test
    void duplicateTermsDoNotReplaceTheList() {
        List<String> names = List.of("apple", "pingguo");

        assertSame(names, JeiSearchTerms.merge(names, List.of("apple", "pingguo")));
    }
}
