package com.cgessinger.creaturesandbeasts;

import com.cgessinger.creaturesandbeasts.config.CNBConfig;
import com.cgessinger.creaturesandbeasts.events.CNBEvents;
import com.cgessinger.creaturesandbeasts.init.*;
import com.cgessinger.creaturesandbeasts.util.BiomeSpawns;
import com.cgessinger.creaturesandbeasts.world.gen.ModEntitySpawns;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infernalstudios.config.Config;

import java.io.IOException;

public class CreaturesAndBeasts {
    private static CreaturesAndBeasts instance;
    private final ModLoaderProxy proxy;

    public static CreaturesAndBeasts getInstance() {
        return instance;
    }

    public static final String MOD_ID = "creaturesandbeasts";
    public static final Logger LOGGER = LogManager.getLogger();

    private static final DeferredRegister<CreativeModeTab> TAB_REGISTRY = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> TAB = TAB_REGISTRY.register("tab", () -> CreativeTabRegistry.create(Component.translatable("itemGroup.cnb_tab"), () -> new ItemStack(CNBItems.GREBE_SPAWN_EGG.get())));

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
        CNBItems.ITEMS.register();
        CNBContainerTypes.CONTAINER_TYPES.register();
        CNBPaintingTypes.PAINTINGS.register();
        CNBSoundEvents.SOUND_EVENTS.register();
        CNBEntityTypes.ENTITY_TYPES.register();

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

        CNBConfig.CONFIG.onReload(stage -> {
            if (stage == Config.ReloadStage.PRE) {
                CreaturesAndBeasts.LOGGER.debug("Reloading Creatures and Beasts config");
            }
        });

        this.events.createEntityAttributes();

        TickEvent.PLAYER_POST.register(this.events::onLivingTick);
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, interactionHand, blockPos, direction) -> this.events.onRightClickBlock(player));
    }

    public CNBEvents getEvents() {
        return events;
    }

    public ModLoaderProxy getProxy() {
        return proxy;
    }

    public void addSpawns(BiomeSpawns spawns) {
        spawns.addSpawn(
            biome -> biome.is(BiomeTags.IS_BADLANDS) || biome.is(Biomes.DESERT),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.CACTEM.get(), 3, 6, 13),
            null
        );

        spawns.addSpawn(
            biome -> biome.is(Biomes.NETHER_WASTES),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.CINDERSHELL.get(), 400, 2, 8),
            null
        );

        spawns.addSpawn(
            biome -> biome.is(BiomeTags.IS_END),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.END_WHALE.get(), 1, 1, 1),
            new MobSpawnSettings.MobSpawnCost(400, 1)
        );

        spawns.addSpawn(
            biome -> biome.is(Biomes.SWAMP),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.LILYTAD.get(), 45, 1, 1),
            null
        );

        spawns.addSpawn(
            biome -> biome.is(BiomeTags.IS_RIVER),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.LITTLE_GREBE.get(), 35, 2, 3),
            null
        );

        spawns.addSpawn(
            biome -> biome.is(BiomeTags.IS_BADLANDS) || biome.is(Biomes.DESERT),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.LIZARD.get(), 15, 1, 4),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(BiomeTags.IS_JUNGLE),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.LIZARD.get(), 100, 1, 4),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.MUSHROOM_FIELDS), // TODO: use common tag
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.LIZARD.get(), 10, 1, 4),
            null
        );

        spawns.addSpawn(
            biome -> biome.is(Biomes.SWAMP),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.MINIPAD.get(), 20, 3, 6),
            null
        );

        spawns.addSpawn(
            biome -> biome.is(Biomes.MUSHROOM_FIELDS),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.SPORELING.get(), 20, 3, 5),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.SWAMP),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.SPORELING.get(), 25, 3, 5),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.LUSH_CAVES),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.SPORELING.get(), 60, 3, 5),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.DARK_FOREST),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.SPORELING.get(), 70, 3, 5),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.NETHER_WASTES),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.SPORELING.get(), 60, 2, 4),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.WARPED_FOREST),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.SPORELING.get(), 2, 2, 4),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.CRIMSON_FOREST),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.SPORELING.get(), 120, 2, 4),
            null
        );

        spawns.addSpawn(
            biome -> biome.is(Biomes.SNOWY_PLAINS) || biome.is(Biomes.ICE_SPIKES) || biome.is(Biomes.SNOWY_TAIGA) || biome.is(Biomes.SNOWY_SLOPES),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.YETI.get(), 1, 2, 3),
            null
        );
        spawns.addSpawn(
            biome -> biome.is(Biomes.FROZEN_PEAKS),
            MobCategory.CREATURE,
            new MobSpawnSettings.SpawnerData(CNBEntityTypes.YETI.get(), 2, 2, 3),
            null
        );
    }

    public void commonSetup() {
        ModEntitySpawns.entitySpawnPlacementRegistry();
    }
    
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
