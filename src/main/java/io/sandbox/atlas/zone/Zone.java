package io.sandbox.atlas.zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import io.sandbox.atlas.Main;
import io.sandbox.atlas.config.data_types.MobDefinition;
import io.sandbox.atlas.config.data_types.ZoneConfig;
import io.sandbox.atlas.zone.data_types.MobDetails;
import io.sandbox.atlas.zone.data_types.PreviousPos;
import io.sandbox.atlas.zone.data_types.Room;
import io.sandbox.atlas.zone.data_types.RoomData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class Zone {
    public BlockPos blockPos;
    private List<Block> breakableBlocks;
    private List<Item> placeableBlocks;
    private StructureBuildQueue buildConfig;
    private int difficulty = 0;
    private String dimentionType;
    private int emptyTicks = 0;
    private int emptyTicksMax = 100; // TODO: load this from a config for cleanup time
    private UUID id = UUID.randomUUID();
    private int instanceKey;
    private Boolean hasSpawnedMobsAndChests = false;
    private Map<UUID, Room> mobToRoomMap = new HashMap<>();
    private List<PlayerEntity> players = new ArrayList<>();
    private HashMap<UUID, PreviousPos> previousPlayerPositions = new HashMap<>();
    private Boolean processingStructures = true;
    private Random random = new Random();
    private List<Room> rooms = new ArrayList<>();
    private ServerWorld world;
    private ZoneConfig zoneConfig;
    
    public Zone(ZoneConfig zoneConfig, BlockPos blockPos, int instanceKey, int difficulty) {
        // Loot tables?
        this.blockPos = blockPos;
        this.dimentionType = zoneConfig.dimentionType;
        this.difficulty = difficulty;
        this.instanceKey = instanceKey;
        this.zoneConfig = zoneConfig;
    }

    public void addBuildConfig(StructureBuildQueue buildConfig) {
        this.buildConfig = buildConfig;
    }

    public Boolean addPlayer(PlayerEntity player) {
        if (this.world.isClient) {
            return false;
        }

        if (this.world != null) {
            this.players.add(player);
            ServerPlayerEntity servPlayer = (ServerPlayerEntity)player;

            this.previousPlayerPositions.put(
                servPlayer.getUuid(),
                new PreviousPos(
                    servPlayer.getBlockPos(),
                    servPlayer.getWorld().getRegistryKey()
                )
            );

            this.teleportToZoneEntrance(servPlayer);

            if (!this.hasSpawnedMobsAndChests) {
                for (RoomData roomData : this.buildConfig.rooms) {
                    Room room = new Room();
                    this.rooms.add(room);

                    for (BlockPos chestPos : roomData.chestPositions) {
                        Optional<ChestBlockEntity> chestOpt = world.getBlockEntity(chestPos, BlockEntityType.CHEST);
                        if (chestOpt.isPresent()) {
                            String chestLootTable = ZoneConfig.getLootTableAtLevel(this.difficulty, zoneConfig.defaultLootTables);
                            if (zoneConfig.chestLootTables != null) {
                                String hasChestLootTable = ZoneConfig.getLootTableAtLevel(this.difficulty, zoneConfig.chestLootTables);
                                if (hasChestLootTable != null) {
                                    chestLootTable = hasChestLootTable;
                                }
                            }

                            ChestBlockEntity chest = chestOpt.get();
                            chest.setLootTable(new Identifier(chestLootTable), this.random.nextInt());
                        }
                    }

                    // Place the boss and remove one spawn location
                    if (roomData.isBossRoom) {
                        this.placeMob(room, roomData.mobPositions.remove(0), true);
                    }

                    // Place all mobs for each room
                    for (BlockPos spawnPos : roomData.mobPositions) {
                        this.placeMob(room, spawnPos, false);
                    }
                }

                this.hasSpawnedMobsAndChests = true;
            }

            return true;
        }

        return false;
    }

    public Boolean canBreakBlock(PlayerEntity player, BlockState blockState) {
        if (this.zoneConfig.breakableBlocks.size() > 0) {
            if (this.breakableBlocks == null) {
                this.breakableBlocks = new ArrayList<>();
                for (String blockName : this.zoneConfig.breakableBlocks) {
                    this.breakableBlocks.add(Registry.BLOCK.get(new Identifier(blockName)));
                }
            }

            for (Block breakableBlock : this.breakableBlocks) {
                if (blockState.isOf(breakableBlock)) {
                    return true;
                }
            }

            String msg = String.join(", ", this.zoneConfig.breakableBlocks);
            Text text = Text.of("Can only Break: " + msg);
            player.sendMessage(text, true);
            return false;
        }

        return false;
    }

    public Boolean canPlaceBlock(PlayerEntity player) {
        if (this.zoneConfig.placeableBlocks.size() > 0) {
            if (this.placeableBlocks == null) {
                this.placeableBlocks = new ArrayList<>();
                for (String itemName : this.zoneConfig.placeableBlocks) {
                    this.placeableBlocks.add(Registry.ITEM.get(new Identifier(itemName)));
                }
            }

            for (Item placeableBlock : this.placeableBlocks) {
                if (player.getMainHandStack().getItem().equals(placeableBlock)) {
                    return true;
                }
            }
        }

        return false;
    }

    public Boolean canUseElytra() {
        return this.zoneConfig.elytraAllowed;
    }

    public void cleanupBlocks() {
        List<BlockPos> blockList = getBlockList();
        for (BlockPos blockPos : blockList) {
            if (blockPos != null) {
                // this.world.getblock
                BlockState existingBlock = this.world.getBlockState(blockPos);
                if (existingBlock != null) {
                    if (existingBlock.isOf(Blocks.CHEST)) {
                        Optional<ChestBlockEntity> chest = this.world.getBlockEntity(blockPos, BlockEntityType.CHEST);
                        if (chest.isPresent()) {
                            // ChestBlockEntity chestEntity = chest.get();
                            // chestEntity.
                            // TODO: figure out later to cleanup better
                        }
                    }

                    BlockState blockState = Registry.BLOCK.get(new Identifier("air")).getDefaultState();
                    this.world.setBlockState(blockPos, blockState);
                }
            }
        }
    }

    public void cleanupItems() {
        for(RoomData room: this.buildConfig.rooms) {
            // Get center Block  
            Box roomBoxt = new Box(
                room.startBlockPos.getX(),
                room.startBlockPos.getY(),
                room.startBlockPos.getZ(),
                room.startBlockPos.getX() + room.size.getX(),
                room.startBlockPos.getY() + room.size.getY(),
                room.startBlockPos.getZ() + room.size.getZ()
            );
            // roomBoxt.expand(20);
            List<ItemEntity> boxItems = world.getEntitiesByType(
                TypeFilter.instanceOf(ItemEntity.class),
                roomBoxt,
                EntityPredicates.VALID_ENTITY
            );

            for(ItemEntity item : boxItems) {
                item.remove(RemovalReason.DISCARDED);
            }
        }
    }

    public void cleanupMobs() {
        Set<UUID> mobIds = mobToRoomMap.keySet();
        for(UUID mobId : mobIds) {
            Entity mob = world.getEntity(mobId);
            if (mob != null) {
                // Discard doesn't trigger kill events
                mob.discard();
            }
        }
    }

    public List<BlockPos> getBlockList() {
        return this.buildConfig.blockList;
    }

    public StructureBuildQueue getBuildConfig() {
        return this.buildConfig;
    }
    
    public int getDifficulty() {
        return this.difficulty;
    }

    public String getDimentionType() {
        return this.dimentionType;
    }

    public int getEmptyTicks() {
        return this.emptyTicks;
    }

    public int getEmptyTicksMax() {
        return this.emptyTicksMax;
    }

    public UUID getId() {
        return this.id;
    }

    public int getInstanceKey() {
        return this.instanceKey;
    }

    public MobDetails getMobById(UUID mobId) {
        Room room = this.mobToRoomMap.get(mobId);
        if (room != null) {
            MobDetails mobDetails = room.mobs.get(mobId);
            if (mobDetails == null) {
                mobDetails = room.bosses.get(mobId);
            }

            return mobDetails;
        }

        return null;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public Boolean getProcessingStructures() {
        return this.processingStructures;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public ZoneConfig getZoneConfig() {
        return this.zoneConfig;
    }

    public Boolean hasNextMainStructure() {
        return this.buildConfig.mainPathQueue.peek() != null;
    }

    public Boolean hasNextJigsawStructure() {
        return this.buildConfig.jigsawQueue.peek() != null;
    }

    public Boolean matchEntryPoint(BlockPos blockPos) {
        return this.blockPos.equals(blockPos);
    }

    private void preventZombification(MobEntity mob) {
        if (mob instanceof PiglinEntity) {
            ((PiglinEntity)mob).setImmuneToZombification(true);
        }

        if (mob instanceof PiglinBruteEntity) {
            ((PiglinBruteEntity)mob).setImmuneToZombification(true);
        }

        if (mob instanceof HoglinEntity) {
            ((HoglinEntity)mob).setImmuneToZombification(true);
        }
    }

    // Takes a room and list of pos, choses and removes pos
    private void placeMob(Room room, BlockPos spawnPos, Boolean isBoss) {
        MobDetails mobDetails = new MobDetails();
        MobDefinition mobDefinition = this.zoneConfig.mobs.getRandomMob(this.difficulty, isBoss);
        EntityType<?> entity = Registry.ENTITY_TYPE.get(new Identifier(mobDefinition.mobType));
        MobEntity mob = (MobEntity)entity.create((World)world);

        mobDetails.expMultiplier = mobDefinition.xpMultiplier != null ? mobDefinition.xpMultiplier : 0;
        mob.setCustomName(Text.of(isBoss ? "Da Boss" : "Walker"));

        String lootTable = ZoneConfig.getLootTableAtLevel(this.difficulty, zoneConfig.defaultLootTables);
        if (mobDefinition.lootTables != null) {
            String hasLootForDifficulty = ZoneConfig.getLootTableAtLevel(this.difficulty, mobDefinition.lootTables);
            if (hasLootForDifficulty != null) {
                lootTable = hasLootForDifficulty;
            }
        }

        // Add loottable if exists
        if (lootTable != null) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("DeathLootTable", lootTable);
            nbt.putLong("DeathLootTableSeed", this.random.nextLong());
            mob.readNbt(nbt);
        }

        // Boost the damage
        if (mobDefinition.damageMultiplier != null) {
            mob.getAttributeInstance(
                EntityAttributes.GENERIC_ATTACK_DAMAGE
            ).addPersistentModifier(
                new EntityAttributeModifier(
                    "damageMultiplier",
                    this.difficulty * mobDefinition.damageMultiplier,
                    EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                )
            );
        }

        // Boost the health
        if (mobDefinition.healthMultiplier != null) {
            mob.getAttributeInstance(
                EntityAttributes.GENERIC_MAX_HEALTH
            ).addPersistentModifier(
                new EntityAttributeModifier(
                    "healthMultiplier",
                    this.difficulty * mobDefinition.healthMultiplier,
                    EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                )
            );

            // Mobs need to be healed if their life is increased
            mob.heal(mob.getMaxHealth());
        }

        mob.setPosition(new Vec3d(spawnPos.getX(), spawnPos.getY() + 1, spawnPos.getZ()));
        mob.setPersistent();
        mob.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.TRIGGERED, null, null);

        // TODO: config baby chance later...
        if (mob.isBaby()) {
            mob.setBaby(false);
            mob.equipStack(EquipmentSlot.MAINHAND, (double)this.random.nextFloat() < 0.5D ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD));
        }

        this.preventZombification(mob);
        world.spawnEntity(mob);
        mobDetails.mob = mob;
        if (isBoss) {
            room.bosses.put(mob.getUuid(), mobDetails);
        } else {
            room.mobs.put(mob.getUuid(), mobDetails);
        }

        this.mobToRoomMap.put(mob.getUuid(), room);
        ZoneManager.mapMobToZone(mob.getUuid(), this);
    }

    public void removeMobById(UUID mobId) {
        Room room = this.mobToRoomMap.get(mobId);
        if (room != null) {
            room.mobs.remove(mobId);
            room.bosses.remove(mobId);
        }
    }

    public void removePlayer(PlayerEntity player) {
        ServerPlayerEntity servPlayer = (ServerPlayerEntity)player;
        Main.LOGGER.info("Removing Player from zone: " + servPlayer.getName());
        
        this.players.remove(player);
        if (this.getPlayerCount() <= 0) {
            this.cleanupMobs();
            this.cleanupBlocks();
            this.cleanupItems();
            
            Main.LOGGER.info("Cleaning up Zone");

            ZoneManager.cleanupZone(player.getWorld(), this.id);
        }

        PreviousPos previousPos = this.previousPlayerPositions.get(player.getUuid());
        servPlayer.teleport(
            this.world.getServer().getWorld(previousPos.worldKey),
            previousPos.lastPos.getX(),
            previousPos.lastPos.getY(),
            previousPos.lastPos.getZ(),
            player.getYaw(),
            player.getPitch()
        );

        // just always set this back to 0 when someone leaves
        // it will only start counting when no one is left though
        this.emptyTicks = 0; // not used at this time
    }

    public void respawnPlayer(ServerPlayerEntity player) {
        this.teleportToZoneEntrance(player);
    }

    public void incrementEmptyTicks() {
        this.emptyTicks++;
    }

    public void setProcessingStructures(Boolean processing) {
        this.processingStructures = processing;
    }

    public void setWorld(ServerWorld world) {
        this.world = world;
    }

    public Boolean shouldKeepInventory(UUID playerId) {
        // TODO: do some kind of death count on the player
        // if they exceed that they will die and lose their stuff
        return this.zoneConfig.keepInventoryOnDeath;
    }

    public void teleportToZoneEntrance(ServerPlayerEntity player) {
        int randomPos = this.random.nextInt(this.buildConfig.spawnPositions.size());
        BlockPos spawnLoc = this.buildConfig.spawnPositions.get(randomPos);
        player.teleport(
            this.world,
            spawnLoc.getX() + 0.5,
            spawnLoc.getY() + 1,
            spawnLoc.getZ() + 0.5,
            player.getYaw(),
            player.getPitch()
        );
    }
}
