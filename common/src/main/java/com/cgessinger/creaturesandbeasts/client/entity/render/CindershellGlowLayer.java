package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.CindershellEntity;
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
public class CindershellGlowLayer extends GeoRenderLayer<CindershellEntity, Void, LivingEntityRenderState> {
    private static final DataTicket<Boolean> SHOULD_GLOW = DataTickets.create("cnb_cindershell_should_glow", Boolean.class);
    private static final Identifier GLOW_TEXTURE = CreaturesAndBeasts.id("textures/entity/cindershell/cindershell_glow.png");
    private static final int GLOW_COLOR = 0xFFFFFFFF;

    public CindershellGlowLayer(GeoRenderer<CindershellEntity, Void, LivingEntityRenderState> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void addRenderData(CindershellEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        renderState.addGeckolibData(SHOULD_GLOW, !animatable.isBaby());
    }

    @Override
    public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderInfo, SubmitNodeCollector submitNodeCollector) {
        if (!renderInfo.willRender() || !renderInfo.getOrDefaultGeckolibData(SHOULD_GLOW, false)) {
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
