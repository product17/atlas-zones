package io.sandbox.zones.config;

import io.sandbox.zones.zone.Zone;
import io.sandbox.zones.zone.ZoneManagerStore;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBreakConfig {
  public static void initBlockBreakListener() {
    PlayerBlockBreakEvents.BEFORE.register((World world, PlayerEntity player, BlockPos blockPos, BlockState blockState, BlockEntity entity) -> {
			Zone zone = ZoneManagerStore.getZoneByPlayerId(player.getUuid());

			// If the player is not in create and is in a Zone, check
			if (!player.isCreative() && zone != null) {
				return zone.canBreakBlock(player, blockState);
			}
			return true;
		});
  }
}
