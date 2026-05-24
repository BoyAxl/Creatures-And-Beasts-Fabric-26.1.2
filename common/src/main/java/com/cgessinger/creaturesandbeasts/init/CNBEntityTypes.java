package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.*;
import com.cgessinger.creaturesandbeasts.util.CNBDeferredRegister;
import com.cgessinger.creaturesandbeasts.util.CNBRegistrySupplier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;

public class CNBEntityTypes {
    public static final CNBDeferredRegister<EntityType<?>> ENTITY_TYPES = CNBDeferredRegister.create(CreaturesAndBeasts.MOD_ID, BuiltInRegistries.ENTITY_TYPE);

    /* CREATURES */
    public static final CNBRegistrySupplier<EntityType<LittleGrebeEntity>> LITTLE_GREBE = ENTITY_TYPES.register("little_grebe", () -> FabricEntityType.Builder.createMob(LittleGrebeEntity::new, MobCategory.CREATURE, builder -> builder.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LittleGrebeEntity::checkGrebeSpawnRules)).sized(0.5f, 0.6f).build(entityKey("little_grebe")));
    public static final CNBRegistrySupplier<EntityType<LizardEntity>> LIZARD = ENTITY_TYPES.register("lizard", () -> FabricEntityType.Builder.createMob(LizardEntity::new, MobCategory.CREATURE, builder -> builder.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LizardEntity::checkLizardSpawnRules)).sized(0.52f, 0.3f).build(entityKey("lizard")));
    public static final CNBRegistrySupplier<EntityType<LilytadEntity>> LILYTAD = ENTITY_TYPES.register("lilytad", () -> FabricEntityType.Builder.createMob(LilytadEntity::new, MobCategory.WATER_AMBIENT, builder -> builder.spawnPlacement(SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.OCEAN_FLOOR, LilytadEntity::checkLilytadSpawnRules)).sized(0.7f, 1.02f).build(entityKey("lilytad")));
    public static final CNBRegistrySupplier<EntityType<SporelingEntity>> SPORELING = ENTITY_TYPES.register("sporeling", () -> FabricEntityType.Builder.createMob(SporelingEntity::new, MobCategory.CREATURE, builder -> builder.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SporelingEntity::checkSporelingSpawnRules)).sized(0.6f, 0.85f).build(entityKey("sporeling")));
    public static final CNBRegistrySupplier<EntityType<MinipadEntity>> MINIPAD = ENTITY_TYPES.register("minipad", () -> FabricEntityType.Builder.createMob(MinipadEntity::new, MobCategory.WATER_AMBIENT, builder -> builder.spawnPlacement(SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.OCEAN_FLOOR, MinipadEntity::checkMinipadSpawnRules)).sized(0.6f, 0.7f).build(entityKey("minipad")));
    public static final CNBRegistrySupplier<EntityType<EndWhaleEntity>> END_WHALE = ENTITY_TYPES.register("end_whale", () -> FabricEntityType.Builder.createMob(EndWhaleEntity::new, MobCategory.CREATURE, builder -> builder.spawnPlacement(SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndWhaleEntity::checkEndWhaleSpawnRules)).sized(3.0f, 1.5f).build(entityKey("end_whale")));
    public static final CNBRegistrySupplier<EntityType<CactemEntity>> CACTEM = ENTITY_TYPES.register("cactem", () -> FabricEntityType.Builder.createMob(CactemEntity::new, MobCategory.CREATURE, builder -> builder.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CactemEntity::checkCactemSpawnRules)).sized(0.75F, 1.0F).build(entityKey("cactem")));
    public static final CNBRegistrySupplier<EntityType<YetiEntity>> YETI = ENTITY_TYPES.register("yeti", () -> FabricEntityType.Builder.createMob(YetiEntity::new, MobCategory.CREATURE, builder -> builder.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, YetiEntity::checkMobSpawnRules)).sized(1.55f, 2.05f).build(entityKey("yeti")));
    public static final CNBRegistrySupplier<EntityType<CindershellEntity>> CINDERSHELL = ENTITY_TYPES.register("cindershell", () -> FabricEntityType.Builder.createMob(CindershellEntity::new, MobCategory.CREATURE, builder -> builder.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CindershellEntity::checkCindershellSpawnRules)).sized(1.25f, 1.45f).fireImmune().build(entityKey("cindershell")));

    /* PROJECTILES */
    public static final CNBRegistrySupplier<EntityType<LizardEggEntity>> LIZARD_EGG = ENTITY_TYPES.register("lizard_egg", () -> EntityType.Builder.<LizardEggEntity>of(LizardEggEntity::new, MobCategory.MISC).sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).build(entityKey("lizard_egg")));
    public static final CNBRegistrySupplier<EntityType<ThrownCactemSpearEntity>> THROWN_CACTEM_SPEAR = ENTITY_TYPES.register("thrown_cactem_spear", () -> EntityType.Builder.<ThrownCactemSpearEntity>of(ThrownCactemSpearEntity::new, MobCategory.MISC).sized(0.4F, 0.4F).clientTrackingRange(4).updateInterval(10).build(entityKey("cactem_spear")));

    private static ResourceKey<EntityType<?>> entityKey(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE, CreaturesAndBeasts.id(path));
    }
}
