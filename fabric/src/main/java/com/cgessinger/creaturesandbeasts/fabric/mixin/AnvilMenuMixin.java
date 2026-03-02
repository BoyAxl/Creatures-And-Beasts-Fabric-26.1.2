package com.cgessinger.creaturesandbeasts.fabric.mixin;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;

    public AnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1))
    private void checkAnvilChange(CallbackInfo ci, @Local(ordinal = 0) ItemStack stack, @Local(ordinal = 1) ItemStack stack2, @Local(ordinal = 1) int j) {
        var result = CreaturesAndBeasts.getInstance().getEvents().onAnvilChange(stack, stack2);
        if (result != null) {
            this.cost.set(result.getLeft());
            this.repairItemCountCost = result.getMiddle();
            this.resultSlots.setItem(0, result.getRight());
        }
    }
}
