package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.SporelingEntity;
import com.cgessinger.creaturesandbeasts.init.CNBSporelingTypes;
import com.cgessinger.creaturesandbeasts.util.SporelingType;
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
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SporelingGlowLayer extends GeoRenderLayer<SporelingEntity, Void, LivingEntityRenderState> {
    private static final DataTicket<Boolean> SHOULD_GLOW = DataTickets.create("cnb_sporeling_should_glow", Boolean.class);
    private static final DataTicket<Identifier> GLOW_TEXTURE = DataTickets.create("cnb_sporeling_glow_texture", Identifier.class);
    private static final Identifier CRIMSON_GLOW_TEXTURE = CreaturesAndBeasts.id("textures/entity/sporeling/sporeling_crimson_fungus_glow.png");
    private static final Identifier WARPED_GLOW_TEXTURE = CreaturesAndBeasts.id("textures/entity/sporeling/sporeling_warped_fungus_glow.png");
    private static final int GLOW_COLOR = 0xFFFFFFFF;

    public SporelingGlowLayer(GeoRenderer<SporelingEntity, Void, LivingEntityRenderState> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void addRenderData(SporelingEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        Identifier glowTexture = getGlowTexture(animatable.getSporelingType());
        renderState.addGeckolibData(SHOULD_GLOW, glowTexture != null);
        if (glowTexture != null) {
            renderState.addGeckolibData(GLOW_TEXTURE, glowTexture);
        }
    }

    @Override
    public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderInfo, SubmitNodeCollector submitNodeCollector) {
        Identifier glowTexture = renderInfo.getGeckolibData(GLOW_TEXTURE);
        if (!renderInfo.willRender() || !renderInfo.getOrDefaultGeckolibData(SHOULD_GLOW, false) || glowTexture == null) {
            return;
        }

        int previousColor = renderInfo.renderColor();
        int previousLight = renderInfo.packedLight();
        int previousOverlay = renderInfo.packedOverlay();

        renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, GLOW_COLOR);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, LightCoordsUtil.FULL_BRIGHT);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_OVERLAY, OverlayTexture.NO_OVERLAY);

        getRenderer().submitRenderTasks(renderInfo, submitNodeCollector.order(1), RenderTypes.entityTranslucentEmissive(glowTexture));

        renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, previousColor);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, previousLight);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_OVERLAY, previousOverlay);
    }

    @Nullable
    private static Identifier getGlowTexture(SporelingType sporelingType) {
        if (CNBSporelingTypes.CRIMSON_FUNGUS.equals(sporelingType)) {
            return CRIMSON_GLOW_TEXTURE;
        }

        if (CNBSporelingTypes.WARPED_FUNGUS.equals(sporelingType)) {
            return WARPED_GLOW_TEXTURE;
        }

        return null;
    }
}
