package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.util.CNBDeferredRegister;
import com.cgessinger.creaturesandbeasts.util.CNBRegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;

public final class CNBDataComponents {
    public static final CNBDeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = CNBDeferredRegister.create(CreaturesAndBeasts.MOD_ID, BuiltInRegistries.DATA_COMPONENT_TYPE);

    public static final CNBRegistrySupplier<DataComponentType<Integer>> CINDER_SWORD_IMBUED_TICKS = DATA_COMPONENT_TYPES.register("cinder_sword_imbued_ticks", () -> DataComponentType.<Integer>builder()
            .persistent(ExtraCodecs.NON_NEGATIVE_INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .ignoreSwapAnimation()
            .build());

    private CNBDataComponents() {
    }
}
