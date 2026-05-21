package com.cgessinger.creaturesandbeasts.events;

import com.cgessinger.creaturesandbeasts.init.CNBItems;
import com.cgessinger.creaturesandbeasts.items.HealSpellBookItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

class HealSpellBookEvents {

    Triple<Integer, Integer, ItemStack> onAnvilChange(ItemStack left, ItemStack right) {
        if (left.getItem() instanceof HealSpellBookItem && right.getItem() instanceof HealSpellBookItem && ItemStack.isSameItem(left, right)) {
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
