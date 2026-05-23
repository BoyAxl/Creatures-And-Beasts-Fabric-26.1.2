package com.cgessinger.creaturesandbeasts.client.armor.render;

import com.cgessinger.creaturesandbeasts.client.armor.model.FlowerCrownModel;
import com.cgessinger.creaturesandbeasts.items.FlowerCrownItem;
import com.cgessinger.creaturesandbeasts.items.GlowingFlowerCrownItem;
import com.cgessinger.creaturesandbeasts.util.MinipadGlow;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class FlowerCrownRenderer extends GeoArmorRenderer<FlowerCrownItem, HumanoidRenderState> {
    public FlowerCrownRenderer() {
        super(new FlowerCrownModel());
    }

    @Override
    public void addRenderData(FlowerCrownItem animatable, RenderData renderData, HumanoidRenderState renderState, float partialTick) {
        if (!(animatable instanceof GlowingFlowerCrownItem)) {
            return;
        }

        float glowAlpha = MinipadGlow.getAlpha(renderData.entity().level().getDefaultClockTime(), partialTick);
        if (glowAlpha <= 0.0F) {
            return;
        }

        int lightLevel = Mth.clamp(Mth.ceil(glowAlpha * 15.0F), 0, 15);
        renderState.addGeckolibData(DataTickets.PACKED_LIGHT, LightCoordsUtil.pack(lightLevel, lightLevel));
    }

    @Override
    public String getBoneNameForSegment(HumanoidRenderState renderState, ArmorSegment segment) {
        return "group";
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<HumanoidRenderState> renderInfo, OrderedSubmitNodeCollector submitNodeCollector, RenderType renderType) {
        super.submitRenderTasks(renderInfo, submitNodeCollector, renderType);

        if (renderInfo.getOrDefaultGeckolibData(DataTickets.HAS_GLINT, false)) {
            super.submitRenderTasks(renderInfo, submitNodeCollector, RenderTypes.armorEntityGlint());
        }
    }
}
