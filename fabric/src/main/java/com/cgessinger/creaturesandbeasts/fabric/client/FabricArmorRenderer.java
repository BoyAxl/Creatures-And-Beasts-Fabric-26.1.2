package com.cgessinger.creaturesandbeasts.fabric.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.function.Supplier;

public class FabricArmorRenderer implements RenderProvider {
    private final Supplier<GeoArmorRenderer<?>> rendererProvider;
    private GeoArmorRenderer<?> renderer;

    public FabricArmorRenderer(Supplier<GeoArmorRenderer<?>> provider) {
        this.rendererProvider = provider;
    }

    @Override
    public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<LivingEntity> original) {
        if (this.renderer == null) {
            this.renderer = this.rendererProvider.get();
        }

        this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
        return this.renderer;
    }
}
