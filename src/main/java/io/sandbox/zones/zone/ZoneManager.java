package io.sandbox.zones.zone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.sandbox.zones.config.AtlasZonesConfig;
import io.sandbox.zones.config.data_types.StructurePoolConfig;
import io.sandbox.zones.config.data_types.ZoneConfig;
import io.sandbox.zones.processors.JigsawProcessor;
import io.sandbox.zones.zone.data_types.DimensionStartPoints;
import io.sandbox.zones.zone.data_types.RoomData;
import net.minecraft.block.BlockState;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class ZoneManager {
    public static final int DEFAULT_COOLDOWN_TICKS = 20 * 60 * 30; // 30 min
    // Active Zones
    private static Map<UUID, Zone> activeZones = new HashMap<>();
    private static Map<BlockPos, Long> zonesOnCooldown = new HashMap<>();
    private static Map<UUID, Zone> mobToZoneMap = new HashMap<>();
    private static Map<UUID, UUID> playerToZoneMap = new HashMap<>();
    private static Random random = Random.create();
    private static Map<PlayerEntity, UUID> joinQueue = new HashMap<>();
    private static Map<String, DimensionStartPoints> startPoints = new HashMap<>();
    
    public static void addZone(UUID key, Zone lab) {
        ZoneManager.activeZones.put(key, lab);
    }

    public static void cleanupZone(World world, UUID zoneId) {
        Zone zone = ZoneManager.activeZones.remove(zoneId);
        ZoneManager.zonesOnCooldown.put(zone.blockPos, world.getTime());
    }

    // public static Map<Integer, Zone> getActiveZonesInWorld(String worldName) {
    //     HashMap<Integer, Zone> activeZones = new HashMap<Integer, Zone>();
    //     for (Zone zoneConf : ZoneManager.activeZones.values()) {
    //         if (zoneConf.getDimentionType().equals(worldName)) {
    //             activeZones.put(zoneConf.getInstanceKey(), zoneConf);
    //         }
    //     }

    //     return activeZones;
    // }

    public static int getNextInstanceKeyInWorld(String worldName) {
        if (!startPoints.containsKey(worldName)) {
            startPoints.put(worldName, new DimensionStartPoints());
        }

        return startPoints.get(worldName).getNextPoint();
    }

    public static Zone getZone(UUID key) {
        return ZoneManager.activeZones.get(key);
    }

    public static Zone getZoneAtLocation(BlockPos blockPos) {
        for (Zone zone : ZoneManager.activeZones.values()) {
            if (zone.matchEntryPoint(blockPos)) {
                return zone;
            };
        }

        return null;
    }

    public static Long getZoneCooldown(BlockPos blockPos) {
        Long cooldownTime = zonesOnCooldown.get(blockPos);
        if (cooldownTime != null) {
            return cooldownTime;
        }

        return 0L;
    }

    public static Optional<Zone> generateZone(World world, PlayerEntity player, BlockPos blockPos, String zoneName) {
        if (world.isClient) {
            return Optional.ofNullable(null);
        }

        // Create new Zone
        ZoneConfig zoneConfig = AtlasZonesConfig.getZoneConfig(zoneName);
        zoneConfig.dimensionType = zoneConfig.dimensionType != null ? zoneConfig.dimensionType : "piglin_gate:gate_realm";
        int nextInstanceKey = ZoneManager.getNextInstanceKeyInWorld(zoneConfig.dimensionType);
        
        // nextInstanceKey is used to place the startlocation so zones don't overlap
        BlockPos startLocation = new BlockPos(0, zoneConfig.worldHeight, nextInstanceKey * 64);
        
        StructureBuildQueue structConfig = new StructureBuildQueue(zoneConfig.spawnBlock);
        structConfig.setMaxDepth(zoneConfig.maxDepth);

        // DimensionType openedInDimension = player.getWorld().getDimension();
        Zone zone = new Zone(zoneConfig, blockPos, nextInstanceKey, 1);
        zone.addBuildConfig(structConfig);
        ZoneManager.activeZones.put(zone.getId(), zone);
        // set start pos by checking existing in the biome

        ServerWorld serverWorld = (ServerWorld) world;
        for (RegistryKey<World> worldKey : world.getServer().getWorldRegistryKeys()) {
            if (worldKey.getValue().equals(new Identifier(zoneConfig.dimensionType))) {
                serverWorld = world.getServer().getWorld(worldKey);
                zone.setWorld(serverWorld, worldKey);
            }
        }

        StructureTemplateManager structureTemplateManager = serverWorld.getStructureTemplateManager();
        StructurePoolConfig startPool = AtlasZonesConfig.structurePools.get(zoneConfig.roomPools.start);
        Optional<StructureTemplate> structure = structureTemplateManager.getTemplate(new Identifier(startPool.elements[0].element.location));
        if (structure.isPresent()) {
            StructureTemplate startStructure = structure.get();
            StructurePlacementData placementData = new StructurePlacementData().setMirror(BlockMirror.NONE);
            RoomData roomData = structConfig.createNextRoom(false); // add a new room before processing

            // Add the config processors
            placementData.addProcessor(new JigsawProcessor());
            // placementData.addProcessor(new SpawnProcessor(structConfig));
            // placementData.addProcessor(new CleanupProcessor(structConfig));

            // Place the Start Structure
            startStructure.place(serverWorld, startLocation, null, placementData, (net.minecraft.util.math.random.Random) ZoneManager.random, 0);
            roomData.startBlockPos = startLocation;
            roomData.size = startStructure.getSize();

            structConfig.currentDepth = 0; // preventing an infinite loop... starting at 0
            structConfig.mainPathRotationAlignment = null;
        }
        return Optional.of(zone);
    }

    public static void addNextJigsawStructure(Zone zone) {
        // TODO: this will need to get added to the cleanup list
        StructureBuildQueue structConfig = zone.getBuildConfig();
        JigsawBlockEntity jigsawBlockEntity = structConfig.nextJigsaw();
        if (jigsawBlockEntity != null && !jigsawBlockEntity.getPool().getValue().equals(new Identifier("empty"))) {
            jigsawBlockEntity.generate(zone.getWorld(), 3, false);
        }
    }

    public static void addNextMainStructure(Zone zone) {
        StructureBuildQueue structConfig = zone.getBuildConfig();
        StructureBlockInfo structureBlockInfo = structConfig.next();
        structConfig.currentDepth++;

        if (structConfig.currentDepth > structConfig.maxDepth) {
            return;
        }

        if (structureBlockInfo != null) {

            // Block info of the jigsaw block we are building from
            BlockState blockState = structureBlockInfo.state;
            // Use last rotation to align this block
            if (structConfig.mainPathRotationAlignment != null) {
                blockState = blockState.rotate(structConfig.mainPathRotationAlignment);
            }

            Direction jigsawDirection = JigsawBlock.getFacing(blockState);
            JigsawBlock pathJigsawBlock = (JigsawBlock) blockState.getBlock();

            // Get the target pool info
            String targetPool = structureBlockInfo.nbt.getString(JigsawBlockEntity.POOL_KEY);
            Boolean isBossRoom = structConfig.currentDepth == structConfig.maxDepth;
            if (isBossRoom) {
                // if it's the last room, load the boss room
                targetPool = zone.getZoneConfig().roomPools.bossRoom;
            }

            StructurePoolConfig structurePoolConfig = AtlasZonesConfig.structurePools.get(targetPool);

            // If the pool has elements, continue
            if (structurePoolConfig != null && structurePoolConfig.elements.length > 0) {
                ServerWorld serverWorld = zone.getWorld();
                StructureTemplateManager structureManager = serverWorld.getStructureTemplateManager();
                // TODO: grab weighted random element
                // Also add a level element? or some kind of tiering system
                int len = structurePoolConfig.elements.length;
                int rand = random.nextInt(len);
                Optional<StructureTemplate> optPathStructure = structureManager
                        .getTemplate(new Identifier(structurePoolConfig.elements[rand].element.location));
                if (optPathStructure.isPresent()) {
                    RoomData pathRoomData = structConfig.createNextRoom(isBossRoom); // add a new room before processing
                    
                    // Initialize the Structure to place for this jigsaw block
                    StructureTemplate pathStructure = optPathStructure.get();
                    
                    // Make this so we can add the processors to continue this process.. (it will
                    // add more items for this while to process, until done)
                    // NOTE: this can cause an infinite loop if there is no max size
                    StructurePlacementData pathPlacementData = new StructurePlacementData()
                        .setMirror(BlockMirror.NONE);
                    pathPlacementData.addProcessor(new JigsawProcessor());
                    // pathPlacementData.addProcessor(new SpawnProcessor(structConfig));
                    // pathPlacementData.addProcessor(new CleanupProcessor(structConfig));

                    // Get the target Jigsaw blocks in the chosen structure
                    List<StructureBlockInfo> structBlocks = pathStructure
                            .getInfosForBlock(structureBlockInfo.pos, pathPlacementData, pathJigsawBlock);
                    StructureBlockInfo mainPath = ZoneManager.getConnectionJigsawBlock(structBlocks);

                    // If item has a main path
                    // If not... maybe we should log an error? with the name of the structure that
                    // doesn't have a main_path
                    if (mainPath != null) {
                        // Align the rotation
                        Direction mainPathDirection = JigsawBlock.getFacing(mainPath.state);
                        structConfig.mainPathRotationAlignment = ZoneManager.getRotationAmount(jigsawDirection, mainPathDirection);
                        pathPlacementData.setRotation(structConfig.mainPathRotationAlignment);

                        // Get the mainPath again with corrected direction
                        structBlocks = pathStructure.getInfosForBlock(structureBlockInfo.pos, pathPlacementData,
                                pathJigsawBlock);
                        mainPath = ZoneManager.getConnectionJigsawBlock(structBlocks);

                        // Shift the pos for the maths
                        BlockPos structBlockPos = structureBlockInfo.pos.offset(jigsawDirection);
                        BlockPos mainPathBlockPos = mainPath.pos.offset(jigsawDirection);

                        // Adjust position of structure to align jigsaw blocks
                        int xDiff = structBlockPos.getX() - mainPathBlockPos.getX();
                        int yDiff = structBlockPos.getY() - mainPathBlockPos.getY();
                        int zDiff = structBlockPos.getZ() - mainPathBlockPos.getZ();
                        BlockPos shift = new BlockPos(xDiff, yDiff, zDiff);
                        BlockPos updatedPos = structBlockPos.add(shift);
                        pathStructure.place(serverWorld, updatedPos, null, pathPlacementData, ZoneManager.random, 0);
                        pathRoomData.startBlockPos = updatedPos;
                        pathRoomData.size = pathStructure.getSize();
                    }
                }
            }
        }
    }

    public static StructureBlockInfo getConnectionJigsawBlock(List<StructureBlockInfo> structBlocks) {
        if (structBlocks.size() > 0) {
            for (StructureBlockInfo structBlock : structBlocks) {
                // TODO: pull the target from the config
                if (structBlock.nbt.getString(JigsawBlockEntity.NAME_KEY).equals("zone:path")) {
                    // This is our target block, need to shift this structure to line this up with
                    // our jigsaw block
                    return structBlock;
                }
            }
        }

        return null;
    }

    public static BlockRotation getRotationAmount(Direction jigsawDirection, Direction targetDirection) {
        if (jigsawDirection.getOpposite().equals(targetDirection)) {
            // if it's already the opposite no change
            return BlockRotation.NONE;
        }

        // If it's the same, just flip it
        if (jigsawDirection.equals(targetDirection)) {
            return BlockRotation.CLOCKWISE_180;
        }

        // Rotate 90 and run the check again
        BlockRotation adjusted = ZoneManager.getRotationAmount(jigsawDirection, targetDirection.rotateClockwise(Axis.Y));
        // If it's the same, just rotate 90
        if (adjusted.equals(BlockRotation.NONE)) {
            return BlockRotation.CLOCKWISE_90;
        }

        // If it's not the same, rotate countClockwise
        return BlockRotation.COUNTERCLOCKWISE_90;
    }

    public static Zone getZoneByMobId(UUID mobId) {
        return ZoneManager.mobToZoneMap.get(mobId);
    }

    public static void mapMobToZone(UUID mobId, Zone zone) {
        ZoneManager.mobToZoneMap.put(mobId, zone);
    }

    public static void removeMobFromZones(UUID mobId) {
        Zone zone = ZoneManager.getZoneByMobId(mobId);
        if (zone != null) {
            zone.removeMobById(mobId);
        }

        ZoneManager.mobToZoneMap.remove(mobId);
    }

    public static Zone getZoneByPlayerId(UUID playerId) {
        UUID zoneId = ZoneManager.playerToZoneMap.get(playerId);
        if (zoneId == null) {
            return null;
        }

        return ZoneManager.activeZones.get(zoneId);
    }

    public static Boolean joinZone(UUID zoneUuid, PlayerEntity player) {
        // Remove the player from existing zone before adding to new one
        Zone currentZone = ZoneManager.getZoneByPlayerId(player.getUuid());
        if (currentZone != null) {
            currentZone.removePlayer(player);
        }

        // add the player to the specific zone or it's queue
        Zone activeZone = ZoneManager.activeZones.get(zoneUuid);
        if (activeZone != null) {
            if (activeZone.isProcessingStructures()) {
                // Will just overwrite zoneUuid if called more than once
                ZoneManager.joinQueue.put(player, zoneUuid);
            } else {
                Boolean successfullyAdded = activeZone.addPlayer(player);
                if (successfullyAdded) {
                    ZoneManager.playerToZoneMap.put(player.getUuid(), zoneUuid);
                    return true;
                }
            }
        } else {
            // Send message: Cannot join right now
            player.sendMessage(Text.of("Unable to join zone"), true);
        }

        return false;
    }

    public static void leaveZone(PlayerEntity player) {
        UUID zoneName = ZoneManager.playerToZoneMap.get(player.getUuid());

        if (zoneName != null) {
            Zone zone = ZoneManager.activeZones.get(zoneName);
            if (zone != null) {
                zone.removePlayer(player);
            }

            ZoneManager.playerToZoneMap.remove(player.getUuid());
        }
    }

    public static void processJoinQueue() {
        for (PlayerEntity player : ZoneManager.joinQueue.keySet()) {
            // If it's not ready it will just add them back to the queue.
            // If two are loading at once, the one that is not done
            // should place players back in queue.
            ZoneManager.joinZone(ZoneManager.joinQueue.get(player), player);
        }
    }
}
