package pnnh.mcbilingual.compat;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import pnnh.mcbilingual.diagnostics.LoadReport;

/**
 * One dedicated-compatibility failure per mod id for this client launch.
 * The saved list is left untouched. Safe to call from a mixin plugin.
 */
public final class IntegrationGuard {
    private static final Set<String> FAILED = ConcurrentHashMap.newKeySet();

    private IntegrationGuard() {
    }

    public static void fail(String modId, Throwable error) {
        if (!mark(modId)) {
            return;
        }
        LoadReport.integrationFailed(CompatList.id(modId), error);
    }

    public static void fail(String modId, String reason) {
        if (!mark(modId)) {
            return;
        }
        LoadReport.integrationFailed(CompatList.id(modId), reason);
    }

    public static boolean failed(String modId) {
        return FAILED.contains(CompatList.id(modId));
    }

    private static boolean mark(String modId) {
        String id = CompatList.id(modId);
        if (id.isEmpty()) {
            return false;
        }
        return FAILED.add(id);
    }
}
