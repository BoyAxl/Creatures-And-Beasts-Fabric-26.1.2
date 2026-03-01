package com.cgessinger.creaturesandbeasts.entities.extensions;

import net.minecraft.world.entity.MobCategory;

public interface MultiCategoryEntity {
    MobCategory getClassification(boolean forSpawnCount);
}
