package io.sandbox.atlas.block_entities;

import io.sandbox.atlas.Main;
import io.sandbox.atlas.blocks.AtlasDeviceBlock;
import io.sandbox.atlas.blocks.BlockLoader;
import io.sandbox.atlas.client.atlas_device.AtlasDeviceBlockRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class BlockEntityLoader {
  public static BlockEntityType<AtlasDeviceBlockEntity> ATLAS_DEVICE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(
    AtlasDeviceBlockEntity::new,
    BlockLoader.ATLAS_DEVICE_BLOCK
  ).build(null);

  public static void init() {
    Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id(AtlasDeviceBlock.name), ATLAS_DEVICE_BLOCK_ENTITY);
  }

  public static void initClient() {
    BlockEntityRendererRegistry.register(BlockEntityLoader.ATLAS_DEVICE_BLOCK_ENTITY, AtlasDeviceBlockRenderer::new);
  }
}
