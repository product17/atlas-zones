package io.sandbox.zones.client.atlas_device;

import io.sandbox.zones.Main;
import io.sandbox.zones.block_entities.AtlasDeviceBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AtlasDeviceBlockModel extends AnimatedGeoModel<AtlasDeviceBlockEntity> {

  @Override
  public Identifier getAnimationResource(AtlasDeviceBlockEntity animatable) {
    return new Identifier(Main.modId, "animations/atlas_device.animation.json");
  }
  
  @Override
  public Identifier getModelResource(AtlasDeviceBlockEntity object) {
    return new Identifier(Main.modId, "geo/atlas_device_block.geo.json");
  }
  
  @Override
  public Identifier getTextureResource(AtlasDeviceBlockEntity object) {
    return new Identifier(Main.modId, "textures/block/atlas_device_texture.png"); 
  }

}
