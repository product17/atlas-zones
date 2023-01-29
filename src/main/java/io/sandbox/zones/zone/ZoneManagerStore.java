package io.sandbox.zones.zone;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.sandbox.lib.Config;
import io.sandbox.zones.Main;
import io.sandbox.zones.config.data_types.StructurePoolConfig;
import io.sandbox.zones.config.data_types.ZoneConfig;
import io.sandbox.zones.zone.data_types.DimensionStartPoints;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ZoneManagerStore {
  private static Map<UUID, Zone> activeZones = new HashMap<>();
  private static Map<UUID, Zone> mobToZoneMap = new HashMap<>();
  private static Map<String, StructurePoolConfig> poolConfigs = new HashMap<>();
  private static Map<String, DimensionStartPoints> startPoints = new HashMap<>();
  private static Map<String, ZoneConfig> zoneConfigs = new HashMap<>();
  private static Map<String, Map<BlockPos, Long>> zonesOnCooldown = new HashMap<>();

  public static void activateZoneCooldown(String dimensionName, BlockPos blockPos, Long time) {
    Map<BlockPos, Long> dimensionZonesOnCooldown = zonesOnCooldown.get(dimensionName);
    if (dimensionZonesOnCooldown == null) {
      zonesOnCooldown.put(dimensionName, new HashMap<>());
      dimensionZonesOnCooldown = zonesOnCooldown.get(dimensionName);
    }

    dimensionZonesOnCooldown.put(blockPos, time);
  }

  public static void deactivateZoneCooldown(String dimensionName, BlockPos blockPos) {
    Map<BlockPos, Long> dimensionZonesOnCooldown = zonesOnCooldown.get(dimensionName);
    if (dimensionZonesOnCooldown == null) {
      return;
    }

    dimensionZonesOnCooldown.remove(blockPos);
  }

  public static void addActiveZone(UUID id, Zone zone) {
    activeZones.put(id, zone);
  }

  public static void cleanupZone(World world, UUID zoneId) {
    Zone zone = ZoneManagerStore.getActiveZone(zoneId);
    ZoneManagerStore.activateZoneCooldown(world.getDimensionKey().getValue().toString(), zone.blockPos, world.getTime());
    ZoneManagerStore.activeZones.remove(zoneId);
  }

  public static Zone getActiveZone(UUID id) {
    return activeZones.get(id);
  }

  public static int getNextInstanceKeyInWorld(String worldName) {
    if (!startPoints.containsKey(worldName)) {
      startPoints.put(worldName, new DimensionStartPoints());
    }

    return startPoints.get(worldName).getNextPoint();
  }

  public static StructurePoolConfig getPoolConfig(String poolName) {
    return poolConfigs.get(poolName);
  }

  public static Zone getZoneAtLocation(BlockPos blockPos) {
    for (Zone zone : ZoneManagerStore.activeZones.values()) {
      if (zone.matchEntryPoint(blockPos)) {
        return zone;
      }
      ;
    }

    return null;
  }

  public static Zone getZoneByPlayerId(UUID playerId) {
    for (Zone zone : ZoneManagerStore.activeZones.values()) {
      if (zone.hasPlayer(playerId)) {
        return zone;
      }
    }

    return null;
  }

  public static ZoneConfig getZoneConfig(String zoneName) {
    return zoneConfigs.get(zoneName);
  }

  public static Long getZoneCooldown(String dimensionName, BlockPos blockPos) {
    Map<BlockPos, Long> dimensionZonesOnCooldown = zonesOnCooldown.get(dimensionName);
    if (dimensionZonesOnCooldown != null) {
      Long cooldownTime = dimensionZonesOnCooldown.get(blockPos);
      if (cooldownTime != null) {
        return cooldownTime;
      }
    }

    return 0L;
  }

  public static void init() {
    ResourceManagerHelper.get(ResourceType.SERVER_DATA)
        .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
          @Override
          public Identifier getFabricId() {
            return new Identifier(Main.modId, "template_pools");
          }

          @Override
          public void reload(ResourceManager manager) {
            // Load all template pools for reference later
            Map<Identifier, Resource> templateList = manager.findResources("worldgen/template_pool", path -> true);
            for (Identifier resourceName : templateList.keySet()) {
              if (!resourceName.toString().startsWith("minecraft:")) {
                Main.LOGGER.info("Template Loading: " + resourceName);
                StructurePoolConfig poolConfig = new Config<StructurePoolConfig>(StructurePoolConfig.class, templateList.get(resourceName)).getConfig();
                ZoneManagerStore.poolConfigs.put(poolConfig.name, poolConfig);
              }
            }

            // Load Sandbox Zones from datapacks
            Map<Identifier, Resource> zoneList = manager.findResources(Main.modId, path -> true);
            Main.LOGGER.info("ZoneListV2: " + zoneList.keySet().toString());
            for (Resource resource : zoneList.values()) {
              ZoneConfig zoneConfig = new Config<ZoneConfig>(ZoneConfig.class, resource).getConfig();
              ZoneManagerStore.zoneConfigs.put(zoneConfig.name, zoneConfig);
            }
          }
        });

    ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY
        .register((ServerWorld world, Entity entity, LivingEntity killedEntity) -> {
          Zone zone = ZoneManagerStore.getZoneByMobId(killedEntity.getUuid());
          if (zone != null) {
            zone.entityKilledBy(entity, killedEntity);
          }
        });
  }

  public static Zone getZoneByMobId(UUID mobId) {
    return ZoneManagerStore.mobToZoneMap.get(mobId);
  }

  public static void mapMobToZone(UUID mobId, Zone zone) {
    ZoneManagerStore.mobToZoneMap.put(mobId, zone);
  }

  public static void removeMobFromZones(UUID mobId) {
    Zone zone = ZoneManagerStore.getZoneByMobId(mobId);
    if (zone != null) {
      zone.removeMobById(mobId);
    }

    ZoneManagerStore.mobToZoneMap.remove(mobId);
  }
}
