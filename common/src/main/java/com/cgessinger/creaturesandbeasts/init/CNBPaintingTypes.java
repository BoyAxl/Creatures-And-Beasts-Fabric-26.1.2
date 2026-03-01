package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class CNBPaintingTypes {
    public static final DeferredRegister<PaintingVariant> PAINTINGS = DeferredRegister.create(CreaturesAndBeasts.MOD_ID, Registries.PAINTING_VARIANT);

    public static final RegistrySupplier<PaintingVariant> LILYTAD_PAINTING = PAINTINGS.register("lilytad", () -> new PaintingVariant(16, 16));
}
