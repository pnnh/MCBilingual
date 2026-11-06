package pnnh.mcbilingual.integration.jei;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the {@code ListElementInfo} members the search mixin needs.
 * The version chooses the constructor. This scan checks that the chosen constructor,
 * {@code names}, and {@code element} are actually in the jar.
 */
public final class JeiListElementShape {
    public static final String NAMES = "names";
    public static final String NAMES_DESCRIPTOR = "Ljava/util/List;";
    public static final String ELEMENT = "element";
    public static final String ELEMENT_DESCRIPTOR = "Lmezz/jei/gui/ingredients/IListElement;";

    private JeiListElementShape() {
    }

    public record Member(String name, String descriptor) {
    }

    public record Shape(
            boolean namesField,
            boolean elementField,
            boolean legacyConstructor,
            boolean filterConstructor,
            String detail) {
    }

    public static Shape scan(List<Member> fields, List<Member> methods) {
        List<Member> safeFields = fields == null ? List.of() : fields;
        List<Member> safeMethods = methods == null ? List.of() : methods;
        boolean names = has(safeFields, NAMES, NAMES_DESCRIPTOR);
        boolean element = has(safeFields, ELEMENT, ELEMENT_DESCRIPTOR);
        boolean legacy = false;
        boolean filter = false;
        List<String> constructors = new ArrayList<>();
        for (Member method : safeMethods) {
            if (method == null || !"<init>".equals(method.name())) {
                continue;
            }
            String descriptor = method.descriptor() == null ? "?" : method.descriptor();
            constructors.add(descriptor);
            if (JeiVersions.LEGACY_DESCRIPTOR.equals(descriptor)) {
                legacy = true;
            } else if (JeiVersions.FILTER_CONFIG_DESCRIPTOR.equals(descriptor)) {
                filter = true;
            }
        }
        String detail = "namesField=" + names
                + ", elementField=" + element
                + ", legacyConstructor=" + legacy
                + ", filterConstructor=" + filter
                + ", constructors=" + constructors;
        return new Shape(names, element, legacy, filter, detail);
    }

    private static boolean has(List<Member> members, String name, String descriptor) {
        for (Member member : members) {
            if (member != null && name.equals(member.name()) && descriptor.equals(member.descriptor())) {
                return true;
            }
        }
        return false;
    }
}
