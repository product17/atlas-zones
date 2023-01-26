package io.sandbox.zones.zone;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.sandbox.zones.Main;
import io.sandbox.zones.zone.data_types.RoomData;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class StructureBuildQueue {
    public LinkedList<StructureBlockInfo> mainPathQueue = new LinkedList<>();
    public LinkedList<JigsawBlockEntity> jigsawQueue = new LinkedList<>();
    public BlockRotation mainPathRotationAlignment;
    public List<BlockPos> blockList = new ArrayList<BlockPos>(); // List of all blocks generated to be removed on zone close
    public Integer currentDepth = 0; // used while building to track depth
    public Integer maxDepth = 3; // How many chains down the main path in the jigsaw
    public Block playerSpawnBlockType;
    public List<RoomData> rooms = new ArrayList<>();
    public LinkedList<BlockPos> spawnPositions = new LinkedList<>();

    public StructureBuildQueue(String spawnBlockTypeString) {
        Block block = Registries.BLOCK.get(new Identifier(spawnBlockTypeString));
        if (block != null) {
            this.playerSpawnBlockType = block;
        } else {
            Main.LOGGER.info("SpawnBlock type could not load: " + spawnBlockTypeString);
            this.playerSpawnBlockType = Blocks.TARGET; // Default to target block
        }
    }

    public void addJigsawBlockEntity(JigsawBlockEntity entity) {
        this.jigsawQueue.add(entity);
    }

    public void addMainPathEntity(StructureBlockInfo mainPathEntity) {
        this.mainPathQueue.add(mainPathEntity);
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

    public RoomData createNextRoom(Boolean isBossRoom) {
        RoomData room = new RoomData();
        room.isBossRoom = isBossRoom ? true : false;
        this.rooms.add(room);
        return room;
    }

    public RoomData getCurrentRoom() {
        int count = this.rooms.size();
        if (count > 0) {
            return this.rooms.get(count - 1);
        }

        return null;
    }

    public StructureBlockInfo next() {
        if (this.mainPathQueue.size() > 0) {
            return this.mainPathQueue.removeFirst();
        }

        return null;
    }

    public JigsawBlockEntity nextJigsaw() {
        if (this.jigsawQueue.size() > 0) {
            return this.jigsawQueue.removeFirst();
        }

        return null;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }
}

