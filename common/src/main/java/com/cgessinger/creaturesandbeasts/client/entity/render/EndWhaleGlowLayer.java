package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.EndWhaleEntity;
import com.geckolib.constant.DataTickets;
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
public class EndWhaleGlowLayer extends GeoRenderLayer<EndWhaleEntity, Void, LivingEntityRenderState> {
    private static final Identifier GLOW_TEXTURE = CreaturesAndBeasts.id("textures/entity/end_whale/end_whale_glow.png");
    private static final int GLOW_COLOR = 0xFFFFFFFF;

    public EndWhaleGlowLayer(GeoRenderer<EndWhaleEntity, Void, LivingEntityRenderState> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderInfo, SubmitNodeCollector submitNodeCollector) {
        if (!renderInfo.willRender()) {
            return;
        }

        int previousColor = renderInfo.renderColor();
        int previousLight = renderInfo.packedLight();
        int previousOverlay = renderInfo.packedOverlay();

        renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, GLOW_COLOR);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, LightCoordsUtil.FULL_BRIGHT);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_OVERLAY, OverlayTexture.NO_OVERLAY);

        getRenderer().submitRenderTasks(renderInfo, submitNodeCollector.order(1), RenderTypes.entityTranslucentEmissive(GLOW_TEXTURE));

        renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, previousColor);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, previousLight);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_OVERLAY, previousOverlay);
    }
}
