package io.sandbox.zones;

import io.sandbox.lib.SandboxLogger;
import io.sandbox.zones.block_entities.BlockEntityLoader;
import io.sandbox.zones.blocks.BlockLoader;
import io.sandbox.zones.config.BlockBreakConfig;
import io.sandbox.zones.config.BlockPlaceConfig;
import io.sandbox.zones.config.ElytraConfig;
import io.sandbox.zones.config.PlayerRespawnConfig;
import io.sandbox.zones.items.ItemLoader;
import io.sandbox.zones.processors.ProcessorLoader;
import io.sandbox.zones.screens.ScreenLoader;
import io.sandbox.zones.zone.ZoneManagerStore;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Main implements ModInitializer {
  public static final String modId = "sandbox-zones";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	// public static final Logger LOGGER = LoggerFactory.getLogger(modId);
  public static final SandboxLogger LOGGER = new SandboxLogger(modId);

	@Override
	public void onInitialize() {
    LOGGER.info("Loading mod: " + modId);

    // Load in datapacks
    ZoneManagerStore.initDatapackLoader();
    ProcessorLoader.init();

    // Initialize a main config
    // AtlasZonesConfig atlasConfig = new AtlasZonesConfig(modId);
    // atlasConfig.readConfigFromFile(); // This line doesn't really do anything yet...
    // atlasConfig.initConfigListener();

    // Allow/disable elytra based on zone configs
    ElytraConfig.initElytraListener();

    // If PLayer is in Zone, respawn at start
    PlayerRespawnConfig.initRespawnListener();

    // Allow/disable block breaking in zone config (has allow list)
    BlockBreakConfig.initBlockBreakListener();
    BlockPlaceConfig.initBlockPlaceListener();

    // Load custom Items/Blocks/Screens
    ItemLoader.init();
    BlockLoader.init();
    BlockEntityLoader.init();
    ScreenLoader.init();
    
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Loaded mod: " + modId);
	}

  public static Identifier id(String name) {
		return new Identifier(Main.modId, name);
	}
}
