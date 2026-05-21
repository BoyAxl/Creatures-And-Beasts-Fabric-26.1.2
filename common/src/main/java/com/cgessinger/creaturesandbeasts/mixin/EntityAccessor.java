package com.cgessinger.creaturesandbeasts.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("vehicle")
    void creaturesAndBeasts$setVehicle(Entity vehicle);

    @Invoker("addPassenger")
    void creaturesAndBeasts$addPassenger(Entity passenger);
}
