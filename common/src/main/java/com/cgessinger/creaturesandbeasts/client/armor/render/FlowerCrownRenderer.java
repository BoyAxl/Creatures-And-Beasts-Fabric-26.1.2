package com.cgessinger.creaturesandbeasts.client.armor.render;

import com.cgessinger.creaturesandbeasts.client.armor.model.FlowerCrownModel;
import com.cgessinger.creaturesandbeasts.items.FlowerCrownItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@Environment(EnvType.CLIENT)
public class FlowerCrownRenderer extends GeoArmorRenderer<FlowerCrownItem> {
    public FlowerCrownRenderer() {
        super(new FlowerCrownModel());
    }

    @Override
    public GeoBone getHeadBone() {
        return this.getGeoModel().getBone("group").orElseThrow();
    }
}
