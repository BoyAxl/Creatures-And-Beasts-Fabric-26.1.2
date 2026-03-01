package com.cgessinger.creaturesandbeasts.items.extensions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface CanEnchantItem {
    default boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category.canEnchant(stack.getItem());
    }
}
