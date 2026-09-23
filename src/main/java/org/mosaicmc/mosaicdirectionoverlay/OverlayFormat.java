package org.mosaicmc.mosaicdirectionoverlay;

import java.util.Locale;

public final class OverlayFormat {
    public static final int TICK_INTERVAL = 5;

    private OverlayFormat() {
    }

    public static boolean shouldUpdate(long tickCount) {
        return tickCount % TICK_INTERVAL == 0;
    }

    public static String format(double x, double y, double z, String facing, float yaw, float pitch) {
        String safeFacing = facing == null || facing.isBlank() ? "?" : facing.strip();
        return String.format(Locale.ROOT, "XYZ %.1f / %.1f / %.1f  %s  (yaw %.1f pitch %.1f)",
                x, y, z, safeFacing, yaw, pitch);
    }
}
