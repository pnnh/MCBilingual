package pnnh.mcbilingual.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import pnnh.mcbilingual.diagnostics.LoadReport;

/**
 * Client list of {@code modid=Mode} lines. An empty list means every id is Full.
 * Loading and reloading replace the whole snapshot. A bad reload keeps the previous one.
 */
public final class CompatConfig {
    private static final String FILE_NAME = "mcbilingual-client.toml";
    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> MODES;
    private static volatile Map<String, CompatMode> snapshot = Map.of();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment(
                "Empty means every mod id is Full. One line is modid=Full, modid=Generic, or modid=Off.");
        builder.translation("config.mcbilingual.compat_modes");
        MODES = builder.defineListAllowEmpty(
                "compatModes",
                List.of(),
                () -> "",
                value -> value instanceof String);
        SPEC = builder.build();
    }

    private CompatConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC, FILE_NAME);
    }

    public static void onLoading(ModConfigEvent.Loading event) {
        apply(event.getConfig());
    }

    public static void onReloading(ModConfigEvent.Reloading event) {
        apply(event.getConfig());
    }

    public static CompatMode mode(String modId) {
        return CompatList.modeOf(snapshot, modId);
    }

    public static Map<String, CompatMode> snapshot() {
        return snapshot;
    }

    private static void apply(ModConfig config) {
        if (config == null
                || config.getType() != ModConfig.Type.CLIENT
                || !FILE_NAME.equals(config.getFileName())) {
            return;
        }
        try {
            List<String> lines = new ArrayList<>();
            List<? extends String> raw = MODES.get();
            if (raw != null) {
                for (Object value : raw) {
                    if (value instanceof String text) {
                        lines.add(text);
                    }
                }
            }
            CompatList.Parsed parsed = CompatList.parse(lines);
            snapshot = parsed.modes();
            for (CompatList.Warning warning : parsed.warnings()) {
                if (warning.kind() == CompatList.Kind.DUPLICATE) {
                    LoadReport.compatDuplicate(warning.modId(), warning.raw());
                } else {
                    LoadReport.compatInvalid(warning.modId(), warning.raw());
                }
            }
        } catch (Exception | LinkageError exception) {
            LoadReport.compatReloadFailed(exception);
        }
    }
}
