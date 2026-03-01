package com.cgessinger.creaturesandbeasts;

import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;

public interface ModLoaderProxy {
    Path getConfigDir();

    // Custom event calls
    default boolean callAnimalTameEvent(Animal animal, Player tamer) {
        return !EntityEvent.ANIMAL_TAME.invoker().tame(animal, tamer).interruptsFurtherEvaluation();
    }

    default void callPlayerSmeltedEvent(Player player, ItemStack stack) {
        PlayerEvent.SMELT_ITEM.invoker().smelt(player, stack);
    }
}
