package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.blocks.CinderFurnaceBlock;
import com.cgessinger.creaturesandbeasts.blocks.LizardEggBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class CNBBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(CreaturesAndBeasts.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<FlowerBlock> PINK_WATERLILY_BLOCK = BLOCKS.register("pink_waterlily_block", () -> new FlowerBlock(MobEffects.HEAL, 5, BlockBehaviour.Properties.copy(Blocks.POPPY).noCollission().instabreak().sound(SoundType.GRASS)));
    public static final RegistrySupplier<FlowerPotBlock> POTTED_PINK_WATERLILY = BLOCKS.register("potted_pink_waterlily", () -> new FlowerPotBlock(PINK_WATERLILY_BLOCK.get(), BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));
    public static final RegistrySupplier<FlowerBlock> LIGHT_PINK_WATERLILY_BLOCK = BLOCKS.register("light_pink_waterlily_block", () -> new FlowerBlock(MobEffects.HEAL, 5, BlockBehaviour.Properties.copy(Blocks.ALLIUM).noCollission().instabreak().sound(SoundType.GRASS)));
    public static final RegistrySupplier<FlowerPotBlock> POTTED_LIGHT_PINK_WATERLILY = BLOCKS.register("potted_light_pink_waterlily", () -> new FlowerPotBlock(LIGHT_PINK_WATERLILY_BLOCK.get(), BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));
    public static final RegistrySupplier<FlowerBlock> YELLOW_WATERLILY_BLOCK = BLOCKS.register("yellow_waterlily_block", () -> new FlowerBlock(MobEffects.HEAL, 5, BlockBehaviour.Properties.copy(Blocks.DANDELION).noCollission().instabreak().sound(SoundType.GRASS)));
    public static final RegistrySupplier<FlowerPotBlock> POTTED_YELLOW_WATERLILY = BLOCKS.register("potted_yellow_waterlily", () -> new FlowerPotBlock(YELLOW_WATERLILY_BLOCK.get(), BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));

    public static final RegistrySupplier<Block> CINDER_FURNACE = BLOCKS.register("cinder_furnace", () -> new CinderFurnaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F)));

    public static RegistrySupplier<Block> LIZARD_EGGS = BLOCKS.register("lizard_egg_block", LizardEggBlock::new);
}
