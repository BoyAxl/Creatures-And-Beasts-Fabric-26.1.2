package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.containers.CinderFurnaceContainer;
import com.cgessinger.creaturesandbeasts.util.CNBDeferredRegister;
import com.cgessinger.creaturesandbeasts.util.CNBRegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class CNBContainerTypes {
    public static final CNBDeferredRegister<MenuType<?>> CONTAINER_TYPES = CNBDeferredRegister.create(CreaturesAndBeasts.MOD_ID, BuiltInRegistries.MENU);

    public static final CNBRegistrySupplier<MenuType<CinderFurnaceContainer>> CINDER_FURNACE_CONTAINER = CONTAINER_TYPES.register("cinder_furnace_container", () -> new MenuType<>(CinderFurnaceContainer::new, FeatureFlags.VANILLA_SET));
}
