package pnnh.mcbilingual.integration.jei;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import pnnh.mcbilingual.integration.jei.JeiListElementShape.Member;

class JeiListElementShapeTest {
    @Test
    void betterMcJei19_27ConstructorIsTheLegacyShape() {
        JeiListElementShape.Shape shape = JeiListElementShape.scan(fields(), List.of(
                new Member("<init>", JeiVersions.LEGACY_DESCRIPTOR),
                new Member("create", "(Lmezz/jei/api/ingredients/ITypedIngredient;)Lmezz/jei/gui/ingredients/IListElementInfo;")));

        assertTrue(shape.namesField());
        assertTrue(shape.elementField());
        assertTrue(shape.legacyConstructor());
        assertFalse(shape.filterConstructor());
        assertTrue(shape.detail().contains(JeiVersions.LEGACY_DESCRIPTOR));
    }

    @Test
    void jei19_56ConstructorIsTheFilterShape() {
        JeiListElementShape.Shape shape = JeiListElementShape.scan(
                fields(),
                List.of(new Member("<init>", JeiVersions.FILTER_CONFIG_DESCRIPTOR)));

        assertTrue(shape.filterConstructor());
        assertFalse(shape.legacyConstructor());
        assertTrue(shape.detail().contains(JeiVersions.FILTER_CONFIG_DESCRIPTOR));
    }

    @Test
    void missingNamesFieldIsReported() {
        JeiListElementShape.Shape shape = JeiListElementShape.scan(
                List.of(new Member(JeiListElementShape.ELEMENT, JeiListElementShape.ELEMENT_DESCRIPTOR)),
                List.of(new Member("<init>", JeiVersions.LEGACY_DESCRIPTOR)));

        assertFalse(shape.namesField());
        assertTrue(shape.detail().contains("namesField=false"));
    }

    @Test
    void missingConstructorIsReported() {
        JeiListElementShape.Shape shape = JeiListElementShape.scan(
                fields(),
                List.of(new Member("create", "()V")));

        assertFalse(shape.legacyConstructor());
        assertFalse(shape.filterConstructor());
        assertTrue(shape.detail().contains("constructors=[]"));
    }

    private static List<Member> fields() {
        return List.of(
                new Member(JeiListElementShape.NAMES, JeiListElementShape.NAMES_DESCRIPTOR),
                new Member(JeiListElementShape.ELEMENT, JeiListElementShape.ELEMENT_DESCRIPTOR),
                new Member("modIds", "Ljava/util/List;"));
    }
}
