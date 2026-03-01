package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.client.particle.CactemHealParticle;
import com.cgessinger.creaturesandbeasts.client.particle.MinipadFlowerParticle;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class CNBParticleTypes {

    public static DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(CreaturesAndBeasts.MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<SimpleParticleType> PINK_MINIPAD_FLOWER = PARTICLE_TYPES.register("pink_minipad_flower", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> LIGHT_PINK_MINIPAD_FLOWER = PARTICLE_TYPES.register("light_pink_minipad_flower", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> YELLOW_MINIPAD_FLOWER = PARTICLE_TYPES.register("yellow_minipad_flower", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> CACTEM_HEAL_PARTICLE = PARTICLE_TYPES.register("heal", () -> new SimpleParticleType(false) {});

    @Environment(EnvType.CLIENT)
    public static void registerParticleFactories() {
        ParticleProviderRegistry.register(PINK_MINIPAD_FLOWER.get(), MinipadFlowerParticle.Factory::new);
        ParticleProviderRegistry.register(LIGHT_PINK_MINIPAD_FLOWER.get(), MinipadFlowerParticle.Factory::new);
        ParticleProviderRegistry.register(YELLOW_MINIPAD_FLOWER.get(), MinipadFlowerParticle.Factory::new);
        ParticleProviderRegistry.register(CACTEM_HEAL_PARTICLE.get(), CactemHealParticle.Factory::new);
    }
}
