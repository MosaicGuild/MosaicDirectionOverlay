package org.mosaicmc.mosaicdirectionoverlay.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.mosaicmc.api.EventRegistration;
import org.mosaicmc.api.ExtensionMetadata;
import org.mosaicmc.extension.Extension;
import org.mosaicmc.mosaicdirectionoverlay.OverlayFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class DirectionOverlayExtension extends Extension {

    public static final String ID = "directionoverlay";

    private static final Logger LOGGER = LoggerFactory.getLogger("mosaicdirectionoverlay");

    private static final ExtensionMetadata METADATA = new ExtensionMetadata() {
        @Override
        public String getId() {
            return ID;
        }

        @Override
        public String getName() {
            return "Direction Overlay";
        }

        @Override
        public String getDescription() {
            return "Shows position and facing in the action bar";
        }

        @Override
        public String getVersion() {
            return "1.0.0";
        }

        @Override
        public String getAuthors() {
            return "Sky";
        }

        @Override
        public String getWebsite() {
            return "";
        }
    };

    private EventRegistration tick;
    private long ticks;

    @Override
    public ExtensionMetadata getMetadata() {
        return METADATA;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {

        if (tick != null) {
            tick.unregister();
            tick = null;
        }
        ticks = 0;
        tick = getContext().getEvents().onClientTick(this::onTick);
        LOGGER.info("[DirectionOverlay] enabled: overlay tick registered");
    }

    @Override
    public void onDisable() {
        if (tick != null) {
            tick.unregister();
            tick = null;
        }
        LOGGER.info("[DirectionOverlay] disabled: overlay tick unregistered");
    }

    private void onTick(Minecraft client) {
        ticks++;
        if (client == null || client.player == null || client.level == null) {
            return;
        }
        if (!OverlayFormat.shouldUpdate(ticks)) {
            return;
        }
        var player = client.player;
        String text = OverlayFormat.format(player.getX(), player.getY(), player.getZ(),
                player.getDirection().name(), player.getYRot(), player.getXRot());
        client.gui.hud.setOverlayMessage(Component.literal(text), false);
    }

    public long getTicksSeen() {
        return ticks;
    }
}
