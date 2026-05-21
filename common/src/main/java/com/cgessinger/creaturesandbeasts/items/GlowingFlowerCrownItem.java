package com.cgessinger.creaturesandbeasts.items;

import com.cgessinger.creaturesandbeasts.items.extensions.CanEnchantItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class GlowingFlowerCrownItem extends FlowerCrownItem implements CanEnchantItem {
    public GlowingFlowerCrownItem(Properties properties) {
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
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
