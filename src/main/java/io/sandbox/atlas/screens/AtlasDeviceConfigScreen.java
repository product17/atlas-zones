package io.sandbox.atlas.screens;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class AtlasDeviceConfigScreen extends CottonInventoryScreen<AtlasDeviceConfigGui> {

  public AtlasDeviceConfigScreen(AtlasDeviceConfigGui description, PlayerInventory inventory, Text title) {
    super(description, inventory, title);
  }
}
