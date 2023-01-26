package io.sandbox.zones.items;

import io.sandbox.zones.Main;
import io.sandbox.zones.blocks.AtlasDeviceBlock;
import io.sandbox.zones.blocks.AtlasRecallBlock;
import io.sandbox.zones.blocks.BlockLoader;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ItemLoader {
  private static final ItemGroup ITEM_GROUP = FabricItemGroup.builder(Main.id("zones")).build();

  public static BlockItem ATLAS_DEVICE_BOCK_ITEM = new BlockItem(BlockLoader.ATLAS_DEVICE_BLOCK, new FabricItemSettings());
  public static BlockItem ATLAS_RECALL_BOCK_ITEM = new BlockItem(BlockLoader.ATLAS_RECALL_BLOCK, new FabricItemSettings());
  public static ZoneOut ZONE_OUT = new ZoneOut(
    new Item.Settings().maxCount(1)
  );
  
  public static void init() {
    Registry.register(Registries.ITEM, Main.id(AtlasDeviceBlock.name), ATLAS_DEVICE_BOCK_ITEM);
    Registry.register(Registries.ITEM, Main.id(AtlasRecallBlock.name), ATLAS_RECALL_BOCK_ITEM);
    Registry.register(Registries.ITEM, Main.id(ZoneOut.name), ZONE_OUT);


    ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP).register(content -> {
      content.add(ATLAS_DEVICE_BOCK_ITEM);
      content.add(ATLAS_RECALL_BOCK_ITEM);
      content.add(ZONE_OUT);
    });
  }
}
