package com.cgessinger.creaturesandbeasts.items;

import com.cgessinger.creaturesandbeasts.client.armor.render.FlowerCrownRenderer;
import com.cgessinger.creaturesandbeasts.items.extensions.CanEnchantItem;
import com.google.common.base.Suppliers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FlowerCrownItem extends ArmorItem implements GeoItem, CanEnchantItem {
    private final Ingredient repairItems;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Environment(EnvType.CLIENT)
    private final Supplier<Object> renderer = Suppliers.memoize(FlowerCrownRenderer::new);

    public FlowerCrownItem(ArmorMaterial material, Ingredient repairItems, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
        this.repairItems = repairItems;
    }

    @Override
    public boolean isEnchantable(ItemStack p_41456_) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stackInput, ItemStack repairStack) {
        return this.repairItems.test(repairStack);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
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
