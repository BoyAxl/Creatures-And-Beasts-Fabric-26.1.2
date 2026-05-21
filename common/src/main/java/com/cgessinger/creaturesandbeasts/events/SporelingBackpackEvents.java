package com.cgessinger.creaturesandbeasts.events;

import com.cgessinger.creaturesandbeasts.entities.SporelingEntity;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import com.cgessinger.creaturesandbeasts.mixin.EntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

class SporelingBackpackEvents {

    InteractionResult onUseEntity(Player player, Entity entity) {
        if (!(entity instanceof SporelingEntity sporelingEntity) || !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        if (!sporelingEntity.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }

        if (player.level().isClientSide()) {
            return InteractionResult.PASS;
        }

        this.tryMount(player, sporelingEntity);
        return InteractionResult.SUCCESS_SERVER;
    }

    InteractionResult onUseBlock(Player player, BlockHitResult hitResult) {
        SporelingEntity sporelingEntity = SporelingEntity.getBackpackPassenger(player);
        if (!player.isSecondaryUseActive() || sporelingEntity == null) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide()) {
            BlockPos targetPos = this.getDismountTarget(hitResult);
            Vec3 dismountLocation = this.findDismountLocation(player, sporelingEntity, targetPos);
            if (dismountLocation != null) {
                SporelingEntity.dropFromBackpack(player, sporelingEntity);
                this.syncPassengers(player);
            }
        }

        return player.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    boolean tryMount(Player player, SporelingEntity sporelingEntity) {
        if (!this.canMount(player, sporelingEntity)) {
            return false;
        }

        sporelingEntity.setPose(Pose.STANDING);
        ((EntityAccessor) sporelingEntity).creaturesAndBeasts$setVehicle(player);
        ((EntityAccessor) player).creaturesAndBeasts$addPassenger(sporelingEntity);
        sporelingEntity.setOrderedToSit(false);
        this.syncPassengers(player);
        return sporelingEntity.getVehicle() == player;
    }

    void onPlayerTick(Player player) {
        if (!this.hasBackpack(player) || player.isInWater()) {
            this.dropPassenger(player);
        }
    }

    private boolean canMount(Player player, SporelingEntity sporelingEntity) {
        return sporelingEntity.isTame()
                && sporelingEntity.isOwnedBy(player)
                && this.hasBackpack(player)
                && player.getPassengers().isEmpty()
                && !sporelingEntity.isPassenger();
    }

    private boolean hasBackpack(Player player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).is(CNBItems.SPORELING_BACKPACK.get());
    }

    private BlockPos getDismountTarget(BlockHitResult hitResult) {
        if (hitResult.getDirection() == Direction.UP) {
            return hitResult.getBlockPos().above();
        }

        return hitResult.getBlockPos().relative(hitResult.getDirection());
    }

    private Vec3 findDismountLocation(Player player, SporelingEntity sporelingEntity, BlockPos requestedPos) {
        BlockPos playerPos = player.blockPosition();
        if (this.isAdjacentToPlayer(playerPos, requestedPos)) {
            Vec3 requestedLocation = DismountHelper.findSafeDismountLocation(sporelingEntity.getType(), player.level(), requestedPos, true);
            if (requestedLocation != null) {
                return requestedLocation;
            }
        }

        for (int[] offset : DismountHelper.offsetsForDirection(player.getDirection())) {
            BlockPos candidatePos = playerPos.offset(offset[0], 0, offset[1]);
            Vec3 candidateLocation = DismountHelper.findSafeDismountLocation(sporelingEntity.getType(), player.level(), candidatePos, true);
            if (candidateLocation != null) {
                return candidateLocation;
            }
        }

        return null;
    }

    private boolean isAdjacentToPlayer(BlockPos playerPos, BlockPos requestedPos) {
        return Math.abs(requestedPos.getX() - playerPos.getX()) <= 1
                && Math.abs(requestedPos.getY() - playerPos.getY()) <= 1
                && Math.abs(requestedPos.getZ() - playerPos.getZ()) <= 1;
    }

    private void dropPassenger(Player player) {
        SporelingEntity sporelingEntity = SporelingEntity.getBackpackPassenger(player);
        if (sporelingEntity != null) {
            SporelingEntity.dropFromBackpack(player, sporelingEntity);
            this.syncPassengers(player);
        }
    }

    private void syncPassengers(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().sendToTrackingPlayersAndSelf(player, new ClientboundSetPassengersPacket(player));
        }
    }
}
