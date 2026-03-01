package com.cgessinger.creaturesandbeasts.forge.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.function.Supplier;

public class ForgeArmorRenderer implements IClientItemExtensions {
    private final Supplier<GeoArmorRenderer<?>> rendererProvider;
    private GeoArmorRenderer<?> renderer;

    public ForgeArmorRenderer(Supplier<GeoArmorRenderer<?>> provider) {
        this.rendererProvider = provider;
    }

    @Override
    public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        if (this.renderer == null) {
            this.renderer = this.rendererProvider.get();
        }

        this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
        return this.renderer;
    }
}
