package com.cgessinger.creaturesandbeasts.events;

import com.cgessinger.creaturesandbeasts.config.CNBConfig;
import com.cgessinger.creaturesandbeasts.entities.*;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import com.cgessinger.creaturesandbeasts.items.HealSpellBookItem;
import com.cgessinger.creaturesandbeasts.mixin.EntityAccessor;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.BiConsumer;

public class CNBEvents {

    public void createEntityAttributes() {
        FabricDefaultAttributeRegistry.register(CNBEntityTypes.CINDERSHELL.get(), CindershellEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CNBEntityTypes.SPORELING.get(), SporelingEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CNBEntityTypes.LITTLE_GREBE.get(), LittleGrebeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CNBEntityTypes.LILYTAD.get(), LilytadEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CNBEntityTypes.LIZARD.get(), LizardEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CNBEntityTypes.YETI.get(), YetiEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CNBEntityTypes.MINIPAD.get(), MinipadEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CNBEntityTypes.END_WHALE.get(), EndWhaleEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CNBEntityTypes.CACTEM.get(), CactemEntity.createAttributes());
    }

    public InteractionResult onBackpackSporelingUseEntity(Player player, Entity entity) {
        if (!(entity instanceof SporelingEntity sporelingEntity) || !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        if (!sporelingEntity.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }

        if (player.level().isClientSide()) {
            return InteractionResult.PASS;
        }

        this.tryMountBackpackSporeling(player, sporelingEntity);
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult onBackpackSporelingUseBlock(Player player, BlockHitResult hitResult) {
        SporelingEntity sporelingEntity = SporelingEntity.getBackpackPassenger(player);
        if (!player.isSecondaryUseActive() || sporelingEntity == null) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide()) {
            BlockPos targetPos = this.getBackpackDismountTarget(hitResult);
            Vec3 dismountLocation = this.findBackpackDismountLocation(player, sporelingEntity, targetPos);
            if (dismountLocation != null) {
                SporelingEntity.dropFromBackpack(player, sporelingEntity);
                this.syncBackpackPassengers(player);
            }
        }

        return player.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    public boolean tryMountBackpackSporeling(Player player, SporelingEntity sporelingEntity) {
        if (!this.canMountBackpackSporeling(player, sporelingEntity)) {
            return false;
        }

        sporelingEntity.setPose(Pose.STANDING);
        ((EntityAccessor) sporelingEntity).creaturesAndBeasts$setVehicle(player);
        ((EntityAccessor) player).creaturesAndBeasts$addPassenger(sporelingEntity);
        sporelingEntity.setOrderedToSit(false);
        this.syncBackpackPassengers(player);
        return sporelingEntity.getVehicle() == player;
    }

    private boolean canMountBackpackSporeling(Player player, SporelingEntity sporelingEntity) {
        return sporelingEntity.isTame()
                && sporelingEntity.isOwnedBy(player)
                && this.hasSporelingBackpack(player)
                && player.getPassengers().isEmpty()
                && !sporelingEntity.isPassenger();
    }

    private boolean hasSporelingBackpack(Player player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).is(CNBItems.SPORELING_BACKPACK.get());
    }

    private BlockPos getBackpackDismountTarget(BlockHitResult hitResult) {
        if (hitResult.getDirection() == Direction.UP) {
            return hitResult.getBlockPos().above();
        }

        return hitResult.getBlockPos().relative(hitResult.getDirection());
    }

    private Vec3 findBackpackDismountLocation(Player player, SporelingEntity sporelingEntity, BlockPos requestedPos) {
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

    private void dropBackpackSporeling(Player player) {
        SporelingEntity sporelingEntity = SporelingEntity.getBackpackPassenger(player);
        if (sporelingEntity != null) {
            SporelingEntity.dropFromBackpack(player, sporelingEntity);
            this.syncBackpackPassengers(player);
        }
    }

    private void syncBackpackPassengers(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().sendToTrackingPlayersAndSelf(player, new ClientboundSetPassengersPacket(player));
        }
    }

    public int onLootingCalculate(DamageSource damageSource) {
        if (damageSource.getDirectEntity() instanceof ThrownCactemSpearEntity thrownSpear) {
            Holder<Enchantment> looting = thrownSpear.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
            return EnchantmentHelper.getItemEnchantmentLevel(looting, thrownSpear.getSpear());
        }

        return -1;
    }

    public void onLivingTick(LivingEntity entity) {
        if (entity instanceof Player player && (!this.hasSporelingBackpack(player) || player.isInWater())) {
            this.dropBackpackSporeling(player);
        }
    }

	public void onItemAttributeModifierCalculate(ItemStack input, EquipmentSlot slotType, BiConsumer<Holder<Attribute>, AttributeModifier> modifierConsumer) {
        CompoundTag tag = input.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        Equippable equippable = input.get(DataComponents.EQUIPPABLE);
        EquipmentSlot equipmentSlot = equippable == null ? null : equippable.slot();

		if (equipmentSlot != null && slotType.equals(equipmentSlot) && tag.contains("HideAmount")) {
            int hideAmount = tag.getIntOr("HideAmount", 0);

            if (equipmentSlot.equals(EquipmentSlot.HEAD)) {
                modifierConsumer.accept(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("cnb", "yeti_hide_head"), CNBConfig.hideMultiplier * hideAmount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else if (equipmentSlot.equals(EquipmentSlot.CHEST)) {
                modifierConsumer.accept(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("cnb", "yeti_hide_chest"), CNBConfig.hideMultiplier * hideAmount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else if (equipmentSlot.equals(EquipmentSlot.LEGS)) {
                modifierConsumer.accept(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("cnb", "yeti_hide_legs"), CNBConfig.hideMultiplier * hideAmount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else {
                modifierConsumer.accept(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("cnb", "yeti_hide_feet"), CNBConfig.hideMultiplier * hideAmount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
		}
	}

    public Triple<Integer, Integer, ItemStack> onAnvilChange(ItemStack left, ItemStack right) {
        if (left.has(DataComponents.EQUIPPABLE) && right.is(CNBItems.YETI_HIDE.get())) {
            ItemStack output = left.copy();
            CompoundTag nbt = output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            int hideAmount = 1;

            if (nbt.contains("HideAmount")) {
                hideAmount += nbt.getIntOr("HideAmount", 0);

                if (hideAmount > CNBConfig.hideAmount) {
                    return null;
                }
            }

            nbt.putInt("HideAmount", hideAmount);
            output.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            return new ImmutableTriple<>(CNBConfig.hideCost, 1, output);
        } else if (left.getItem() instanceof HealSpellBookItem && right.getItem() instanceof HealSpellBookItem && ItemStack.isSameItem(left, right)) {
            ItemStack output;
            int cost;
            if (left.is(CNBItems.HEAL_SPELL_BOOK_1.get())) {
                output = new ItemStack(CNBItems.HEAL_SPELL_BOOK_2.get());
                cost = 3;
            } else if (left.is(CNBItems.HEAL_SPELL_BOOK_2.get())) {
                output = new ItemStack(CNBItems.HEAL_SPELL_BOOK_3.get());
                cost = 6;
            } else {
                return null;
            }

            output.set(DataComponents.CUSTOM_DATA, left.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY));
            return new ImmutableTriple<>(cost, 1, output);
        }

        return null;
    }
}
