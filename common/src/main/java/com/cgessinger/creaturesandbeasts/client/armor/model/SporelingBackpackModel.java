package com.cgessinger.creaturesandbeasts.client.armor.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.items.SporelingBackpackItem;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class SporelingBackpackModel extends GeoModel<SporelingBackpackItem> {
    private static final Identifier SPORELING_BACKPACK_MODEL = CreaturesAndBeasts.id("geo/armor/sporeling_backpack");
    private static final Identifier SPORELING_BACKPACK_TEXTURE = CreaturesAndBeasts.id("textures/armor/sporeling_backpack.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return SPORELING_BACKPACK_MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return SPORELING_BACKPACK_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(SporelingBackpackItem animatable) {
        return null;
    }
}
