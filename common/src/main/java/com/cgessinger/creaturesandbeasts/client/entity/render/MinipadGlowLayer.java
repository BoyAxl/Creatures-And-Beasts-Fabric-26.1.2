package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.MinipadEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

@Environment(EnvType.CLIENT)
public class MinipadGlowLayer extends GeoRenderLayer<MinipadEntity, Void, LivingEntityRenderState> {
    private static final DataTicket<Boolean> GLOWING = DataTickets.create("cnb_minipad_glowing", Boolean.class);
    private static final DataTicket<Boolean> SHEARED = DataTickets.create("cnb_minipad_sheared", Boolean.class);
    private static final DataTicket<Float> FLOWER_ALPHA = DataTickets.create("cnb_minipad_flower_alpha", Float.class);
    private static final DataTicket<Float> EYES_ALPHA = DataTickets.create("cnb_minipad_eyes_alpha", Float.class);
    private static final DataTicket<Identifier> FLOWER_TEXTURE = DataTickets.create("cnb_minipad_flower_texture", Identifier.class);
    private static final Identifier EYES_TEXTURE = CreaturesAndBeasts.id("textures/entity/minipad/minipad_eyes_glow.png");

    public MinipadGlowLayer(GeoRenderer<MinipadEntity, Void, LivingEntityRenderState> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void addRenderData(MinipadEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        long time = animatable.level().getDefaultClockTime() % 24000L;
        float flowerAlpha = (float)Math.pow((time - 18000L) / 5000F, 2);

        renderState.addGeckolibData(GLOWING, animatable.isGlowing());
        renderState.addGeckolibData(SHEARED, animatable.getSheared());
        renderState.addGeckolibData(FLOWER_ALPHA, flowerAlpha);
        renderState.addGeckolibData(EYES_ALPHA, 1F - flowerAlpha);
        renderState.addGeckolibData(FLOWER_TEXTURE, animatable.getMinipadType().getGlowTextureLocation());
    }

    @Override
    public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderInfo, SubmitNodeCollector submitNodeCollector) {
        if (!renderInfo.willRender() || !renderInfo.getOrDefaultGeckolibData(GLOWING, false)) {
            return;
        }

        if (!renderInfo.getOrDefaultGeckolibData(SHEARED, false)) {
            submitGlow(renderInfo, submitNodeCollector, renderInfo.getGeckolibData(FLOWER_TEXTURE), renderInfo.getOrDefaultGeckolibData(FLOWER_ALPHA, 0F));
        }

        submitGlow(renderInfo, submitNodeCollector, EYES_TEXTURE, renderInfo.getOrDefaultGeckolibData(EYES_ALPHA, 0F));
    }

    private void submitGlow(RenderPassInfo<LivingEntityRenderState> renderInfo, SubmitNodeCollector submitNodeCollector, Identifier texture, float alpha) {
        if (texture == null || alpha <= 0F) {
            return;
        }

        int previousColor = renderInfo.renderColor();
        int previousLight = renderInfo.packedLight();
        int previousOverlay = renderInfo.packedOverlay();
        int glowColor = ((Math.round(alpha * 255F) & 255) << 24) | 0xFFFFFF;

        renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, glowColor);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, LightCoordsUtil.FULL_BRIGHT);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_OVERLAY, OverlayTexture.NO_OVERLAY);

        getRenderer().submitRenderTasks(renderInfo, submitNodeCollector.order(1), RenderTypes.entityTranslucentEmissive(texture));

        renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, previousColor);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, previousLight);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_OVERLAY, previousOverlay);
    }
}
