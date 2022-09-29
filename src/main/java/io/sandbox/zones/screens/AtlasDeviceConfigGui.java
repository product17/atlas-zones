package io.sandbox.zones.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import io.sandbox.zones.config.AtlasZonesConfig;
import io.sandbox.zones.gui_element.Selection;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class AtlasDeviceConfigGui extends SyncedGuiDescription {
  // private List<String> zoneList = new ArrayList<>();
  public WGridPanel root;

  public AtlasDeviceConfigGui(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
    this(syncId, playerInventory);
    String jsonString = buf.readString();
    
    if (jsonString != null && !jsonString.isEmpty()) {
      // Gson gson = new Gson();
      // this.configData = gson.fromJson(jsonString, AtlasDeviceConfigData.class); // might need to handle the json error for future updates
      // this.buildZoneListItems();
      // this.buildSelectedZoneListItems();
      
      // Build out list of available
      // Build out list of selected
    }
  }

  public AtlasDeviceConfigGui(int syncId, PlayerInventory playerInventory) {
    super(ScreenLoader.ATLAS_DEVICE_CONFIG_SCREEN_HANDLER_TYPE, syncId, playerInventory);
    this.root = new WGridPanel(1);
    setRootPanel(root);
    root.setSize(256, 166);
    root.setInsets(Insets.ROOT_PANEL);
    
    // Grab list of zones available
    List<String> zoneList = new ArrayList<>(AtlasZonesConfig.zones.keySet());
    System.out.println("Zone List: " + zoneList);
    zoneList.add("piglin_gate:cave");
    zoneList.add("piglin_gate:castle");
    zoneList.add("piglin_gate:bastion");
    WListPanel<String, Selection> list = new WListPanel<>(zoneList, Selection::new, this.configurator);
    list.setListItemHeight(18);

    this.root.add(list, 1, 10, 120, 160);

    // This syncs the Client
    root.validate(this);
  }

  public BiConsumer<String, Selection> configurator = (String zoneName, Selection elem) -> {
    elem.setLabel(Text.translatable(zoneName));
    elem.addHoverIcon(new ItemIcon(Items.NETHER_STAR));
    elem.setAlignment(HorizontalAlignment.RIGHT);
    elem.setOnClick(() -> {

    });
  };
}
