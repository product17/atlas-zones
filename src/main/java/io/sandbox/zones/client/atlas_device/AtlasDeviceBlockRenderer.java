package io.sandbox.zones.client.atlas_device;

import io.sandbox.zones.block_entities.AtlasDeviceBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class AtlasDeviceBlockRenderer extends GeoBlockRenderer<AtlasDeviceBlockEntity> {

  public AtlasDeviceBlockRenderer(BlockEntityRendererFactory.Context context) {
    super(new AtlasDeviceBlockModel());
  }
  
  // @Override
  // public RenderLayer getRenderType(
  //   AtlasDeviceBlockEntity animatable,
  //   float partialTicks,
  //   MatrixStack stack,
  //   @Nullable VertexConsumerProvider renderTypeBuffer,
  //   @Nullable VertexConsumer vertexBuilder,
  //   int PackedLightIn,
  //   Identifier textureLocation
  // ) {
  //   return RenderLayer.getEntityTranslucent(getTextureResource(animatable));
  // }
}
