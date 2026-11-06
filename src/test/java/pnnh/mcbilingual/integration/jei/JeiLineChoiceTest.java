package pnnh.mcbilingual.integration.jei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import pnnh.mcbilingual.integration.jei.JeiListElementShape.Member;

class JeiLineChoiceTest {
    @Test
    void versionSelectsTheLegacyMixinForBetterMc() {
        JeiLineChoice.Choice choice = choose("19.27.0.343", JeiVersions.LEGACY_DESCRIPTOR);

        assertEquals(JeiVersions.Line.LEGACY, choice.line());
        assertEquals(JeiLineChoice.Source.VERSION, choice.source());
        assertTrue(JeiLineChoice.applies(choice.line(), "pnnh.mcbilingual.integration.jei.mixin.JeiListElementInfoLegacyMixin"));
        assertFalse(JeiLineChoice.applies(choice.line(), "pnnh.mcbilingual.integration.jei.mixin.JeiListElementInfoMixin"));
        String message = JeiLineChoice.message("19.27.0.343", choice);
        assertTrue(message.contains("19.27.0.343"));
        assertTrue(message.contains("19.56 之前的三参数构造函数"));
        assertTrue(message.contains("three-argument constructor from before 19.56"));
    }

    @Test
    void versionSelectsTheFilterMixinFrom19_56() {
        JeiLineChoice.Choice choice = choose("19.57.0.446", JeiVersions.FILTER_CONFIG_DESCRIPTOR);

        assertEquals(JeiVersions.Line.FILTER_CONFIG, choice.line());
        assertEquals(JeiLineChoice.Source.VERSION, choice.source());
        assertTrue(JeiLineChoice.applies(choice.line(), "pnnh.mcbilingual.integration.jei.mixin.JeiListElementInfoMixin"));
        assertFalse(JeiLineChoice.applies(choice.line(), "pnnh.mcbilingual.integration.jei.mixin.JeiListElementInfoLegacyMixin"));
        String message = JeiLineChoice.message("19.57.0.446", choice);
        assertTrue(message.contains("19.56 起带 IIngredientFilterConfig 的四参数构造函数"));
        assertTrue(message.contains("four-argument IIngredientFilterConfig constructor from 19.56 onward"));
    }

    @Test
    void classFileOverridesAVersionThatNamesTheMissingConstructor() {
        JeiLineChoice.Choice choice = choose("19.27.0.343", JeiVersions.FILTER_CONFIG_DESCRIPTOR);

        assertEquals(JeiVersions.Line.FILTER_CONFIG, choice.line());
        assertEquals(JeiVersions.Line.LEGACY, choice.parsed());
        assertEquals(JeiLineChoice.Source.BYTECODE, choice.source());
        assertTrue(JeiLineChoice.message("19.27.0.343", choice).contains("版本号与类文件不一致"));
    }

    @Test
    void unparsedVersionFollowsTheOnlyKnownConstructor() {
        JeiLineChoice.Choice choice = choose("not-a-version", JeiVersions.LEGACY_DESCRIPTOR);

        assertEquals(JeiVersions.Line.LEGACY, choice.line());
        assertEquals(JeiLineChoice.Source.BYTECODE, choice.source());
        assertTrue(JeiLineChoice.message("not-a-version", choice).contains("版本号无法按 19.x 解析"));
    }

    @Test
    void unparsedVersionWithBothConstructorsIsSkipped() {
        JeiListElementShape.Shape shape = JeiListElementShape.scan(fields(), List.of(
                new Member("<init>", JeiVersions.LEGACY_DESCRIPTOR),
                new Member("<init>", JeiVersions.FILTER_CONFIG_DESCRIPTOR)));

        JeiLineChoice.Choice choice = JeiLineChoice.choose(JeiVersions.Line.UNKNOWN, shape);

        assertFalse(choice.applies());
        assertTrue(JeiLineChoice.message("26.1.0", choice).contains("客户端会继续启动"));
    }

    @Test
    void parsedVersionWithNeitherConstructorIsSkipped() {
        JeiLineChoice.Choice choice = choose("19.60.0.450", "()V");

        assertFalse(choice.applies());
    }

    @Test
    void missingNamesFieldSkipsEvenWhenTheConstructorMatches() {
        JeiListElementShape.Shape shape = JeiListElementShape.scan(
                List.of(new Member(JeiListElementShape.ELEMENT, JeiListElementShape.ELEMENT_DESCRIPTOR)),
                List.of(new Member("<init>", JeiVersions.FILTER_CONFIG_DESCRIPTOR)));

        JeiLineChoice.Choice choice = JeiLineChoice.choose(JeiVersions.Line.FILTER_CONFIG, shape);

        assertFalse(choice.applies());
        assertTrue(choice.detail().contains("namesField=false"));
    }

    @Test
    void unreadClassTrustsAParsedVersion() {
        JeiLineChoice.Choice choice = JeiLineChoice.choose(JeiVersions.classify("19.56.0.438"), null);

        assertEquals(JeiVersions.Line.FILTER_CONFIG, choice.line());
        assertEquals(JeiLineChoice.Source.UNVERIFIED, choice.source());
        assertTrue(JeiLineChoice.message("19.56.0.438", choice).contains("读不到 ListElementInfo"));
    }

    @Test
    void unreadClassAndUnknownVersionSkips() {
        JeiLineChoice.Choice choice = JeiLineChoice.choose(JeiVersions.Line.UNKNOWN, null);

        assertFalse(choice.applies());
        assertEquals(JeiLineChoice.Source.UNVERIFIED, choice.source());
    }

    @Test
    void bothConstructorsFollowTheParsedVersion() {
        JeiListElementShape.Shape shape = JeiListElementShape.scan(fields(), List.of(
                new Member("<init>", JeiVersions.LEGACY_DESCRIPTOR),
                new Member("<init>", JeiVersions.FILTER_CONFIG_DESCRIPTOR)));

        JeiLineChoice.Choice choice = JeiLineChoice.choose(JeiVersions.classify("19.70.0.469"), shape);

        assertEquals(JeiVersions.Line.FILTER_CONFIG, choice.line());
        assertEquals(JeiLineChoice.Source.VERSION, choice.source());
    }

    private static JeiLineChoice.Choice choose(String version, String constructor) {
        JeiListElementShape.Shape shape = JeiListElementShape.scan(
                fields(),
                List.of(new Member("<init>", constructor)));
        return JeiLineChoice.choose(JeiVersions.classify(version), shape);
    }

    private static List<Member> fields() {
        return List.of(
                new Member(JeiListElementShape.NAMES, JeiListElementShape.NAMES_DESCRIPTOR),
                new Member(JeiListElementShape.ELEMENT, JeiListElementShape.ELEMENT_DESCRIPTOR));
    }
}
