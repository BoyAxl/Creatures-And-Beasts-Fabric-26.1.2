package com.cgessinger.creaturesandbeasts.events;

import com.cgessinger.creaturesandbeasts.entities.ThrownCactemSpearEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

class CactemSpearEvents {

    int onLootingCalculate(DamageSource damageSource) {
        if (damageSource.getDirectEntity() instanceof ThrownCactemSpearEntity thrownSpear) {
            Holder<Enchantment> looting = thrownSpear.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
            return EnchantmentHelper.getItemEnchantmentLevel(looting, thrownSpear.getSpear());
        }

        return -1;
    }
}
