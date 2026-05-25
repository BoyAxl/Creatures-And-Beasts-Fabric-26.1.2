package com.cgessinger.creaturesandbeasts;

import com.cgessinger.creaturesandbeasts.config.CNBConfig;
import com.cgessinger.creaturesandbeasts.entities.CactemEntity;
import com.cgessinger.creaturesandbeasts.entities.LilytadEntity;
import com.cgessinger.creaturesandbeasts.entities.LizardEntity;
import com.cgessinger.creaturesandbeasts.entities.MinipadEntity;
import com.cgessinger.creaturesandbeasts.entities.YetiEntity;
import com.cgessinger.creaturesandbeasts.events.CNBEvents;
import com.cgessinger.creaturesandbeasts.init.*;
import com.cgessinger.creaturesandbeasts.util.BiomeSpawns;
import com.cgessinger.creaturesandbeasts.util.CNBDeferredRegister;
import com.cgessinger.creaturesandbeasts.util.CNBRegistrySupplier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import org.infernalstudios.config.Config;

import java.io.IOException;
import java.util.function.Predicate;

public class CreaturesAndBeasts {
    private static CreaturesAndBeasts instance;
    private final ModLoaderProxy proxy;

    public static CreaturesAndBeasts getInstance() {
        return instance;
    }

    public static final String MOD_ID = "cnb";

    private static final CNBDeferredRegister<CreativeModeTab> TAB_REGISTRY = CNBDeferredRegister.create(MOD_ID, BuiltInRegistries.CREATIVE_MODE_TAB);
    public static final CNBRegistrySupplier<CreativeModeTab> TAB = TAB_REGISTRY.register("tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.cnb_tab"))
            .icon(() -> new ItemStack(CNBItems.SPORELING_OVERWORLD_EGG.get()))
            .displayItems((parameters, output) -> CNBItems.addCreativeTabItems(output))
            .build());

    private final CNBEvents events = new CNBEvents();

    public CreaturesAndBeasts(ModLoaderProxy proxy) {
        if (instance != null) {
            throw new IllegalStateException("Tried to initialize Creatures and Beasts twice!");
        }

        instance = this;
        this.proxy = proxy;

        TAB_REGISTRY.register();
        CNBParticleTypes.PARTICLE_TYPES.register();
        CNBBlocks.BLOCKS.register();
        CNBEntityTypes.ENTITY_TYPES.register();
        CNBItems.ITEMS.register();
        CNBContainerTypes.CONTAINER_TYPES.register();
        CNBSoundEvents.SOUND_EVENTS.register();

        CNBSporelingTypes.registerAll();
        CNBLizardTypes.registerAll();
        CNBLilytadTypes.registerAll();
        CNBMinipadTypes.registerAll();

        try {
            CNBConfig.CONFIG = Config
                    .builder(proxy.getConfigDir().resolve("creaturesandbeasts-common.toml"))
                    .loadClass(CNBConfig.class)
                    .build();
        } catch (IllegalStateException | IllegalArgumentException | IOException e) {
            throw new RuntimeException("Failed to load Creatures and Beasts config", e);
        }

        this.events.createEntityAttributes();

        ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers().forEach(this.events::onLivingTick));
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> this.events.onBackpackSporelingUseBlock(player, hitResult));
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> this.events.onBackpackSporelingUseEntity(player, entity));
    }

    public CNBEvents getEvents() {
        return events;
    }

    public ModLoaderProxy getProxy() {
        return proxy;
    }

    public void addSpawns(BiomeSpawns spawns) {
        this.addCactemSpawns(spawns);

        spawns.addSpawn(
            biome -> biome.is(Biomes.NETHER_WASTES),
            MobCategory.CREATURE,
            BiomeSpawns.spawn(CNBEntityTypes.CINDERSHELL.get(), 90, 2, 8),
            null
        );

        this.addEndWhaleSpawns(spawns);

        this.addLilytadSpawns(spawns);

        spawns.addSpawn(
            biome -> biome.is(BiomeTags.IS_RIVER),
            MobCategory.CREATURE,
            BiomeSpawns.spawn(CNBEntityTypes.LITTLE_GREBE.get(), 25, 2, 3),
            null
        );

        this.addLizardSpawns(spawns);

        this.addMinipadSpawns(spawns);

        spawns.addSpawn(
            biome -> biome.is(Biomes.MUSHROOM_FIELDS),
            MobCategory.CREATURE,
            BiomeSpawns.spawn(CNBEntityTypes.SPORELING.get(), 20, 3, 5),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.SWAMP),
            MobCategory.CREATURE,
            BiomeSpawns.spawn(CNBEntityTypes.SPORELING.get(), 25, 3, 5),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.LUSH_CAVES),
            MobCategory.CREATURE,
            BiomeSpawns.spawn(CNBEntityTypes.SPORELING.get(), 60, 3, 5),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.DARK_FOREST),
            MobCategory.CREATURE,
            BiomeSpawns.spawn(CNBEntityTypes.SPORELING.get(), 70, 3, 5),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.NETHER_WASTES),
            MobCategory.MONSTER,
            BiomeSpawns.spawn(CNBEntityTypes.SPORELING.get(), 60, 2, 4),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.WARPED_FOREST),
            MobCategory.MONSTER,
            BiomeSpawns.spawn(CNBEntityTypes.SPORELING.get(), 2, 2, 4),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.CRIMSON_FOREST),
            MobCategory.MONSTER,
            BiomeSpawns.spawn(CNBEntityTypes.SPORELING.get(), 120, 2, 4),
            null
        );

        this.addYetiSpawns(spawns);
    }

    private void addLilytadSpawns(BiomeSpawns spawns) {
        this.addCreatureAndWaterAmbientSpawns(
            spawns,
            LilytadEntity::isLilytadSpawnBiome,
            CNBEntityTypes.LILYTAD.get(),
            12,
            25,
            1,
            3
        );
    }

    private void addCactemSpawns(BiomeSpawns spawns) {
        EntityType<CactemEntity> cactem = CNBEntityTypes.CACTEM.get();
        this.addSpawn(spawns, CactemEntity::isCactemSpawnBiome, MobCategory.CREATURE, cactem, 8, 6, 13);
    }

    private void addEndWhaleSpawns(BiomeSpawns spawns) {
        spawns.addSpawn(
            biome -> biome.is(Biomes.END_HIGHLANDS)
                    || biome.is(Biomes.END_MIDLANDS)
                    || biome.is(Biomes.SMALL_END_ISLANDS)
                    || biome.is(Biomes.END_BARRENS),
            MobCategory.CREATURE,
            BiomeSpawns.spawn(CNBEntityTypes.END_WHALE.get(), 1, 1, 1),
            new MobSpawnSettings.MobSpawnCost(400, 1)
        );
    }

    private void addMinipadSpawns(BiomeSpawns spawns) {
        this.addCreatureAndWaterAmbientSpawns(
            spawns,
            MinipadEntity::isMinipadSpawnBiome,
            CNBEntityTypes.MINIPAD.get(),
            12,
            30,
            3,
            5
        );
    }

    private void addLizardSpawns(BiomeSpawns spawns) {
        EntityType<LizardEntity> lizard = CNBEntityTypes.LIZARD.get();
        this.addCreatureSpawn(spawns, biome -> biome.is(BiomeTags.IS_BADLANDS) || biome.is(Biomes.DESERT), lizard, 12, 1, 4);
        this.addCreatureSpawn(spawns, biome -> biome.is(BiomeTags.IS_JUNGLE), lizard, 45, 1, 4);
        this.addCreatureSpawn(spawns, biome -> biome.is(Biomes.MUSHROOM_FIELDS), lizard, 10, 1, 4);
    }

    private void addYetiSpawns(BiomeSpawns spawns) {
        EntityType<YetiEntity> yeti = CNBEntityTypes.YETI.get();
        this.addCreatureSpawn(
            spawns,
            biome -> biome.is(Biomes.SNOWY_PLAINS)
                    || biome.is(Biomes.ICE_SPIKES)
                    || biome.is(Biomes.SNOWY_TAIGA)
                    || biome.is(Biomes.SNOWY_SLOPES),
            yeti,
            1,
            2,
            3
        );
        this.addCreatureSpawn(spawns, biome -> biome.is(Biomes.FROZEN_PEAKS), yeti, 2, 2, 3);
    }

    private void addCreatureAndWaterAmbientSpawns(BiomeSpawns spawns, Predicate<Holder<Biome>> selector, EntityType<?> entityType, int creatureWeight, int waterAmbientWeight, int minCount, int maxCount) {
        this.addSpawn(spawns, selector, MobCategory.CREATURE, entityType, creatureWeight, minCount, maxCount);
        this.addSpawn(spawns, selector, MobCategory.WATER_AMBIENT, entityType, waterAmbientWeight, minCount, maxCount);
    }

    private void addCreatureSpawn(BiomeSpawns spawns, Predicate<Holder<Biome>> selector, EntityType<?> entityType, int weight, int minCount, int maxCount) {
        this.addSpawn(spawns, selector, MobCategory.CREATURE, entityType, weight, minCount, maxCount);
    }

    private void addSpawn(BiomeSpawns spawns, Predicate<Holder<Biome>> selector, MobCategory category, EntityType<?> entityType, int weight, int minCount, int maxCount) {
        spawns.addSpawn(
            selector,
            category,
            BiomeSpawns.spawn(entityType, weight, minCount, maxCount),
            null
        );
    }

    public void commonSetup() {
    }
    
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static ResourceKey<Item> itemKey(String path) {
        return ResourceKey.create(Registries.ITEM, id(path));
    }

    public static Item.Properties itemProperties(String path) {
        return new Item.Properties().setId(itemKey(path));
    }

    public static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(Registries.BLOCK, id(path));
    }
}
