package com.cgessinger.creaturesandbeasts.client.armor.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.items.SporelingBackpackItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class SporelingBackpackModel extends GeoModel<SporelingBackpackItem> {
    private final ResourceLocation SPORELING_BACKPACK_MODEL = CreaturesAndBeasts.id("geo/armor/sporeling_backpack.geo.json");
    private final ResourceLocation SPORELING_BACKPACK_TEXTURE = CreaturesAndBeasts.id("textures/armor/sporeling_backpack.png");
    private final ResourceLocation SPORELING_BACKPACK_ANIMATION = CreaturesAndBeasts.id("animations/sporeling_backpack.json");

    @Override
    public ResourceLocation getModelResource(SporelingBackpackItem object) {
        return SPORELING_BACKPACK_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SporelingBackpackItem object) {
        return SPORELING_BACKPACK_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SporelingBackpackItem animatable) {
        return null;
    }

}
