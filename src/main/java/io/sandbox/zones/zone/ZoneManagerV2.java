package io.sandbox.zones.zone;

import java.util.List;
import java.util.Optional;

import io.sandbox.zones.Main;
import io.sandbox.zones.config.data_types.StructurePoolConfig;
import io.sandbox.zones.config.data_types.ZoneConfig;
import io.sandbox.zones.processors.CleanupProcessor;
import io.sandbox.zones.processors.JigsawProcessor;
import io.sandbox.zones.processors.ProcessorLoader;
import io.sandbox.zones.processors.SpawnProcessor;
import io.sandbox.zones.processors.ZoneProcessorBase;
import io.sandbox.zones.zone.data_types.RoomData;
import net.minecraft.block.BlockState;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class ZoneManagerV2 {
  private static Random random = Random.create();
  public static final int DEFAULT_COOLDOWN_TICKS = 20 * 60 * 30; // 30 min

  public static Optional<Zone> generateZone(World world, PlayerEntity player, BlockPos blockPos, String zoneName) {
    if (world.isClient) {
      return Optional.ofNullable(null);
    }

    // Create new Zone
    ZoneConfig zoneConfig = ZoneManagerStore.getZoneConfig(zoneName);
    zoneConfig.dimensionType = zoneConfig.dimensionType != null ? zoneConfig.dimensionType : "piglin_gate:gate_realm";
    int nextInstanceKey = ZoneManagerStore.getNextInstanceKeyInWorld(zoneConfig.dimensionType);

    // nextInstanceKey is used to place the startlocation so zones don't overlap
    BlockPos startLocation = new BlockPos(0, zoneConfig.worldHeight, nextInstanceKey * 64);

    StructureBuildQueue structConfig = new StructureBuildQueue(zoneConfig.spawnBlock);
    structConfig.setMaxDepth(zoneConfig.maxDepth);

    // DimensionType openedInDimension = player.getWorld().getDimension();
    Zone zone = new Zone(zoneConfig, blockPos, nextInstanceKey, 1);
    zone.addBuildConfig(structConfig);
    ZoneManagerStore.addActiveZone(zone.getId(), zone);
    // set start pos by checking existing in the biome

    ServerWorld serverWorld = (ServerWorld) world;
    for (RegistryKey<World> worldKey : world.getServer().getWorldRegistryKeys()) {
      if (worldKey.getValue().equals(new Identifier(zoneConfig.dimensionType))) {
        serverWorld = world.getServer().getWorld(worldKey);
        zone.setWorld(serverWorld, worldKey);
      }
    }

    StructureTemplateManager structureTemplateManager = serverWorld.getStructureTemplateManager();
    StructurePoolConfig startPool = ZoneManagerStore.getPoolConfig(zoneConfig.roomPools.start);
    Optional<StructureTemplate> structure = structureTemplateManager
        .getTemplate(new Identifier(startPool.elements[0].element.location));
    if (structure.isPresent()) {
      StructureTemplate startStructure = structure.get();
      StructurePlacementData placementData = new StructurePlacementData().setMirror(BlockMirror.NONE);
      RoomData roomData = structConfig.createNextRoom(false); // add a new room before processing

      // Add Processors from Config
      ZoneManagerV2.applyProcessors(structConfig, placementData);

      // Place the Start Structure
      startStructure.place(serverWorld, startLocation, null, placementData,
          (net.minecraft.util.math.random.Random) ZoneManagerV2.random, 0);
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

      StructurePoolConfig structurePoolConfig = ZoneManagerStore.getPoolConfig(targetPool);

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
          
          // Apply Processors
          ZoneManagerV2.applyProcessors(structConfig, pathPlacementData);

          // Get the target Jigsaw blocks in the chosen structure
          List<StructureBlockInfo> structBlocks = pathStructure
              .getInfosForBlock(structureBlockInfo.pos, pathPlacementData, pathJigsawBlock);
          StructureBlockInfo mainPath = ZoneManagerV2.getConnectionJigsawBlock(structBlocks);

          // If item has a main path
          // If not... maybe we should log an error? with the name of the structure that
          // doesn't have a main_path
          if (mainPath != null) {
            // Align the rotation
            Direction mainPathDirection = JigsawBlock.getFacing(mainPath.state);
            structConfig.mainPathRotationAlignment = ZoneManagerV2.getRotationAmount(jigsawDirection,
                mainPathDirection);
            pathPlacementData.setRotation(structConfig.mainPathRotationAlignment);

            // Get the mainPath again with corrected direction
            structBlocks = pathStructure.getInfosForBlock(structureBlockInfo.pos, pathPlacementData,
                pathJigsawBlock);
            mainPath = ZoneManagerV2.getConnectionJigsawBlock(structBlocks);

            // Shift the pos for the maths
            BlockPos structBlockPos = structureBlockInfo.pos.offset(jigsawDirection);
            BlockPos mainPathBlockPos = mainPath.pos.offset(jigsawDirection);

            // Adjust position of structure to align jigsaw blocks
            int xDiff = structBlockPos.getX() - mainPathBlockPos.getX();
            int yDiff = structBlockPos.getY() - mainPathBlockPos.getY();
            int zDiff = structBlockPos.getZ() - mainPathBlockPos.getZ();
            BlockPos shift = new BlockPos(xDiff, yDiff, zDiff);
            BlockPos updatedPos = structBlockPos.add(shift);
            pathStructure.place(serverWorld, updatedPos, null, pathPlacementData, ZoneManagerV2.random, 0);
            pathRoomData.startBlockPos = updatedPos;
            pathRoomData.size = pathStructure.getSize();
          }
        }
      }
    }
  }

  public static void applyProcessors(StructureBuildQueue structConfig, StructurePlacementData placementData) {
    // Add the config processors
    // Defaults, need to add any from configs
    Identifier processorList[] = new Identifier[] {
      Main.id(JigsawProcessor.NAME),
      Main.id(SpawnProcessor.NAME),
      Main.id(CleanupProcessor.NAME),
    };

    Main.LOGGER.info("Zone List Size: " + processorList.length);

    for (Identifier id : processorList) {
      Main.LOGGER.info("Processor Id: " + id.toString());
      ZoneProcessorBase processor = ProcessorLoader.getProcessor(id);
      if (processor == null) {
        Main.LOGGER.warn("Zone Processor not found: " + id.toString());
        continue;
      }

      
      if (processor instanceof ZoneProcessorBase) {
        Main.LOGGER.info("Zone Processor Added: " + id.toString());
        processor.setConfig(structConfig);
      }

      placementData.addProcessor(processor);
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

}
