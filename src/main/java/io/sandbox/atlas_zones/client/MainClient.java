package io.sandbox.atlas_zones.client;

import io.sandbox.atlas_zones.screens.ScreenLoader;
import net.fabricmc.api.ClientModInitializer;

public class MainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Config Menu
        // HandledScreens.<AtlasDeviceConfigGui, AtlasDeviceConfigScreen>register(
        //     Main.ATLAS_DEVICE_CONFIG_SCREEN_HANDLER_TYPE,
        //     (gui, inventory, title) -> new AtlasDeviceConfigScreen(gui, inventory, title)
        // );

        // Dungeon Menu
        ScreenLoader.initClient();
    }
    
}
