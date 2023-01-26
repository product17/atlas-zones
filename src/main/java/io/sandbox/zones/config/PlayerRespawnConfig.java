package io.sandbox.zones.config;

import io.sandbox.zones.zone.Zone;
import io.sandbox.zones.zone.ZoneManagerStore;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerRespawnConfig {
  public static void initRespawnListener() {
    // This tails the respawn event and can probably be used to teleport the player
		ServerPlayerEvents.AFTER_RESPAWN.register((
			ServerPlayerEntity oldPlayer,
			ServerPlayerEntity newPlayer,
			boolean alive
		) -> {
			Zone zone = ZoneManagerStore.getZoneByPlayerId(newPlayer.getUuid());
			if (zone != null) {
				if (zone.shouldKeepInventory(newPlayer.getUuid())) {
					newPlayer.getInventory().clone(oldPlayer.getInventory());
				}

				// respawn in the Zone
				zone.respawnPlayer(newPlayer);
			}
		});
  }
}
