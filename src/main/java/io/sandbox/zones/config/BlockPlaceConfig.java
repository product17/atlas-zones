package io.sandbox.zones.config;

import io.sandbox.zones.zone.Zone;
import io.sandbox.zones.zone.ZoneManagerStore;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class BlockPlaceConfig {
  public static void initBlockPlaceListener() {
    UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {
      Zone zone = ZoneManagerStore.getZoneByPlayerId(player.getUuid());

			// If the player is not in create and is in a Zone, check
			if (
        !player.isCreative() &&
        zone != null &&
        player.getMainHandStack().getItem() instanceof BlockItem
      ) {
        return zone.canPlaceBlock(player) ? ActionResult.PASS : ActionResult.FAIL;
			}

			return ActionResult.PASS;
		});
  }
}
