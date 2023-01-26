package io.sandbox.zones.blocks;

import io.sandbox.zones.Main;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BlockLoader {
  public static Block ATLAS_DEVICE_BLOCK = new AtlasDeviceBlock(FabricBlockSettings.of(Material.METAL).strength(1.5f, 6.0f));
  public static Block ATLAS_RECALL_BLOCK = new AtlasRecallBlock(FabricBlockSettings.of(Material.METAL).strength(1.5f, 6.0f));

  public static void init() {
    Registry.register(Registries.BLOCK, Main.id(AtlasDeviceBlock.name), ATLAS_DEVICE_BLOCK);
    Registry.register(Registries.BLOCK, Main.id(AtlasRecallBlock.name), ATLAS_RECALL_BLOCK);
  }
}
