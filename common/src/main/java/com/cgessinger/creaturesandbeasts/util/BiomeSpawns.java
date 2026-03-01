package com.cgessinger.creaturesandbeasts.util;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BiomeSpawns {
    private final List<SpawnData> spawns = new ArrayList<>();

    public void addSpawn(Predicate<Holder<Biome>> predicate, MobCategory category, MobSpawnSettings.SpawnerData spawnerData, MobSpawnSettings.MobSpawnCost spawnCost) {
        spawns.add(new SpawnData(predicate, category, spawnerData, spawnCost));
    }

    public List<SpawnData> getSpawns() {
        return spawns;
    }

    public record SpawnData(
        Predicate<Holder<Biome>> selector,
        MobCategory category,
        MobSpawnSettings.SpawnerData spawnerData,
        @Nullable MobSpawnSettings.MobSpawnCost spawnCosts
    ) {}
}
