package io.sandbox.atlas.blocks;

import io.sandbox.atlas.zone.Zone;
import io.sandbox.atlas.zone.ZoneManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AtlasRecallBlock extends Block {
  public static final String name = "atlas_recall_block";

  public AtlasRecallBlock(Settings settings) {
    super(settings);
    //TODO Auto-generated constructor stub
  }
  
  @Override
  public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
    if (!world.isClient) {
      Zone zone = ZoneManager.getZoneByPlayerId(player.getUuid());
      if (zone != null) {
        zone.removePlayer(player);
      }
    }

		return ActionResult.SUCCESS;
	}
}
