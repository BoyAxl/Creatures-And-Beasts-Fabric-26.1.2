package com.cgessinger.creaturesandbeasts.items;

import com.cgessinger.creaturesandbeasts.client.armor.render.SporelingBackpackRenderer;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.util.GeckoLibUtil;
import com.google.common.base.Suppliers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SporelingBackpackItem extends Item implements GeoItem {
    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);

    @Environment(EnvType.CLIENT)
    private final Supplier<GeoRenderProvider> renderer = Suppliers.memoize(() -> new GeoRenderProvider() {
        private final SporelingBackpackRenderer armorRenderer = new SporelingBackpackRenderer();

        @Override
        public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
            return this.armorRenderer;
        }
    });

    public SporelingBackpackItem(Properties properties) {
        super(properties);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(this.renderer.get());
    }

    @Override
    public Object getRenderProvider() {
        return this.renderer.get();
    }
}
