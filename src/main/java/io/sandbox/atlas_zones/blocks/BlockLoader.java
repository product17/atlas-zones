package io.sandbox.atlas_zones.blocks;

import io.sandbox.atlas_zones.Main;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.registry.Registry;

public class BlockLoader {
  public static Block ATLAS_DEVICE_BLOCK = new AtlasDeviceBlock(FabricBlockSettings.of(Material.METAL).strength(1.5f, 6.0f));
  public static Block ATLAS_RECALL_BLOCK = new AtlasRecallBlock(FabricBlockSettings.of(Material.METAL).strength(1.5f, 6.0f));

  public static void init() {
    Registry.register(Registry.BLOCK, Main.id(AtlasDeviceBlock.name), ATLAS_DEVICE_BLOCK);
    Registry.register(Registry.BLOCK, Main.id(AtlasRecallBlock.name), ATLAS_RECALL_BLOCK);
  }
}
