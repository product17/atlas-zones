package io.sandbox.atlas_zones.block_entities;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import io.github.cottonmc.cotton.gui.networking.NetworkSide;
import io.github.cottonmc.cotton.gui.networking.ScreenNetworking;
import io.sandbox.atlas_zones.inventories.AtlasDeviceInventory;
import io.sandbox.atlas_zones.screens.AtlasDeviceGui;
import io.sandbox.atlas_zones.screens.data_types.CurrentZoneData;
import io.sandbox.atlas_zones.zone.Zone;
import io.sandbox.atlas_zones.zone.ZoneManager;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class AtlasDeviceBlockEntity extends BlockEntity implements AtlasDeviceInventory, ExtendedScreenHandlerFactory, IAnimatable {
  public static final String CONFING_UPDATE_EVENT = "button_selected";
  private ArrayList<String> targetZoneList = new ArrayList<>(); // list of zones that can be selected
  private UUID zoneInstanceId;
  private final String TARGET_ZONE_LIST = "target_zone_list";
  private final DefaultedList<ItemStack> items = DefaultedList.ofSize(4, ItemStack.EMPTY);
  public int lapisCount = 0;
  public AnimationFactory factory = new AnimationFactory(this);

  public AtlasDeviceBlockEntity(BlockPos pos, BlockState state) {
    super(BlockEntityLoader.ATLAS_DEVICE_BLOCK_ENTITY, pos, state);
  }

  public UUID getZoneUuid() {
    return this.zoneInstanceId;
  }

  public Boolean isConfigMenu(PlayerEntity player) {
    return player.isSneaking() && player.isCreative();
  }

  public static void tick(World world, BlockPos pos, BlockState state, AtlasDeviceBlockEntity be) {
    
    // if (be.zoneInstanceId != null) {
    //   Zone zone = ZoneManager.getZone(be.zoneInstanceId);

    //   if (zone == null) {
    //     // make sure to set zoneInstanceId to null to prevent this from looping
    //     be.zoneInstanceId = null;
    //     return;
    //   }

    //   if (zone.getPlayerCount() == 0) {
    //     // Once the zone hits the max ticks it will remove itself
    //     zone.incrementEmptyTicks();
    //   }
    // }
  }

  @Override
  public DefaultedList<ItemStack> getItems() {
    return items;
  }

  @Override
  public boolean canPlayerUse(PlayerEntity player) {
    return pos.isWithinDistance(player.getBlockPos(), 4.5);
  }

  @Override
  public void readNbt(NbtCompound nbt) {
    super.readNbt(nbt);
    Inventories.readNbt(nbt, items);
    String target = nbt.getString(TARGET_ZONE_LIST);
    this.targetZoneList = new ArrayList<>();
    if (target != null) {
      String[] list = target.split(","); // maybe change this to json... w/e
      for (String item : list) {
        if (!item.isEmpty()) {
          this.targetZoneList.add(item);
        }
      }
    }
  }

  @Override
  public void writeNbt(NbtCompound nbt) {
    Inventories.writeNbt(nbt, items);
    if (this.targetZoneList.size() > 0) {
      nbt.putString(TARGET_ZONE_LIST, StringUtils.join(this.targetZoneList, ","));
    } else {
      nbt.remove(TARGET_ZONE_LIST);
    }

    super.writeNbt(nbt);
  }

  @Override
  public Text getDisplayName() {
    // Using the block name as the screen title
    // return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    return Text.of("Atlas Device");
  }

  @Override
  public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
    // This method only fires on the server

    // if (this.isConfigMenu(player)) {
    //   // Admin... setting up the configs for the block
    //   AtlasDeviceConfigGui configWindow = new AtlasDeviceConfigGui(syncId, inventory);

    //   ScreenNetworking.of(configWindow, NetworkSide.SERVER).receive(Util.id(CONFING_UPDATE_EVENT + syncId), buf -> {
    //     Gson gson = new Gson();
    //     String jsonData = buf.readString();
    //     AtlasDeviceConfigData updatedConfig = gson.fromJson(jsonData, AtlasDeviceConfigData.class);
    //     System.out.println(updatedConfig.selectedZoneList);
    //     this.targetZoneList = updatedConfig.selectedZoneList;
    //   });

    //   return configWindow;
    // }

    AtlasDeviceGui uiWindow = new AtlasDeviceGui(syncId, inventory, this);
    ScreenNetworking screenNetworking = ScreenNetworking.of(uiWindow, NetworkSide.SERVER);

    screenNetworking.receive(uiWindow.selectEventId, buf -> {
      String buttonPress = buf.readString();
      System.out.println("Button was pressed: " + buttonPress + " : " + pos.toShortString() + " : " + pos.toString());

      Optional<Zone> zoneOpt = ZoneManager.generateZone(world, inventory.player, pos, buttonPress);
      if (zoneOpt.isPresent()) {
        // TODO: emit zone selection to other player in inventory
        this.zoneInstanceId = zoneOpt.get().getId();
        System.out.println("Zone exists");
      }
    });

    screenNetworking.receive(uiWindow.playerEnterEventId, buf -> {
      if (this.zoneInstanceId != null && ZoneManager.getZone(this.zoneInstanceId) != null) {
        // TODO: Check that zone exists too...
        ZoneManager.joinZone(this.zoneInstanceId, inventory.player);
      }
    });

    return uiWindow;
  }

  @Override
  public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
    Gson gson = new Gson();

    // if (this.isConfigMenu(player)) {
    //   AtlasDeviceConfigData configData = new AtlasDeviceConfigData();
    //   configData.zoneList = new ArrayList<String>(LoadConfig.zones.keySet());
    //   configData.selectedZoneList = this.targetZoneList;
    //   String json = gson.toJson(configData);

    //   buf.writeString(json);
    //   return;
    // }

    Zone existingZone = ZoneManager.getZoneAtLocation(pos);
    CurrentZoneData zoneData = new CurrentZoneData();
    zoneData.zoneName = "piglin_gate:base_lab";
    zoneData.blockPos = pos;
    zoneData.active = existingZone != null;
    if (existingZone == null) {
      Long cooldown = player.getWorld().getTime() - ZoneManager.getZoneCooldown(pos);
      if (cooldown < ZoneManager.DEFAULT_COOLDOWN_TICKS) {
        zoneData.cooldownLeft = cooldown;
      }
    }

    System.out.println(gson.toJson(zoneData));

    // TODO: pull the string from config
    buf.writeString(gson.toJson(zoneData));
  }

  @Override
  public void registerControllers(AnimationData animationData) {
    animationData.addAnimationController(
      new AnimationController<AtlasDeviceBlockEntity>(this, "controller", 0, this::predicate));
  }

  private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
    event.getController().setAnimation(new AnimationBuilder().addAnimation("active", true));

    return PlayState.CONTINUE;
}

  @Override
  public AnimationFactory getFactory() {
    return this.factory;
  }
}
