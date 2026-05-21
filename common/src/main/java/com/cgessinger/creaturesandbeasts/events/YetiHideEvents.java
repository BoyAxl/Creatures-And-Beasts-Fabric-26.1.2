package com.cgessinger.creaturesandbeasts.events;

import com.cgessinger.creaturesandbeasts.config.CNBConfig;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.Equippable;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.BiConsumer;

class YetiHideEvents {

    void onItemAttributeModifierCalculate(ItemStack input, EquipmentSlot slotType, BiConsumer<Holder<Attribute>, AttributeModifier> modifierConsumer) {
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

    Triple<Integer, Integer, ItemStack> onAnvilChange(ItemStack left, ItemStack right) {
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
        }

        return null;
    }
}
