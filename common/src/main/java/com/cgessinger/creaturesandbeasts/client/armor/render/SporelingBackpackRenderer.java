package com.cgessinger.creaturesandbeasts.client.armor.render;

import com.cgessinger.creaturesandbeasts.client.armor.model.SporelingBackpackModel;
import com.cgessinger.creaturesandbeasts.items.SporelingBackpackItem;
import com.geckolib.renderer.GeoArmorRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;

@Environment(EnvType.CLIENT)
public class SporelingBackpackRenderer extends GeoArmorRenderer<SporelingBackpackItem, HumanoidRenderState> {
    public SporelingBackpackRenderer() {
        super(new SporelingBackpackModel());
    }

    @Override
    public List<ArmorSegment> getSegmentsForSlot(HumanoidRenderState renderState, EquipmentSlot slot) {
        return slot == EquipmentSlot.CHEST ? List.of(ArmorSegment.CHEST) : List.of();
    }

    @Override
    public String getBoneNameForSegment(HumanoidRenderState renderState, ArmorSegment segment) {
        return "main";
    }
}
