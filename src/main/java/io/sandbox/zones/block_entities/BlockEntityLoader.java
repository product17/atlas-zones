package io.sandbox.zones.block_entities;

import io.sandbox.zones.Main;
import io.sandbox.zones.blocks.AtlasDeviceBlock;
import io.sandbox.zones.blocks.BlockLoader;
import io.sandbox.zones.client.atlas_device.AtlasDeviceBlockRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BlockEntityLoader {
  public static BlockEntityType<AtlasDeviceBlockEntity> ATLAS_DEVICE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(
    AtlasDeviceBlockEntity::new,
    BlockLoader.ATLAS_DEVICE_BLOCK
  ).build(null);

  public static void init() {
    Registry.register(Registries.BLOCK_ENTITY_TYPE, Main.id(AtlasDeviceBlock.name), ATLAS_DEVICE_BLOCK_ENTITY);
  }

  public static void initClient() {
    BlockEntityRendererRegistry.register(BlockEntityLoader.ATLAS_DEVICE_BLOCK_ENTITY, AtlasDeviceBlockRenderer::new);
  } 
}
