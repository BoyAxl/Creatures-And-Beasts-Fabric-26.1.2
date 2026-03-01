package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.LilytadModel;
import com.cgessinger.creaturesandbeasts.entities.LilytadEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class LilytadRenderer extends GeoEntityRenderer<LilytadEntity> {
    public LilytadRenderer(EntityRendererProvider.Context context) {
        super(context, new LilytadModel());
        this.shadowRadius = 0.7F;
    }

    @Override
    public RenderType getRenderType(LilytadEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
