package io.sandbox.zones.block_entities;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import io.sandbox.zones.zone.Zone;
import io.sandbox.zones.zone.ZoneManager;
import io.sandbox.zones.zone.ZoneManagerStore;
import io.sandbox.zones.zone.ZoneManagerV2;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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

public class AtlasDeviceBlockEntity extends BlockEntity implements IAnimatable {
  private static final String BUILDING_ZONE_FIELD = "building_zone";
  public static final String CONFING_UPDATE_EVENT = "button_selected";
  private ArrayList<String> targetZoneList = new ArrayList<>(); // list of zones that can be selected
  public UUID zoneInstanceId;
  private final String TARGET_ZONE_LIST = "target_zone_list";
  private final DefaultedList<ItemStack> items = DefaultedList.ofSize(4, ItemStack.EMPTY);
  public int lapisCount = 0;
  public AnimationFactory factory = new AnimationFactory(this);
  public Boolean buildingZone = false;

  public AtlasDeviceBlockEntity(BlockPos pos, BlockState state) {
    super(BlockEntityLoader.ATLAS_DEVICE_BLOCK_ENTITY, pos, state);
  }

  public UUID getZoneUuid() {
    return this.zoneInstanceId;
  }

  public static Boolean isConfigMenu(PlayerEntity player) {
    return player.isSneaking() && player.isCreative();
  }

  public static void tick(World world, BlockPos pos, BlockState state, AtlasDeviceBlockEntity atlasDeviceEntity) {
    if (!world.isClient && atlasDeviceEntity.buildingZone) {
      Zone zone = ZoneManagerStore.getActiveZone(atlasDeviceEntity.zoneInstanceId);
      if (zone == null) {
        return;
      }

      if (zone.hasNextMainStructure()) {
        ZoneManagerV2.addNextMainStructure(zone);
      } else if (zone.hasNextJigsawStructure()) {
        ZoneManagerV2.addNextJigsawStructure(zone);
      } else {
        atlasDeviceEntity.buildingZone = false;
        zone.setProcessingStructures(atlasDeviceEntity.buildingZone);
        // zone.addPlayer(null); // Process all queued players to join
      }
    }
  }

  @Override
  public void readNbt(NbtCompound nbt) {
    super.readNbt(nbt);
    Inventories.readNbt(nbt, items);
    this.buildingZone = nbt.getBoolean(BUILDING_ZONE_FIELD);
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
    nbt.putBoolean(BUILDING_ZONE_FIELD, this.buildingZone);
    if (this.targetZoneList.size() > 0) {
      nbt.putString(TARGET_ZONE_LIST, StringUtils.join(this.targetZoneList, ","));
    } else {
      nbt.remove(TARGET_ZONE_LIST);
    }

    super.writeNbt(nbt);
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
