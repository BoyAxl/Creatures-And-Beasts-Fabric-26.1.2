package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.LittleGrebeModel;
import com.cgessinger.creaturesandbeasts.entities.LittleGrebeEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class LittleGrebeRenderer extends GeoEntityRenderer<LittleGrebeEntity> {

    public LittleGrebeRenderer(EntityRendererProvider.Context context) {
        super(context, new LittleGrebeModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public RenderType getRenderType(LittleGrebeEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
