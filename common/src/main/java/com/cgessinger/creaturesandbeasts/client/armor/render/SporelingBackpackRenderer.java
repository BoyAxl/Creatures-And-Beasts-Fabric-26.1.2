package com.cgessinger.creaturesandbeasts.client.armor.render;

import com.cgessinger.creaturesandbeasts.client.armor.model.SporelingBackpackModel;
import com.cgessinger.creaturesandbeasts.items.SporelingBackpackItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@Environment(EnvType.CLIENT)
public class SporelingBackpackRenderer extends GeoArmorRenderer<SporelingBackpackItem> {

    public SporelingBackpackRenderer() {
        super(new SporelingBackpackModel());

        this.body = this.getGeoModel().getBone("main").orElseThrow();
    }
}
