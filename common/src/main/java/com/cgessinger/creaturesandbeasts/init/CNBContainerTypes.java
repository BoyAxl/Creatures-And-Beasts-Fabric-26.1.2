package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.containers.CinderFurnaceContainer;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class CNBContainerTypes {
    public static final DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(CreaturesAndBeasts.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<CinderFurnaceContainer>> CINDER_FURNACE_CONTAINER = CONTAINER_TYPES.register("cinder_furnace_container", () -> new MenuType<>(CinderFurnaceContainer::new, FeatureFlags.VANILLA_SET));
}
