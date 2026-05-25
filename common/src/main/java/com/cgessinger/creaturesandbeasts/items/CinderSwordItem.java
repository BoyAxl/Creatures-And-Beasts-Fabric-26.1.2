package com.cgessinger.creaturesandbeasts.items;

import com.cgessinger.creaturesandbeasts.init.CNBDataComponents;
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
    private static final int IMBUE_DURATION_TICKS = 400;
    private static final int FIRE_SECONDS_PER_IMBUE_LEVEL = 2;
    private static final String LEGACY_IMBUED_TICKS_TAG = "ImbuedTicks";
    private static final List<CNBRegistrySupplier<CinderSwordItem>> IMBUE_TIERS = List.of(CNBItems.CINDER_SWORD, CNBItems.CINDER_SWORD_1, CNBItems.CINDER_SWORD_2, CNBItems.CINDER_SWORD_3, CNBItems.CINDER_SWORD_4);

    private final int imbueLevel;

    public CinderSwordItem(int imbueLevel, Properties properties) {
        super(properties);
        this.imbueLevel = imbueLevel;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity targetEntity, LivingEntity attackingEntity) {
        if (this.imbueLevel > 0) {
            targetEntity.igniteForSeconds(FIRE_SECONDS_PER_IMBUE_LEVEL * this.imbueLevel);
        }

        super.hurtEnemy(stack, targetEntity, attackingEntity);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        int imbuedTicks = getImbuedTicks(stack);

        if (imbuedTicks > 0) {
            setImbuedTicks(stack, imbuedTicks - 1);
        } else if (this.imbueLevel > 0 && entity instanceof Player player) {
            downgradeImbue(stack, player, slot);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockPos pos = blockhitresult.getBlockPos();

        if (level.getFluidState(pos).is(Fluids.LAVA)) {
            player.setItemInHand(hand, createImbuedCopy(itemstack, maxImbueLevel()));
            player.playSound(SoundEvents.BUCKET_FILL_LAVA, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        return super.use(level, player, hand);
    }

    private void downgradeImbue(ItemStack stack, Player player, EquipmentSlot slot) {
        int nextImbueLevel = this.imbueLevel - 1;
        ItemStack sword = createImbuedCopy(stack, nextImbueLevel);

        if (this.imbueLevel == 1) {
            player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
        }

        replaceStack(player, stack, sword, slot);
    }

    private static ItemStack createImbuedCopy(ItemStack stack, int imbueLevel) {
        ItemStack sword = stack.transmuteCopy(IMBUE_TIERS.get(imbueLevel).get());
        setImbuedTicks(sword, imbueLevel > 0 ? IMBUE_DURATION_TICKS : 0);
        return sword;
    }

    private static void replaceStack(Player player, ItemStack oldStack, ItemStack newStack, EquipmentSlot slot) {
        if (slot != null) {
            player.setItemSlot(slot, newStack);
            return;
        }

        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i) == oldStack) {
                inventory.setItem(i, newStack);
                return;
            }
        }
    }

    private static int getImbuedTicks(ItemStack stack) {
        Integer imbuedTicks = stack.get(CNBDataComponents.CINDER_SWORD_IMBUED_TICKS.get());

        if (imbuedTicks != null) {
            return imbuedTicks;
        }

        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(LEGACY_IMBUED_TICKS_TAG, 0);
    }

    private static void setImbuedTicks(ItemStack stack, int imbuedTicks) {
        if (imbuedTicks > 0) {
            stack.set(CNBDataComponents.CINDER_SWORD_IMBUED_TICKS.get(), imbuedTicks);
        } else {
            stack.remove(CNBDataComponents.CINDER_SWORD_IMBUED_TICKS.get());
        }

        clearLegacyImbuedTicks(stack);
    }

    private static void clearLegacyImbuedTicks(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

        if (customData == null) {
            return;
        }

        CompoundTag tag = customData.copyTag();

        if (!tag.contains(LEGACY_IMBUED_TICKS_TAG)) {
            return;
        }

        tag.remove(LEGACY_IMBUED_TICKS_TAG);

        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private static int maxImbueLevel() {
        return IMBUE_TIERS.size() - 1;
    }
}
