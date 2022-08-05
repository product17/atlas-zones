package io.sandbox.atlas.screens;

import io.sandbox.atlas.Main;
import io.sandbox.atlas.blocks.AtlasDeviceBlock;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.registry.Registry;

public class ScreenLoader {
  public static ExtendedScreenHandlerType<AtlasDeviceGui> ATLAS_DEVICE_SCREEN_HANDLER_TYPE;

  public static void init() {
    ATLAS_DEVICE_SCREEN_HANDLER_TYPE = Registry.register(
      Registry.SCREEN_HANDLER,
      Main.id(AtlasDeviceBlock.name),
      new ExtendedScreenHandlerType<AtlasDeviceGui>(
        (syncId, inventory, buf) -> new AtlasDeviceGui(syncId, inventory, buf)
      )
    );
  }

  public static void initClient() {
    HandledScreens.<AtlasDeviceGui, AtlasDeviceScreen>register(
      ScreenLoader.ATLAS_DEVICE_SCREEN_HANDLER_TYPE,
      (gui, inventory, title) -> new AtlasDeviceScreen(gui, inventory, title)
    );
  }
}
