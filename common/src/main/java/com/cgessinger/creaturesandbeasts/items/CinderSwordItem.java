package com.cgessinger.creaturesandbeasts.items;

import com.cgessinger.creaturesandbeasts.init.CNBItems;
import com.cgessinger.creaturesandbeasts.util.CNBRegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class CinderSwordItem extends Item {
    private static final String IMBUED_TICKS_TAG = "ImbuedTicks";

    private static final List<CNBRegistrySupplier<CinderSwordItem>> IMBUE_TIERS = List.of(CNBItems.CINDER_SWORD, CNBItems.CINDER_SWORD_1, CNBItems.CINDER_SWORD_2, CNBItems.CINDER_SWORD_3, CNBItems.CINDER_SWORD_4);
    private final int imbueLevel;

    public CinderSwordItem(int imbueLevel, Properties properties) {
        super(properties);
        this.imbueLevel = imbueLevel;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity targetEntity, LivingEntity attackingEntity) {
        if (this.imbueLevel > 0) {
            targetEntity.igniteForSeconds(2 * this.imbueLevel);
        }

        super.hurtEnemy(stack, targetEntity, attackingEntity);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int imbuedTicks = tag.getIntOr(IMBUED_TICKS_TAG, 0);

        if (imbuedTicks > 0) {
            tag.putInt(IMBUED_TICKS_TAG, imbuedTicks - 1);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        } else if (this.imbueLevel > 0 && entity instanceof Player player) {
            ItemStack sword = stack.transmuteCopy(IMBUE_TIERS.get(this.imbueLevel - 1).get());

            if (this.imbueLevel == 1) {
                player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
            }

            tag.putInt(IMBUED_TICKS_TAG, 400);
            sword.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            if (slot != null) {
                player.setItemSlot(slot, sword);
            } else {
                Inventory inventory = player.getInventory();

                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    if (inventory.getItem(i) == stack) {
                        inventory.setItem(i, sword);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockPos pos = blockhitresult.getBlockPos();

        if (level.getFluidState(pos).is(Fluids.LAVA)) {
            ItemStack imbuedSword = itemstack.transmuteCopy(IMBUE_TIERS.get(IMBUE_TIERS.size() - 1).get());
            CompoundTag tag = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putInt(IMBUED_TICKS_TAG, 400);
            imbuedSword.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.setItemInHand(hand, imbuedSword);
            player.playSound(SoundEvents.BUCKET_FILL_LAVA, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        return super.use(level, player, hand);
    }
}
