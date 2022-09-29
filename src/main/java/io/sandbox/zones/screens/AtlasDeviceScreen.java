package io.sandbox.zones.screens;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class AtlasDeviceScreen extends CottonInventoryScreen<AtlasDeviceGui> {
  public AtlasDeviceScreen(AtlasDeviceGui description, PlayerInventory inventory, Text title) {
    super(description, inventory, title);
  }
}
