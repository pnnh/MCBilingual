package pnnh.mcbilingual.integration.jei;

/**
 * Picks one {@code ListElementInfo} constructor mixin.
 * The parsed JEI version is the switch. The class file overrides that choice only when
 * it contains a single known constructor and the version points at the other one,
 * or when the version cannot be parsed.
 */
public final class JeiLineChoice {
    public static final String LEGACY_MIXIN = "JeiListElementInfoLegacyMixin";
    public static final String FILTER_MIXIN = "JeiListElementInfoMixin";

    public enum Source {
        /** Parsed version and class file select the same constructor. */
        VERSION,
        /** The class file decided, because the version was missing or pointed at a constructor that is not there. */
        BYTECODE,
        /** The version was parsed and {@code ListElementInfo.class} could not be read. */
        UNVERIFIED
    }

    public record Choice(JeiVersions.Line line, JeiVersions.Line parsed, Source source, String detail) {
        public boolean applies() {
            return line == JeiVersions.Line.LEGACY || line == JeiVersions.Line.FILTER_CONFIG;
        }
    }

    private JeiLineChoice() {
    }

    public static Choice choose(JeiVersions.Line parsed, JeiListElementShape.Shape shape) {
        JeiVersions.Line versionLine = parsed == null ? JeiVersions.Line.UNKNOWN : parsed;
        if (shape == null) {
            if (versionLine == JeiVersions.Line.UNKNOWN) {
                return new Choice(
                        JeiVersions.Line.UNKNOWN,
                        versionLine,
                        Source.UNVERIFIED,
                        "ListElementInfo could not be read and the JEI version was not recognized");
            }
            return new Choice(
                    versionLine,
                    versionLine,
                    Source.UNVERIFIED,
                    "ListElementInfo could not be read; trusting JEI version");
        }
        if (!shape.namesField() || !shape.elementField()) {
            return new Choice(JeiVersions.Line.UNKNOWN, versionLine, Source.VERSION, shape.detail());
        }
        boolean legacy = shape.legacyConstructor();
        boolean filter = shape.filterConstructor();
        if (versionLine == JeiVersions.Line.LEGACY || versionLine == JeiVersions.Line.FILTER_CONFIG) {
            boolean wanted = versionLine == JeiVersions.Line.LEGACY ? legacy : filter;
            boolean other = versionLine == JeiVersions.Line.LEGACY ? filter : legacy;
            if (wanted) {
                return new Choice(versionLine, versionLine, Source.VERSION, shape.detail());
            }
            if (other) {
                JeiVersions.Line actual = versionLine == JeiVersions.Line.LEGACY
                        ? JeiVersions.Line.FILTER_CONFIG
                        : JeiVersions.Line.LEGACY;
                return new Choice(actual, versionLine, Source.BYTECODE, shape.detail());
            }
            return new Choice(JeiVersions.Line.UNKNOWN, versionLine, Source.VERSION, shape.detail());
        }
        if (legacy && !filter) {
            return new Choice(JeiVersions.Line.LEGACY, versionLine, Source.BYTECODE, shape.detail());
        }
        if (filter && !legacy) {
            return new Choice(JeiVersions.Line.FILTER_CONFIG, versionLine, Source.BYTECODE, shape.detail());
        }
        return new Choice(JeiVersions.Line.UNKNOWN, versionLine, Source.BYTECODE, shape.detail());
    }

    public static boolean applies(JeiVersions.Line line, String mixinClassName) {
        if (line == null || mixinClassName == null || mixinClassName.isEmpty()) {
            return false;
        }
        String name = mixinClassName.replace('/', '.');
        int dot = name.lastIndexOf('.');
        String simple = dot < 0 ? name : name.substring(dot + 1);
        return switch (line) {
            case LEGACY -> LEGACY_MIXIN.equals(simple);
            case FILTER_CONFIG -> FILTER_MIXIN.equals(simple);
            case UNKNOWN -> false;
        };
    }

    public static String message(String version, Choice choice) {
        String shown = version == null || version.isBlank() ? "unknown" : version;
        String detail = choice.detail() == null ? "" : choice.detail();
        if (!choice.applies()) {
            return "MCBilingual 跳过 JEI " + shown
                    + " 的搜索注入，客户端会继续启动。ListElementInfo 不兼容，JEI 不能用另一种语言或拼音搜索。"
                    + detail
                    + " MCBilingual skipped JEI " + shown
                    + " search injection. The client will still start. "
                    + detail;
        }
        String chineseLine = choice.line() == JeiVersions.Line.LEGACY
                ? "19.56 之前的三参数构造函数"
                : "19.56 起带 IIngredientFilterConfig 的四参数构造函数";
        String englishLine = choice.line() == JeiVersions.Line.LEGACY
                ? "the three-argument constructor from before 19.56"
                : "the four-argument IIngredientFilterConfig constructor from 19.56 onward";
        String chineseNote = switch (choice.source()) {
            case VERSION -> "";
            case BYTECODE -> choice.parsed() == JeiVersions.Line.UNKNOWN
                    ? "版本号无法按 19.x 解析，已按类文件里唯一存在的构造函数注入。"
                    : "版本号与类文件不一致，已按类文件里实际存在的构造函数注入。";
            case UNVERIFIED -> "读不到 ListElementInfo，未能核对类文件。";
        };
        String englishNote = switch (choice.source()) {
            case VERSION -> "";
            case BYTECODE -> choice.parsed() == JeiVersions.Line.UNKNOWN
                    ? " The version is not a JEI 19.x number, so injection follows the only known constructor in the class file."
                    : " The version and the class file disagree, so injection follows the constructor that is present.";
            case UNVERIFIED -> " ListElementInfo could not be read, so the class file was not checked.";
        };
        return "MCBilingual 将按 JEI " + shown + " 注入搜索词，使用 " + chineseLine + "。"
                + chineseNote
                + " MCBilingual will add search terms for JEI " + shown + " using " + englishLine + "."
                + englishNote
                + " " + detail;
    }
}
