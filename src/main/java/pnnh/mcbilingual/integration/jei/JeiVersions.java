package pnnh.mcbilingual.integration.jei;

/**
 * JEI 1.21.1 builds, and which {@code ListElementInfo} constructor they have.
 * <p>
 * Tag {@code v19.55.0} still uses
 * {@code (IListElement, IIngredientManager, IModIdHelper)}.
 * Tag {@code v19.56.0} adds {@code IIngredientFilterConfig} in commit
 * {@code 0f2120c687} ("Migrate JEI configuration to MezzConfig").
 * The four-argument constructor is unchanged through {@code v19.70.0}.
 * GitHub has no release body for tag {@code v19.56.0}; the tags are the source.
 * Published versions look like {@code 19.27.0.343}. The fourth component is the build number.
 * A major other than 19 is not this Minecraft line.
 */
public final class JeiVersions {
    public static final int LINE = 19;
    /** First minor whose {@code ListElementInfo} constructor takes {@code IIngredientFilterConfig}. */
    public static final int FILTER_CONFIG_MINOR = 56;

    public static final String LEGACY_DESCRIPTOR =
            "(Lmezz/jei/gui/ingredients/IListElement;Lmezz/jei/api/runtime/IIngredientManager;Lmezz/jei/api/helpers/IModIdHelper;)V";
    public static final String FILTER_CONFIG_DESCRIPTOR =
            "(Lmezz/jei/gui/ingredients/IListElement;Lmezz/jei/api/runtime/IIngredientManager;Lmezz/jei/common/config/IIngredientFilterConfig;Lmezz/jei/api/helpers/IModIdHelper;)V";

    public static final String LEGACY_INIT = "<init>" + LEGACY_DESCRIPTOR;
    public static final String FILTER_CONFIG_INIT = "<init>" + FILTER_CONFIG_DESCRIPTOR;

    public enum Line {
        /** Before 19.56.0. Three constructor arguments. */
        LEGACY,
        /** 19.56.0 and newer. Four constructor arguments. */
        FILTER_CONFIG,
        /** Not a parsed JEI 19 version. */
        UNKNOWN
    }

    private JeiVersions() {
    }

    /**
     * @return dot-separated components, or null when any component is not an integer
     */
    public static int[] parse(String version) {
        if (version == null) {
            return null;
        }
        String trimmed = version.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String[] pieces = trimmed.split("\\.", -1);
        int[] parts = new int[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            if (!digits(pieces[i])) {
                return null;
            }
            try {
                parts[i] = Integer.parseInt(pieces[i]);
            } catch (NumberFormatException exception) {
                return null;
            }
        }
        return parts;
    }

    public static Line classify(String version) {
        int[] parts = parse(version);
        if (parts == null || parts.length == 0 || parts[0] != LINE) {
            return Line.UNKNOWN;
        }
        int minor = parts.length > 1 ? parts[1] : 0;
        if (minor < FILTER_CONFIG_MINOR) {
            return Line.LEGACY;
        }
        return Line.FILTER_CONFIG;
    }

    private static boolean digits(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
