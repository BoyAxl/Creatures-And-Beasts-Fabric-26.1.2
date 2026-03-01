package com.cgessinger.creaturesandbeasts.client.armor.render;

import com.cgessinger.creaturesandbeasts.client.armor.model.FlowerCrownModel;
import com.cgessinger.creaturesandbeasts.items.FlowerCrownItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@Environment(EnvType.CLIENT)
public class FlowerCrownRenderer extends GeoArmorRenderer<FlowerCrownItem> {
    public FlowerCrownRenderer() {
        super(new FlowerCrownModel());

        this.head = this.getGeoModel().getBone("group").orElseThrow();
    }

    @Override
    public void defaultRender(PoseStack poseStack, FlowerCrownItem animatable, MultiBufferSource bufferSource, RenderType renderType, VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        super.defaultRender(poseStack, animatable, bufferSource, RenderType.eyes(this.getTextureLocation(animatable)), buffer, yaw, partialTick, packedLight);
    }

    @Override
    public void renderFinal(PoseStack poseStack, FlowerCrownItem animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        var renderType = RenderType.eyes(this.getTextureLocation(animatable));
        this.reRender(model, poseStack, bufferSource, animatable, renderType, bufferSource.getBuffer(renderType), partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public RenderType getRenderType(FlowerCrownItem animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
