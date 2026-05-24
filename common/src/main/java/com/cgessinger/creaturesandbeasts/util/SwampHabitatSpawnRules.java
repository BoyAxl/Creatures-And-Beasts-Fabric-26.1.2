package com.cgessinger.creaturesandbeasts.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;

public final class SwampHabitatSpawnRules {
    private static final double STILL_WATER_FLOW_EPSILON = 1.0E-7D;

    private SwampHabitatSpawnRules() {
    }

    public static boolean isNaturalSpawn(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;
    }

    public static boolean isSwampOrCaveBiome(Holder<Biome> biome) {
        return isSwampBiome(biome) || isCaveBiome(biome);
    }

    public static boolean isSwampBiome(Holder<Biome> biome) {
        return biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP);
    }

    public static boolean isCaveBiome(Holder<Biome> biome) {
        return biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.DRIPSTONE_CAVES);
    }

    public static boolean hasSwampSurfaceBiome(LevelAccessor level, BlockPos pos) {
        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);
        return isSwampBiome(level.getBiome(surfacePos));
    }

    public static boolean isSurfaceGroundSpawn(EntityType<?> entityType, LevelAccessor level, BlockPos pos, int surfaceTolerance) {
        return isNearSurface(level, pos, surfaceTolerance)
                && level.getRawBrightness(pos, 0) > 8
                && SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, entityType);
    }

    public static boolean hasStillWaterColumnToOpenSurface(EntityType<?> entityType, LevelAccessor level, BlockPos waterPos, int maxSearch) {
        BlockPos.MutableBlockPos mutablePos = waterPos.mutable();

        for (int checkedBlocks = 0; checkedBlocks <= maxSearch; ++checkedBlocks) {
            if (isOpenSurfaceAir(entityType, level, mutablePos)) {
                return true;
            }

            if (!isStillWater(level, mutablePos)) {
                return false;
            }

            mutablePos.move(0, 1, 0);
        }

        return false;
    }

    public static boolean isOneDeepStillWaterSpawn(EntityType<?> entityType, LevelAccessor level, BlockPos pos) {
        return SpawnPlacementTypes.IN_WATER.isSpawnPositionOk(level, pos, entityType)
                && isStillWater(level, pos)
                && level.getBlockState(pos.below()).isSolid()
                && isOpenSurfaceAir(entityType, level, pos.above());
    }

    public static boolean isStillWater(LevelAccessor level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        return fluidState.is(FluidTags.WATER)
                && fluidState.isSource()
                && fluidState.getFlow(level, pos).lengthSqr() <= STILL_WATER_FLOW_EPSILON;
    }

    public static boolean isOpenSurfaceAir(EntityType<?> entityType, LevelAccessor level, BlockPos pos) {
        return isValidEmptySpawnBlock(entityType, level, pos)
                && isValidEmptySpawnBlock(entityType, level, pos.above());
    }

    private static boolean isNearSurface(LevelAccessor level, BlockPos pos, int surfaceTolerance) {
        return pos.getY() >= level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).getY() - surfaceTolerance;
    }

    private static boolean isValidEmptySpawnBlock(EntityType<?> entityType, LevelAccessor level, BlockPos pos) {
        if (!level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }

        BlockState blockState = level.getBlockState(pos);
        return NaturalSpawner.isValidEmptySpawnBlock(level, pos, blockState, blockState.getFluidState(), entityType);
    }
}
