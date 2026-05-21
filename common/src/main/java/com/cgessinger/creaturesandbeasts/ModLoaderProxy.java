package com.cgessinger.creaturesandbeasts;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;

public interface ModLoaderProxy {
    Path getConfigDir();

    // Custom event calls
    default boolean callAnimalTameEvent(Animal animal, Player tamer) {
        return false;
    }

    default void callPlayerSmeltedEvent(Player player, ItemStack stack) {
    }
}
