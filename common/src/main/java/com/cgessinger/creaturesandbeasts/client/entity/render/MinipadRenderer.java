package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.MinipadModel;
import com.cgessinger.creaturesandbeasts.entities.MinipadEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class MinipadRenderer extends GeoEntityRenderer<MinipadEntity> {
    public MinipadRenderer(EntityRendererProvider.Context context) {
        super(context, new MinipadModel());
        this.addRenderLayer(new MinipadGlowLayer(this));
        this.shadowRadius = 0.4F;
    }

    @Override
    public RenderType getRenderType(MinipadEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
