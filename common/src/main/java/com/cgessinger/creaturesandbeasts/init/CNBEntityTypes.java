package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.*;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class CNBEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(CreaturesAndBeasts.MOD_ID, Registries.ENTITY_TYPE);

    /* CREATURES */
    public static final RegistrySupplier<EntityType<LittleGrebeEntity>> LITTLE_GREBE = ENTITY_TYPES.register("little_grebe", () -> EntityType.Builder.of(LittleGrebeEntity::new, MobCategory.CREATURE).sized(0.5f, 0.6f).build(CreaturesAndBeasts.id("little_grebe").toString()));
    public static final RegistrySupplier<EntityType<LizardEntity>> LIZARD = ENTITY_TYPES.register("lizard", () -> EntityType.Builder.of(LizardEntity::new, MobCategory.CREATURE).sized(0.52f, 0.3f).build(CreaturesAndBeasts.id("lizard").toString()));
    public static final RegistrySupplier<EntityType<LilytadEntity>> LILYTAD = ENTITY_TYPES.register("lilytad", () -> EntityType.Builder.of(LilytadEntity::new, MobCategory.CREATURE).sized(0.7f, 1.02f).build(CreaturesAndBeasts.id("lilytad").toString()));
    public static final RegistrySupplier<EntityType<SporelingEntity>> SPORELING = ENTITY_TYPES.register("sporeling", () -> EntityType.Builder.of(SporelingEntity::new, MobCategory.CREATURE).sized(0.6f, 0.85f).build(CreaturesAndBeasts.id("sporeling").toString()));
    public static final RegistrySupplier<EntityType<MinipadEntity>> MINIPAD = ENTITY_TYPES.register("minipad", () -> EntityType.Builder.of(MinipadEntity::new, MobCategory.CREATURE).sized(0.6f, 0.7f).build(CreaturesAndBeasts.id("minipad").toString()));
    public static final RegistrySupplier<EntityType<EndWhaleEntity>> END_WHALE = ENTITY_TYPES.register("end_whale", () -> EntityType.Builder.of(EndWhaleEntity::new, MobCategory.CREATURE).sized(3.0f, 1.5f).build(CreaturesAndBeasts.id("end_whale").toString()));
    public static final RegistrySupplier<EntityType<CactemEntity>> CACTEM = ENTITY_TYPES.register("cactem", () -> EntityType.Builder.of(CactemEntity::new, MobCategory.CREATURE).sized(0.75F, 1.0F).build(CreaturesAndBeasts.id("cactem").toString()));
    public static final RegistrySupplier<EntityType<YetiEntity>> YETI = ENTITY_TYPES.register("yeti", () -> EntityType.Builder.of(YetiEntity::new, MobCategory.CREATURE).sized(1.55f, 2.05f).build(CreaturesAndBeasts.id("yeti").toString()));
    public static final RegistrySupplier<EntityType<CindershellEntity>> CINDERSHELL = ENTITY_TYPES.register("cindershell", () -> EntityType.Builder.of(CindershellEntity::new, MobCategory.CREATURE).sized(1.25f, 1.45f).fireImmune().build(CreaturesAndBeasts.id("cindershell").toString()));

    /* PROJECTILES */
    public static final RegistrySupplier<EntityType<LizardEggEntity>> LIZARD_EGG = ENTITY_TYPES.register("lizard_egg", () -> EntityType.Builder.<LizardEggEntity>of(LizardEggEntity::new, MobCategory.MISC).sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).build(CreaturesAndBeasts.id("lizard_egg").toString()));
    public static final RegistrySupplier<EntityType<ThrownCactemSpearEntity>> THROWN_CACTEM_SPEAR = ENTITY_TYPES.register("thrown_cactem_spear", () -> EntityType.Builder.<ThrownCactemSpearEntity>of(ThrownCactemSpearEntity::new, MobCategory.MISC).sized(0.4F, 0.4F).clientTrackingRange(4).updateInterval(10).build(CreaturesAndBeasts.id("cactem_spear").toString()));
}
