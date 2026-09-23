package org.mosaicmc.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosaicmc.internal.ClientTickRegistry;
import org.mosaicmc.mosaicdirectionoverlay.client.DirectionOverlayExtension;

class DirectionOverlayLifecycleTest {

    @BeforeEach
    void clearRegistry() {
        ExtensionManager.resetForTesting();
    }

    @Test
    void enableRegistersAndTicksFire() {
        var overlay = registeredOverlay();

        ExtensionManager.enable(DirectionOverlayExtension.ID);
        fireTicks(3);

        assertEquals(3, overlay.getTicksSeen());
    }

    @Test
    void unregisterViaDisableStopsTicks() {
        var overlay = registeredOverlay();

        ExtensionManager.enable(DirectionOverlayExtension.ID);
        fireTicks(2);
        assertEquals(2, overlay.getTicksSeen());

        ExtensionManager.disable(DirectionOverlayExtension.ID);
        fireTicks(3);

        assertEquals(2, overlay.getTicksSeen(), "listener must not fire after disable");
    }

    @Test
    void disableClearsStrayRegistrations() {
        var overlay = registeredOverlay();
        ExtensionManager.enable(DirectionOverlayExtension.ID);

        var strayFires = new AtomicInteger();
        overlay.getContext().getEvents().onClientTick(client -> strayFires.incrementAndGet());

        ExtensionManager.disable(DirectionOverlayExtension.ID);
        fireTicks(2);

        assertEquals(0, strayFires.get(), "disable must leave no callback behind");
        assertEquals(0, overlay.getTicksSeen(), "re-fire must not reach the removed listener");
    }

    @Test
    void repeatedEnableNeverStacksListeners() {
        var overlay = registeredOverlay();

        ExtensionManager.enable(DirectionOverlayExtension.ID);
        fireTicks(2);
        ExtensionManager.enable(DirectionOverlayExtension.ID);
        fireTicks(2);

        assertEquals(2, overlay.getTicksSeen(), "second enable must reset, not stack");
        assertEquals(1, ClientTickRegistry.endTickListeners().size(), "exactly one listener must remain");
    }

    private static DirectionOverlayExtension registeredOverlay() {
        ExtensionManager.init(fakeLoader(List.of(entrypoint("mosaicdirectionoverlay", new DirectionOverlayExtension()))));
        return (DirectionOverlayExtension) ExtensionManager.get(DirectionOverlayExtension.ID).orElseThrow();
    }

    private static void fireTicks(int count) {
        for (int i = 0; i < count; i++) {
            for (var listener : ClientTickRegistry.endTickListeners()) {
                listener.onEndTick(null);
            }
        }
    }

    private static FabricLoader fakeLoader(List<EntrypointContainer<Extension>> containers) {
        return fake(FabricLoader.class, Map.of("getEntrypointContainers", containers));
    }

    private static EntrypointContainer<Extension> entrypoint(String modId, Extension extension) {
        return fake(EntrypointContainer.class, Map.of(
                "getProvider", fake(ModContainer.class, Map.of(
                        "getMetadata", fake(ModMetadata.class, Map.of("getId", modId)))),
                "getEntrypoint", extension));
    }

    @SuppressWarnings("unchecked")
    private static <T> T fake(Class<T> type, Map<String, Object> stubs) {
        return (T) Proxy.newProxyInstance(
                DirectionOverlayLifecycleTest.class.getClassLoader(),
                new Class<?>[] { type },
                (proxy, method, args) -> {
                    if (stubs.containsKey(method.getName())) {
                        return stubs.get(method.getName());
                    }
                    return null;
                });
    }
}
