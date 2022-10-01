package io.sandbox.zones.zone;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ZoneManagerV2 {
  // public static Optional<Zone> generateZone(World world, PlayerEntity player, BlockPos blockPos, String zoneName) {
  //   if (world.isClient) {
  //       return Optional.ofNullable(null);
  //   }

  //   // Create new Zone
  //   ZoneConfig zoneConfig = AtlasZonesConfig.getZoneConfig(zoneName);
  //   zoneConfig.dimentionType = zoneConfig.dimentionType != null ? zoneConfig.dimentionType : "piglin_gate:gate_realm";
  //   int nextInstanceKey = ZoneManager.getNextInstanceKeyInWorld(zoneConfig.dimentionType);
    
  //   // nextInstanceKey is used to place the startlocation so zones don't overlap
  //   BlockPos startLocation = new BlockPos(0, zoneConfig.worldHeight, nextInstanceKey * 64);
    
  //   StructureBuildQueue structConfig = new StructureBuildQueue(zoneConfig.spawnBlock);
  //   structConfig.setMaxDepth(zoneConfig.maxDepth);

  //   // DimensionType openedInDimension = player.getWorld().getDimension();
  //   Zone zone = new Zone(zoneConfig, blockPos, nextInstanceKey, 1);
  //   zone.addBuildConfig(structConfig);
  //   ZoneManager.activeZones.put(zone.getId(), zone);
  //   // set start pos by checking existing in the biome

  //   ServerWorld serverWorld = (ServerWorld) world;
  //   for (RegistryKey<World> worldKey : world.getServer().getWorldRegistryKeys()) {
  //       if (worldKey.getValue().equals(new Identifier(zoneConfig.dimentionType))) {
  //           serverWorld = world.getServer().getWorld(worldKey);
  //           zone.setWorld(serverWorld, worldKey);
  //       }
  //   }
  // }
}
