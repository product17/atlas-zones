package io.sandbox.zones.zone.data_types;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Room {
  public Map<UUID, MobDetails> bosses = new HashMap<>();
  public Map<UUID, MobDetails> mobs = new HashMap<>();
}
