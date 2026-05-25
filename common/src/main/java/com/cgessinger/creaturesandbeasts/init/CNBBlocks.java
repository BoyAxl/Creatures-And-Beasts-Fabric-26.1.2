package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.blocks.CinderFurnaceBlock;
import com.cgessinger.creaturesandbeasts.blocks.GlowingMinipadFlowerBlock;
import com.cgessinger.creaturesandbeasts.blocks.LizardEggBlock;
import com.cgessinger.creaturesandbeasts.util.CNBDeferredRegister;
import com.cgessinger.creaturesandbeasts.util.CNBRegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class CNBBlocks {
    public static final CNBDeferredRegister<Block> BLOCKS = CNBDeferredRegister.create(CreaturesAndBeasts.MOD_ID, BuiltInRegistries.BLOCK);

    public static final CNBRegistrySupplier<FlowerBlock> PINK_MINIPAD_FLOWER_BLOCK = BLOCKS.register("pink_minipad_flower_block", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 5, flowerProperties("pink_minipad_flower_block", Blocks.PINK_TULIP)));
    public static final CNBRegistrySupplier<FlowerBlock> LIGHT_PINK_MINIPAD_FLOWER_BLOCK = BLOCKS.register("light_pink_minipad_flower_block", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 5, flowerProperties("light_pink_minipad_flower_block", Blocks.PINK_TULIP)));
    public static final CNBRegistrySupplier<FlowerBlock> YELLOW_MINIPAD_FLOWER_BLOCK = BLOCKS.register("yellow_minipad_flower_block", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 5, flowerProperties("yellow_minipad_flower_block", Blocks.DANDELION)));
    public static final CNBRegistrySupplier<FlowerBlock> PINK_MINIPAD_FLOWER_GLOW_BLOCK = BLOCKS.register("pink_minipad_flower_glow_block", () -> new GlowingMinipadFlowerBlock(MobEffects.INSTANT_HEALTH, 5, glowingFlowerProperties("pink_minipad_flower_glow_block", Blocks.PINK_TULIP), CNBParticleTypes.PINK_MINIPAD_FLOWER));
    public static final CNBRegistrySupplier<FlowerBlock> LIGHT_PINK_MINIPAD_FLOWER_GLOW_BLOCK = BLOCKS.register("light_pink_minipad_flower_glow_block", () -> new GlowingMinipadFlowerBlock(MobEffects.INSTANT_HEALTH, 5, glowingFlowerProperties("light_pink_minipad_flower_glow_block", Blocks.PINK_TULIP), CNBParticleTypes.LIGHT_PINK_MINIPAD_FLOWER));
    public static final CNBRegistrySupplier<FlowerBlock> YELLOW_MINIPAD_FLOWER_GLOW_BLOCK = BLOCKS.register("yellow_minipad_flower_glow_block", () -> new GlowingMinipadFlowerBlock(MobEffects.INSTANT_HEALTH, 5, glowingFlowerProperties("yellow_minipad_flower_glow_block", Blocks.DANDELION), CNBParticleTypes.YELLOW_MINIPAD_FLOWER));

    public static final CNBRegistrySupplier<FlowerBlock> PINK_WATERLILY_BLOCK = BLOCKS.register("pink_waterlily_block", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 5, blockProperties("pink_waterlily_block", BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY).noCollision().instabreak().sound(SoundType.GRASS))));
    public static final CNBRegistrySupplier<FlowerPotBlock> POTTED_PINK_WATERLILY = BLOCKS.register("potted_pink_waterlily", () -> new FlowerPotBlock(PINK_WATERLILY_BLOCK.get(), blockProperties("potted_pink_waterlily", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))));
    public static final CNBRegistrySupplier<FlowerBlock> LIGHT_PINK_WATERLILY_BLOCK = BLOCKS.register("light_pink_waterlily_block", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 5, blockProperties("light_pink_waterlily_block", BlockBehaviour.Properties.ofFullCopy(Blocks.ALLIUM).noCollision().instabreak().sound(SoundType.GRASS))));
    public static final CNBRegistrySupplier<FlowerPotBlock> POTTED_LIGHT_PINK_WATERLILY = BLOCKS.register("potted_light_pink_waterlily", () -> new FlowerPotBlock(LIGHT_PINK_WATERLILY_BLOCK.get(), blockProperties("potted_light_pink_waterlily", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))));
    public static final CNBRegistrySupplier<FlowerBlock> YELLOW_WATERLILY_BLOCK = BLOCKS.register("yellow_waterlily_block", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 5, blockProperties("yellow_waterlily_block", BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).noCollision().instabreak().sound(SoundType.GRASS))));
    public static final CNBRegistrySupplier<FlowerPotBlock> POTTED_YELLOW_WATERLILY = BLOCKS.register("potted_yellow_waterlily", () -> new FlowerPotBlock(YELLOW_WATERLILY_BLOCK.get(), blockProperties("potted_yellow_waterlily", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))));

    public static final CNBRegistrySupplier<Block> CINDER_FURNACE = BLOCKS.register("cinder_furnace", () -> new CinderFurnaceBlock(blockProperties("cinder_furnace", BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F))));

    public static CNBRegistrySupplier<Block> LIZARD_EGGS = BLOCKS.register("lizard_egg_block", () -> new LizardEggBlock(blockProperties("lizard_egg_block", BlockBehaviour.Properties.ofFullCopy(Blocks.TURTLE_EGG).mapColor(MapColor.SAND).strength(0.5F).sound(SoundType.METAL).randomTicks().noOcclusion())));

    private static BlockBehaviour.Properties flowerProperties(String path, Block block) {
        return blockProperties(path, BlockBehaviour.Properties.ofFullCopy(block).noCollision().instabreak().sound(SoundType.GRASS));
    }

    private static BlockBehaviour.Properties glowingFlowerProperties(String path, Block block) {
        return flowerProperties(path, block).lightLevel(GlowingMinipadFlowerBlock::getLightLevel);
    }

    private static BlockBehaviour.Properties blockProperties(String path, BlockBehaviour.Properties properties) {
        return properties.setId(CreaturesAndBeasts.blockKey(path));
    }
}
