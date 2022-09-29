package io.sandbox.zones.config;

import io.sandbox.zones.zone.Zone;
import io.sandbox.zones.zone.ZoneManager;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.entity.LivingEntity;

public class ElytraConfig {
  public static void initElytraListener() {
    EntityElytraEvents.ALLOW.register((LivingEntity entity) -> {
			Zone zone = ZoneManager.getZoneByPlayerId(entity.getUuid());
			if (zone != null) {
				return zone.canUseElytra();
			}

			return true;
		});
  }
}
