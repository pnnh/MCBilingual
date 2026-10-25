package pnnh.mcbilingual.language;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import pnnh.mcbilingual.freeze.SentenceResolver;
import pnnh.mcbilingual.freeze.SentenceResolver.Node;
import pnnh.mcbilingual.search.FluidSearchIndex;
import pnnh.mcbilingual.search.ItemSearchIndex;
import pnnh.mcbilingual.tooltip.SecondaryNameLine;
import pnnh.mcbilingual.tooltip.SecondaryNamePolicy;

/**
 * Holds zh_cn and en_us loaded from whatever resource packs are already active.
 * Reading these tables does not change the game's selected language.
 */
public final class LanguageTexts extends SimplePreparableReloadListener<LanguageTexts.Loaded> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LanguageTexts INSTANCE = new LanguageTexts();
    private static final String APPLE_KEY = "item.minecraft.apple";

    private volatile boolean ready;
    private volatile ClientLanguage chinese;
    private volatile ClientLanguage english;

    private LanguageTexts() {
    }

    public static LanguageTexts get() {
        return INSTANCE;
    }

    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(INSTANCE);
    }

    public boolean ready() {
        return ready;
    }

    /**
     * The table loaded for {@code zh_cn} or {@code en_us}. Null before the tables
     * are ready, for any other code, or when that language failed to load.
     */
    public ClientLanguage clientLanguage(String languageCode) {
        if (!ready) {
            return null;
        }
        return table(languageCode);
    }

    public boolean hasUsableText(String languageCode) {
        ClientLanguage language = clientLanguage(languageCode);
        return language != null && !language.getLanguageData().isEmpty();
    }

    public String currentLanguage() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return "";
        }
        return minecraft.options.languageCode;
    }

    /**
     * Returns the trimmed translation, or empty when the key is absent.
     * A value equal to the key is treated as missing so the raw key is never shown.
     * Component-valued language entries are flattened the same way the freeze lookup does.
     */
    public Optional<String> translation(String languageCode, String key) {
        if (!ready || key == null || key.isBlank()) {
            return Optional.empty();
        }
        ClientLanguage language = table(languageCode);
        if (language == null) {
            return Optional.empty();
        }
        String value = language.getOrDefault(key, null);
        if (value != null && !value.isBlank() && !value.equals(key)) {
            return Optional.of(value.strip());
        }
        Component component = language.getComponent(key);
        if (component == null) {
            return Optional.empty();
        }
        String fromComponent = component.getString();
        if (fromComponent == null || fromComponent.isBlank() || fromComponent.equals(key)) {
            return Optional.empty();
        }
        return Optional.of(fromComponent.strip());
    }

    public List<SecondaryNameLine> secondaryLines(String title, String translationKey) {
        if (!ready || translationKey == null || translationKey.isBlank()) {
            return List.of();
        }
        return SecondaryNamePolicy.lines(
                title,
                currentLanguage(),
                translationKey,
                translation(LanguageCodes.ZH_CN, translationKey).orElse(null),
                translation(LanguageCodes.EN_US, translationKey).orElse(null));
    }

    /**
     * Tries each candidate key until one yields a secondary line. Earlier keys win
     * so title-component keys beat a bare {@code Item#getDescriptionId()}.
     */
    public List<SecondaryNameLine> secondaryLines(String title, List<String> translationKeys) {
        if (!ready || translationKeys == null || translationKeys.isEmpty()) {
            return List.of();
        }
        for (String translationKey : translationKeys) {
            List<SecondaryNameLine> lines = secondaryLines(title, translationKey);
            if (!lines.isEmpty()) {
                return lines;
            }
        }
        return List.of();
    }

    /**
     * Prefer expanding {@code title}'s {@code %s} args into the other language (Jade spawner,
     * etc.), then fall back to key-only lookup.
     */
    public List<SecondaryNameLine> secondaryLines(Component title, List<String> translationKeys) {
        if (!ready) {
            return List.of();
        }
        String titleText = title == null ? "" : title.getString();
        List<SecondaryNameLine> fromComponent = secondaryLinesFromComponent(title, titleText);
        if (!fromComponent.isEmpty()) {
            return fromComponent;
        }
        return secondaryLines(titleText, translationKeys);
    }

    private List<SecondaryNameLine> secondaryLinesFromComponent(Component title, String titleText) {
        if (title == null) {
            return List.of();
        }
        String other = SentenceResolver.counterpartLanguage(currentLanguage());
        if (other == null) {
            return List.of();
        }
        ClientLanguage table = table(other);
        if (table == null) {
            return List.of();
        }
        Node node = ComponentNodes.from(title, table);
        Optional<String> formatted = SentenceResolver.tryExpand(node, key -> translation(other, key));
        if (formatted.isEmpty()) {
            return List.of();
        }
        String chinese = LanguageCodes.isChinese(other) ? formatted.get() : null;
        String english = LanguageCodes.isEnglish(other) ? formatted.get() : null;
        return SecondaryNamePolicy.lines(
                titleText,
                currentLanguage(),
                "mcbilingual.formatted_name",
                chinese,
                english);
    }

    @Override
    protected Loaded prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return new Loaded(
                load(resourceManager, LanguageCodes.ZH_CN),
                load(resourceManager, LanguageCodes.EN_US));
    }

    @Override
    protected void apply(Loaded loaded, ResourceManager resourceManager, ProfilerFiller profiler) {
        chinese = loaded.chinese();
        english = loaded.english();
        ready = true;
        LOGGER.info(
                "Language tables ready: zh_cn apple='{}', en_us apple='{}'",
                translation(LanguageCodes.ZH_CN, APPLE_KEY).orElse("-"),
                translation(LanguageCodes.EN_US, APPLE_KEY).orElse("-"));
        try {
            ItemSearchIndex.rebuild();
            FluidSearchIndex.rebuild();
        } catch (RuntimeException | LinkageError exception) {
            LOGGER.error("Could not build search terms", exception);
        }
    }

    private ClientLanguage table(String languageCode) {
        if (LanguageCodes.isChinese(languageCode)) {
            return chinese;
        }
        if (LanguageCodes.isEnglish(languageCode)) {
            return english;
        }
        return null;
    }

    private static ClientLanguage load(ResourceManager resourceManager, String code) {
        try {
            return ClientLanguage.loadFrom(resourceManager, List.of(code), false);
        } catch (RuntimeException exception) {
            LOGGER.error("Could not load language {}", code, exception);
            return null;
        }
    }

    public record Loaded(ClientLanguage chinese, ClientLanguage english) {
    }
}
