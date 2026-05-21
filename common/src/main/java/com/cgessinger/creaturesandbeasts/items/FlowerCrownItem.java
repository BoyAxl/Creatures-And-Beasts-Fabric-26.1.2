package com.cgessinger.creaturesandbeasts.items;

import com.cgessinger.creaturesandbeasts.client.armor.render.FlowerCrownRenderer;
import com.cgessinger.creaturesandbeasts.items.extensions.CanEnchantItem;
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
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FlowerCrownItem extends Item implements GeoItem, CanEnchantItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Environment(EnvType.CLIENT)
    private final Supplier<GeoRenderProvider> renderer = Suppliers.memoize(() -> new GeoRenderProvider() {
        private final FlowerCrownRenderer armorRenderer = new FlowerCrownRenderer();

        @Override
        public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
            return this.armorRenderer;
        }
    });

    public FlowerCrownItem(Properties properties) {
        super(properties);
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
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
