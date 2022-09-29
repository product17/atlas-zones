package io.sandbox.zones.client;

import io.sandbox.zones.block_entities.BlockEntityLoader;
import io.sandbox.zones.screens.ScreenLoader;
import net.fabricmc.api.ClientModInitializer;

public class MainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenLoader.initClient();
        BlockEntityLoader.initClient();
    }
}
