package io.sandbox.atlas.client.atlas_device;

import io.sandbox.atlas.block_entities.AtlasDeviceBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class AtlasDeviceBlockRenderer extends GeoBlockRenderer<AtlasDeviceBlockEntity> {

  public AtlasDeviceBlockRenderer(BlockEntityRendererFactory.Context context) {
    super(new AtlasDeviceBlockModel());
    //TODO Auto-generated constructor stub
  }
  
  @Override
  public RenderLayer getRenderType(
    AtlasDeviceBlockEntity animatable,
    float partialTicks,
    MatrixStack stack,
    VertexConsumerProvider renderTypeBuffer,
    VertexConsumer vertexBuilder,
    int PackedLightIn,
    Identifier textureLocation
  ) {
    return RenderLayer.getEntityTranslucent(getTextureResource(animatable));
  }
}
