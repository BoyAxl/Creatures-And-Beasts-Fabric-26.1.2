package com.cgessinger.creaturesandbeasts.items;

import com.cgessinger.creaturesandbeasts.client.armor.render.SporelingBackpackRenderer;
import com.google.common.base.Suppliers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SporelingBackpackItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);

    @Environment(EnvType.CLIENT)
    private final Supplier<Object> renderer = Suppliers.memoize(SporelingBackpackRenderer::new);

    public SporelingBackpackItem(ArmorMaterial material, ArmorItem.Type slot, Properties properties) {
        super(material, slot, properties);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(this.renderer.get());
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderer;
    }
}
