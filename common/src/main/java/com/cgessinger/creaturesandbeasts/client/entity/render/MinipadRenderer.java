package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.MinipadModel;
import com.cgessinger.creaturesandbeasts.entities.MinipadEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class MinipadRenderer extends GeoEntityRenderer<MinipadEntity, LivingEntityRenderState> {
    public MinipadRenderer(EntityRendererProvider.Context context) {
        super(context, new MinipadModel());
        this.withRenderLayer(new MinipadGlowLayer(this));
        this.shadowRadius = 0.4F;
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }
}
