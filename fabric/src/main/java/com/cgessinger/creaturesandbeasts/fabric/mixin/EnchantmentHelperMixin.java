package com.cgessinger.creaturesandbeasts.fabric.mixin;

import com.cgessinger.creaturesandbeasts.items.extensions.CanEnchantItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @ModifyExpressionValue(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean checkCanEnchantItem(boolean original, @Local(argsOnly = true) ItemStack stack, @Local Enchantment enchantment) {
        if (stack.getItem() instanceof CanEnchantItem canEnchantItem) {
            return canEnchantItem.canApplyAtEnchantingTable(stack, enchantment);
        }

        return original;
    }
}
