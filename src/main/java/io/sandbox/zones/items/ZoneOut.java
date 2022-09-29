package io.sandbox.zones.items;

import java.util.List;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class ZoneOut extends Item {
  public static String name = "zone_out";

  public ZoneOut(Settings settings) {
    super(settings);
  }

  @Override
  public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand user) {
    ItemStack tmpStack = player.getStackInHand(user);

    if (world.isClient) {
      return TypedActionResult.success(tmpStack, world.isClient());
    }

    List<ItemEntity> items = world.getEntitiesByType(
      TypeFilter.instanceOf(ItemEntity.class),
      new Box(player.getX()-10,player.getY()-10,player.getZ()-10,player.getX()+10,player.getY()+10,player.getZ()+10),
      EntityPredicates.VALID_ENTITY
    );

    System.out.println("MY Items: " + items);

    // ZoneManager.leaveZone(player);

    return TypedActionResult.success(tmpStack, world.isClient());
  }
}
