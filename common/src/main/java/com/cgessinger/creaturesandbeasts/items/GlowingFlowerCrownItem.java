package com.cgessinger.creaturesandbeasts.items;

import com.cgessinger.creaturesandbeasts.items.extensions.CanEnchantItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;

public class GlowingFlowerCrownItem extends FlowerCrownItem implements CanEnchantItem {
    public GlowingFlowerCrownItem(ArmorMaterial material, Ingredient repairItems, ArmorItem.Type slot, Properties properties) {
        super(material, repairItems, slot, properties);
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
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
