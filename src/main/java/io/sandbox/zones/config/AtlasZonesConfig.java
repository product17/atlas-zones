package io.sandbox.zones.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.sandbox.lib.Config;
import io.sandbox.zones.Main;
import io.sandbox.zones.config.data_types.AtlasZonesBaseConfig;
import io.sandbox.zones.config.data_types.StructurePoolConfig;
import io.sandbox.zones.config.data_types.ZoneConfig;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class AtlasZonesConfig {
  private final File configFile;
  public static AtlasZonesBaseConfig config;
  public static Map<String, ZoneConfig> zones = new HashMap<>();
  public static Map<String, StructurePoolConfig> structurePools = new HashMap<>();
  private Gson gson = new Gson();

  public AtlasZonesConfig(String configName) {
    this.configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), configName + ".json");
  }

  public void readConfigFromFile() {
    try (FileInputStream stream = new FileInputStream(configFile)) {
      byte[] bytes = new byte[stream.available()];
      stream.read(bytes);
      String file = new String(bytes);
      config = gson.fromJson(file, AtlasZonesBaseConfig.class);
    } catch (FileNotFoundException e) {
      saveConfigToFile();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void addZoneConfig(ZoneConfig labConfig) {
    zones.put(labConfig.name, labConfig);
  }

  public static void addPoolConfig(StructurePoolConfig poolConfig) {
    structurePools.put(poolConfig.name, poolConfig);
  }

  public static ZoneConfig getZoneConfig(String name) {
    return AtlasZonesConfig.zones.get(name);
  }

  public void saveConfigToFile() {
    JsonObject object = new JsonObject();

    try (FileOutputStream stream = new FileOutputStream(configFile)) {
      stream.write(gson.toJson(object).getBytes());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String getFileString(Resource resource) {
    try (InputStream stream = resource.getInputStream()) {
      byte[] bytes = new byte[stream.available()];
      stream.read(bytes);
      String file = new String(bytes);
      stream.close();
      return file;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public void initConfigListener() {
    ResourceManagerHelper.get(ResourceType.SERVER_DATA)
      .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
        @Override
        public Identifier getFabricId() {
          return new Identifier(Main.modId, "template_pools");
        }

        @Override
        public void reload(ResourceManager manager) {
          Map<Identifier, Resource> templateList = manager.findResources("worldgen/template_pool", path -> true);
          System.out.println("Templates: " + templateList.keySet().toString());
          for (Resource resource : templateList.values()) {
            Config<StructurePoolConfig> poolConfig = new Config<StructurePoolConfig>(StructurePoolConfig.class, resource);
            AtlasZonesConfig.addPoolConfig(poolConfig.getConfig());
          }

          Map<Identifier, Resource> zoneList = manager.findResources("sandbox-zones", path -> true);
          System.out.println("ZoneList: " + zoneList.keySet().toString());
          for (Resource resource : zoneList.values()) {
            String file = getFileString(resource);
            if (file != null) {
              ZoneConfig zoneConfig = gson.fromJson(file, ZoneConfig.class);
              AtlasZonesConfig.addZoneConfig(zoneConfig);
            }
          }
        }
      });
  }
}
