package io.sandbox.atlas_zones.blocks;

import org.jetbrains.annotations.Nullable;

import io.sandbox.atlas_zones.block_entities.AtlasDeviceBlockEntity;
import io.sandbox.atlas_zones.block_entities.BlockEntityLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AtlasDeviceBlock extends BlockWithEntity {
  public static final String name = "atlas_device_block";
  protected static final VoxelShape BASE_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
  protected static final VoxelShape PORTAL_STONE_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
  protected static final VoxelShape ATLAS_DEVICE_SHAPE = VoxelShapes.union(BASE_SHAPE, PORTAL_STONE_SHAPE);

  public AtlasDeviceBlock(Settings settings) {
    super(settings);
  }

  @Override
  public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return ATLAS_DEVICE_SHAPE;
  }

  @Override
  public boolean hasSidedTransparency(BlockState state) {
    return true;
  }

  @Override
  public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new AtlasDeviceBlockEntity(pos, state);
  }

  @Override
  public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
    // if (hand.equals(Hand.MAIN_HAND)) {
    //   ItemStack itemStack = player.getMainHandStack();
    //   if (itemStack.isOf(Items.LAPIS_LAZULI)) {
    //     AtlasDeviceBlockEntity atlasEntity = (AtlasDeviceBlockEntity)world.getBlockEntity(pos);
    //     if (atlasEntity.lapisCount < 4) {
    //       itemStack.setCount(itemStack.getCount() - 1);
    //       atlasEntity.lapisCount++;
  
    //       // We return to prevent the UI from openning
    //       return ActionResult.SUCCESS; 
    //     }
    //   }
    // }
    
		player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
		return ActionResult.SUCCESS;
	}

    @Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
    return checkType(type, BlockEntityLoader.ATLAS_DEVICE_BLOCK_ENTITY, (world1, pos, state1, be) -> AtlasDeviceBlockEntity.tick(world, pos, state, be));
  }

  @Override
  public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
    return false;
  }
}
