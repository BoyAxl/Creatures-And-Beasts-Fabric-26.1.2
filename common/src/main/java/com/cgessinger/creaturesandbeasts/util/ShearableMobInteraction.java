package com.cgessinger.creaturesandbeasts.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;

public final class ShearableMobInteraction {
    private ShearableMobInteraction() {
    }

    public static <T extends Mob & Shearable> Optional<InteractionResult> tryShear(T entity, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!itemStack.is(Items.SHEARS)) {
            return Optional.empty();
        }

        Level level = entity.level();
        if (level instanceof ServerLevel serverLevel && entity.readyForShearing()) {
            entity.shear(serverLevel, SoundSource.PLAYERS, itemStack);
            entity.gameEvent(GameEvent.SHEAR, player);
            itemStack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            return Optional.of(InteractionResult.SUCCESS_SERVER);
        }

        return Optional.of(InteractionResult.CONSUME);
    }
}
