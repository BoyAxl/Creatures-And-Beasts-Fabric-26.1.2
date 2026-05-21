package com.cgessinger.creaturesandbeasts.client.armor.render;

import com.cgessinger.creaturesandbeasts.client.armor.model.FlowerCrownModel;
import com.cgessinger.creaturesandbeasts.items.FlowerCrownItem;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

@Environment(EnvType.CLIENT)
public class FlowerCrownRenderer extends GeoArmorRenderer<FlowerCrownItem, HumanoidRenderState> {
    public FlowerCrownRenderer() {
        super(new FlowerCrownModel());
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
