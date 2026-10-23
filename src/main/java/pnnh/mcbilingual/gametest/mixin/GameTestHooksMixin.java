package pnnh.mcbilingual.gametest.mixin;

import java.lang.reflect.Method;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforgespi.language.ModFileScanData;

/**
 * Dev clients register every game test they find. Optional compat tests in
 * other mods link classes that are not installed. Skip those instead of crashing.
 * The class name is read by reflection so this mixin does not mention ASM types.
 */
@Mixin(value = GameTestHooks.class, remap = false)
public class GameTestHooksMixin {
    @Inject(method = "addGameTestMethods", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mcbilingual$skipUnlinkedGameTest(
            ModFileScanData.AnnotationData annotationData,
            Set<Method> gameTestMethods,
            CallbackInfo callback) {
        try {
            Object type = annotationData.getClass().getMethod("clazz").invoke(annotationData);
            String className = (String) type.getClass().getMethod("getClassName").invoke(type);
            Class<?> testClass = Class.forName(className, false, GameTestHooks.class.getClassLoader());
            testClass.getDeclaredMethods();
        } catch (ReflectiveOperationException | LinkageError error) {
            callback.cancel();
        }
    }
}
