package io.sandbox.zones.processors;

import com.mojang.serialization.Codec;

import io.sandbox.zones.zone.StructureBuildQueue;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldView;

public class CleanupProcessor extends StructureProcessor {
    public static final Codec<CleanupProcessor> CODEC = Codec.unit(new CleanupProcessor(new StructureBuildQueue(Registry.BLOCK.getId(Blocks.TARGET).toString())));
    
    private StructureBuildQueue config;

    public CleanupProcessor(StructureBuildQueue config) {
        // Can pass the depth level here
        this.config = config;
    }

    @Override
    public StructureBlockInfo process(
        WorldView world,
        BlockPos pos,
        BlockPos var1,
        StructureBlockInfo struct1,
        StructureBlockInfo structureBlockInfo,
        StructurePlacementData var6
    ) {
        if (structureBlockInfo.pos == null) {
            System.out.println("this one is null");
        }
        this.config.blockList.add(structureBlockInfo.pos);

        return structureBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.RULE;
    }
    
}

