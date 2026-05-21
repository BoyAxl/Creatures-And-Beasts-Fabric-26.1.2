package com.cgessinger.creaturesandbeasts.util;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BiomeSpawns {
    private final List<SpawnData> spawns = new ArrayList<>();

    public void addSpawn(Predicate<Holder<Biome>> predicate, MobCategory category, WeightedSpawnerData spawnerData, MobSpawnSettings.MobSpawnCost spawnCost) {
        spawns.add(new SpawnData(predicate, category, spawnerData, spawnCost));
    }

    public List<SpawnData> getSpawns() {
        return spawns;
    }

    public static WeightedSpawnerData spawn(EntityType<?> type, int weight, int minCount, int maxCount) {
        return new WeightedSpawnerData(type, weight, minCount, maxCount);
    }

    public record WeightedSpawnerData(EntityType<?> type, int weight, int minCount, int maxCount) {
        public MobSpawnSettings.SpawnerData toSpawnerData() {
            return new MobSpawnSettings.SpawnerData(this.type, this.minCount, this.maxCount);
        }
    }

    public record SpawnData(
        Predicate<Holder<Biome>> selector,
        MobCategory category,
        WeightedSpawnerData spawnerData,
        @Nullable MobSpawnSettings.MobSpawnCost spawnCosts
    ) {}
}
