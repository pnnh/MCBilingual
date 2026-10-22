package pnnh.mcbilingual.switchlang.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;

/**
 * {@code I18n.setLanguage} is package-private on 21.1.251. It also pushes the
 * strings into NeoForge's {@code I18nManager}.
 */
@Mixin(I18n.class)
public interface I18nLanguageSetter {
    @Invoker("setLanguage")
    static void mcbilingual$setLanguage(Language language) {
        throw new AssertionError();
    }
}
