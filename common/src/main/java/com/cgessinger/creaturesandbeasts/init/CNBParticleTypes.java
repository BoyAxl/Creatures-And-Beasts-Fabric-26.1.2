package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.client.particle.CactemHealParticle;
import com.cgessinger.creaturesandbeasts.client.particle.MinipadFlowerParticle;
import com.cgessinger.creaturesandbeasts.util.CNBDeferredRegister;
import com.cgessinger.creaturesandbeasts.util.CNBRegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class CNBParticleTypes {

    public static CNBDeferredRegister<ParticleType<?>> PARTICLE_TYPES = CNBDeferredRegister.create(CreaturesAndBeasts.MOD_ID, BuiltInRegistries.PARTICLE_TYPE);

    public static final CNBRegistrySupplier<SimpleParticleType> PINK_MINIPAD_FLOWER = PARTICLE_TYPES.register("pink_minipad_flower", () -> new SimpleParticleType(false) {});
    public static final CNBRegistrySupplier<SimpleParticleType> LIGHT_PINK_MINIPAD_FLOWER = PARTICLE_TYPES.register("light_pink_minipad_flower", () -> new SimpleParticleType(false) {});
    public static final CNBRegistrySupplier<SimpleParticleType> YELLOW_MINIPAD_FLOWER = PARTICLE_TYPES.register("yellow_minipad_flower", () -> new SimpleParticleType(false) {});
    public static final CNBRegistrySupplier<SimpleParticleType> CACTEM_HEAL_PARTICLE = PARTICLE_TYPES.register("heal", () -> new SimpleParticleType(false) {});

    @Environment(EnvType.CLIENT)
    public static void registerParticleFactories() {
        ParticleProviderRegistry.getInstance().register(PINK_MINIPAD_FLOWER.get(), MinipadFlowerParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(LIGHT_PINK_MINIPAD_FLOWER.get(), MinipadFlowerParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(YELLOW_MINIPAD_FLOWER.get(), MinipadFlowerParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(CACTEM_HEAL_PARTICLE.get(), CactemHealParticle.Factory::new);
    }
}
