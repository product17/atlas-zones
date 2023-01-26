package io.sandbox.zones.processors;

import java.util.HashMap;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class JigsawProcessor extends ZoneProcessorBase {
  public static final JigsawProcessor INSTANCE = new JigsawProcessor();
  public static final Codec<JigsawProcessor> CODEC = Codec.unit(() -> INSTANCE);
  public static String NAME = "jigsaw_processor";

  private HashMap<String, Boolean> pathTargets = new HashMap<>() {
    {
      put("zone:path", true);
    }
  };

  @Override
  public StructureBlockInfo process(
      WorldView world,
      BlockPos var2,
      BlockPos var3,
      StructureBlockInfo struct1,
      StructureBlockInfo structureBlockInfo,
      StructurePlacementData var6) {
    BlockState state = structureBlockInfo.state;
    if (state.isOf(Blocks.JIGSAW)) {
      // This is where we would add stuff to a tracking class
      // track stage inside the dungeon, if more that x stages, return null

      String jigsawTarget = structureBlockInfo.nbt.getString(JigsawBlockEntity.TARGET_KEY);

      // Generate Jigsaw Entity
      JigsawBlock block = (JigsawBlock) state.getBlock();
      JigsawBlockEntity blockEntity = (JigsawBlockEntity) block.createBlockEntity(structureBlockInfo.pos, state);
      if (this.pathTargets.get(jigsawTarget) != null) {
        // If target is in the list of dungeon paths
        // send structureBlock info so we can grab the pool and grab structure to place
        config.addMainPathEntity(structureBlockInfo);
      } else {
        // Add everything else to list to generate normally
        config.addJigsawBlockEntity(blockEntity);
      }

      return null; // Remove the jigsaw
    }

    return structureBlockInfo;
  }

  @Override
  protected StructureProcessorType<?> getType() {
    return ProcessorLoader.JIGSAW_PROCESSOR;
  }

}
