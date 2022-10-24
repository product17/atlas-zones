package io.sandbox.zones.processors;

import io.sandbox.zones.zone.StructureBuildQueue;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class ZoneProcessorBase extends StructureProcessor {
  public StructureBuildQueue config;

  public void setConfig(StructureBuildQueue config) {
    this.config = config;
  }

  @Override
  public StructureBlockInfo process(
    WorldView var1,
    BlockPos var2,
    BlockPos var3,
    StructureBlockInfo var4,
    StructureBlockInfo var5,
    StructurePlacementData var6
  ) {
    return null;
  }

  @Override
  protected StructureProcessorType<?> getType() {
    return null;
  }
  
}
