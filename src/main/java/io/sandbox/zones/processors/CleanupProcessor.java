package io.sandbox.zones.processors;

import com.mojang.serialization.Codec;

import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class CleanupProcessor extends ZoneProcessorBase {
    public static String NAME = "cleanup_processor";
    public static final Codec<CleanupProcessor> CODEC = Codec.unit(new CleanupProcessor());

    @Override
    public StructureBlockInfo process(
        WorldView world,
        BlockPos pos,
        BlockPos var1,
        StructureBlockInfo struct1,
        StructureBlockInfo structureBlockInfo,
        StructurePlacementData var6
    ) {
        this.config.blockList.add(structureBlockInfo.pos);

        return structureBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.RULE;
    }
    
}

