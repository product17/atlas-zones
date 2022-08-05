package io.sandbox.atlas.items;

import io.sandbox.atlas.Main;
import io.sandbox.atlas.blocks.AtlasDeviceBlock;
import io.sandbox.atlas.blocks.AtlasRecallBlock;
import io.sandbox.atlas.blocks.BlockLoader;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

public class ItemLoader {
  public static BlockItem ATLAS_DEVICE_BOCK_ITEM = new BlockItem(BlockLoader.ATLAS_DEVICE_BLOCK, new FabricItemSettings().group(ItemGroup.MISC));
  public static BlockItem ATLAS_RECALL_BOCK_ITEM = new BlockItem(BlockLoader.ATLAS_RECALL_BLOCK, new FabricItemSettings().group(ItemGroup.MISC));
  public static ZoneOut ZONE_OUT = new ZoneOut(
    new Item.Settings().group(ItemGroup.MISC).maxCount(1)
  );
  
  public static void init() {
    Registry.register(Registry.ITEM, Main.id(AtlasDeviceBlock.name), ATLAS_DEVICE_BOCK_ITEM);
    Registry.register(Registry.ITEM, Main.id(AtlasRecallBlock.name), ATLAS_RECALL_BOCK_ITEM);
    Registry.register(Registry.ITEM, Main.id(ZoneOut.name), ZONE_OUT);
  }
}
