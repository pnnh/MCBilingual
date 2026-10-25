package pnnh.mcbilingual.integration.journeymap.mixin;

import java.lang.reflect.Method;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import journeymap.client.ui.waypointmanager.WaypointManagerQueryService;
import journeymap.client.waypoint.ClientWaypointImpl;

import pnnh.mcbilingual.search.WaypointNameMatch;

/**
 * Widens JourneyMap waypoint-manager search so Chinese names also match
 * toneless pinyin and initials. Injection point (JM 1.21.1-6.0.9+neoforge):
 * {@code WaypointManagerQueryService.lambda$buildWaypointRows$6} — the
 * {@code search} filter inside {@code buildWaypointRows}.
 *
 * <p>{@code getName()} lives on the JourneyMap API interface (Jar-in-Jar). Call it
 * reflectively so compileOnly does not need the nested API jar on the classpath.
 */
@Mixin(value = WaypointManagerQueryService.class, remap = false)
public class WaypointManagerQueryServiceMixin {
    @Unique
    private static final Method MCBILINGUAL$GET_NAME = mcbilingual$lookupGetName();

    @Inject(
            method = "lambda$buildWaypointRows$6",
            at = @At("RETURN"),
            cancellable = true,
            remap = false,
            require = 0)
    private static void mcbilingual$matchPinyin(
            String search,
            ClientWaypointImpl waypoint,
            CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ()) {
            return;
        }
        try {
            if (WaypointNameMatch.matches(search, mcbilingual$waypointName(waypoint))) {
                callback.setReturnValue(true);
            }
        } catch (Throwable ignored) {
            // Soft-fail: keep JM's native false; never break the manager UI.
        }
    }

    @Unique
    private static String mcbilingual$waypointName(ClientWaypointImpl waypoint) {
        if (waypoint == null || MCBILINGUAL$GET_NAME == null) {
            return null;
        }
        try {
            Object name = MCBILINGUAL$GET_NAME.invoke(waypoint);
            return name instanceof String text ? text : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    @Unique
    private static Method mcbilingual$lookupGetName() {
        try {
            return ClientWaypointImpl.class.getMethod("getName");
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
