package io.sandbox.zones.zone.data_types;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class RoomData {
  public Vec3i size;
  public BlockPos startBlockPos;
  public Boolean isBossRoom = false;
  public List<BlockPos> chestPositions = new ArrayList<>();
  public List<BlockPos> mobPositions = new ArrayList<>();
  // entityPositions? for traveling merchants or quest NPCs?
}
