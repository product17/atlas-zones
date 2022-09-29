package io.sandbox.zones.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.sandbox.zones.zone.Zone;
import io.sandbox.zones.zone.ZoneManager;
import io.sandbox.zones.zone.data_types.MobDetails;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
  protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
    super(entityType, world);
  }

  // Adjust XP drop from config
  @Inject(method = "getXpToDrop", at = @At("RETURN"), cancellable = true)
  public void getXpToDrop(CallbackInfoReturnable<Integer> cir) {
    UUID mobId = this.getUuid();
    Zone zone = ZoneManager.getZoneByMobId(mobId);
    if (zone != null) {
      MobDetails mobDetails = zone.getMobById(mobId);
      if (mobDetails != null && mobDetails.expMultiplier != null) {
        int xp = (int) Math.floor(cir.getReturnValue() * zone.getDifficulty() * mobDetails.expMultiplier);
        cir.setReturnValue(xp);
      }
    }
  }
}
