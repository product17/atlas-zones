package io.sandbox.zones.processors;

import java.util.Random;

import com.mojang.serialization.Codec;

import io.sandbox.zones.zone.StructureBuildQueue;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldView;

public class SpawnProcessor extends StructureProcessor {
    public static Random random = new Random();
    public static final Codec<SpawnProcessor> CODEC = Codec.unit(new SpawnProcessor(new StructureBuildQueue(Registry.BLOCK.getId(Blocks.TARGET).toString())));
    private StructureBuildQueue config;

    public SpawnProcessor(StructureBuildQueue config) {
        this.config = config;
    }

    @Override
    public StructureBlockInfo process(
        WorldView world,
        BlockPos blockPos,
        BlockPos var3,
        StructureBlockInfo struct1,
        StructureBlockInfo structureBlockInfo,
        StructurePlacementData var6
    ) {
        BlockState state = structureBlockInfo.state;
        if (state.isOf(config.playerSpawnBlockType)) {
            config.spawnPositions.add(structureBlockInfo.pos);
            return null; // Does this remove the block?
        }

        // Chest shit
        if (state.isOf(Blocks.CHEST)) {
            this.config.addChestToCurrentRoom(structureBlockInfo.pos);
        }

        // Spawner shit
        if (state.isOf(Blocks.SPAWNER)) {
            this.config.addMobToCurrentRoom(structureBlockInfo.pos);
            
            // return null to remove the spawner block
            return null;
        }

        return structureBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.RULE;
    }
    
}
