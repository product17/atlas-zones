package io.sandbox.atlas.client;

import io.sandbox.atlas.block_entities.BlockEntityLoader;
import io.sandbox.atlas.screens.ScreenLoader;
import net.fabricmc.api.ClientModInitializer;

public class MainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenLoader.initClient();
        BlockEntityLoader.initClient();
    }
}
