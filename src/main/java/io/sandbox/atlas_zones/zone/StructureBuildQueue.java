package io.sandbox.atlas_zones.zone;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.sandbox.atlas_zones.Main;
import io.sandbox.atlas_zones.zone.data_types.RoomData;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class StructureBuildQueue {
    public static LinkedList<StructureBlockInfo> mainPathQueue = new LinkedList<>();
    public static LinkedList<JigsawBlockEntity> jigsawQueue = new LinkedList<>();
    
    public List<BlockPos> blockList = new ArrayList<BlockPos>(); // List of all blocks generated to be removed on zone close
    public Integer currentDepth = 0; // used while building to track depth
    public Integer level; // config for difficulty
    public Integer maxDepth = 3; // How many chains down the main path in the jigsaw
    public Block playerSpawnBlockType;
    public List<RoomData> rooms = new ArrayList<>();
    public LinkedList<BlockPos> spawnPositions = new LinkedList<>();

    public StructureBuildQueue(String spawnBlockTypeString) {
        // reset them on create
        mainPathQueue = new LinkedList<>();
        jigsawQueue = new LinkedList<>();
        Block block = Registry.BLOCK.get(new Identifier(spawnBlockTypeString));
        if (block != null) {
            this.playerSpawnBlockType = block;
        } else {
            Main.LOGGER.info("SpawnBlock type could not load: " + spawnBlockTypeString);
            this.playerSpawnBlockType = Blocks.TARGET; // Default to target block
        }
    }

    public void addJigsawBlockEntity(JigsawBlockEntity entity) {
        StructureBuildQueue.jigsawQueue.add(entity);
    }

    public void addMainPathEntity(StructureBlockInfo mainPathEntity) {
        StructureBuildQueue.mainPathQueue.add(mainPathEntity);
    }

    public void addChestToCurrentRoom(BlockPos pos) {
        RoomData room = getCurrentRoom();
        if (room != null) {
            room.chestPositions.add(pos);
        }
    }

    public void addMobToCurrentRoom(BlockPos pos) {
        RoomData room = getCurrentRoom();
        if (room != null) {
            room.mobPositions.add(pos);
        }
    }

    public void createNextRoom(Boolean isBossRoom) {
        RoomData room = new RoomData();
        room.isBossRoom = isBossRoom ? true : false;
        this.rooms.add(room);
    }

    public RoomData getCurrentRoom() {
        int count = this.rooms.size();
        if (count > 0) {
            return this.rooms.get(count - 1);
        }

        return null;
    }

    public StructureBlockInfo next() {
        if (StructureBuildQueue.mainPathQueue.size() > 0) {
            return StructureBuildQueue.mainPathQueue.removeFirst();
        }

        return null;
    }

    public JigsawBlockEntity nextJigsaw() {
        if (StructureBuildQueue.jigsawQueue.size() > 0) {
            return StructureBuildQueue.jigsawQueue.removeFirst();
        }

        return null;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }
}

