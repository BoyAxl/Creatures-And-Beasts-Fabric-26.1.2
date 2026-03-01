package com.cgessinger.creaturesandbeasts.events;

import com.cgessinger.creaturesandbeasts.config.CNBConfig;
import com.cgessinger.creaturesandbeasts.entities.*;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import com.cgessinger.creaturesandbeasts.items.HealSpellBookItem;
import dev.architectury.event.EventResult;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.UUID;
import java.util.function.BiConsumer;

public class CNBEvents {

    public void createEntityAttributes() {
        EntityAttributeRegistry.register(CNBEntityTypes.CINDERSHELL, CindershellEntity::createAttributes);
        EntityAttributeRegistry.register(CNBEntityTypes.SPORELING, SporelingEntity::createAttributes);
        EntityAttributeRegistry.register(CNBEntityTypes.LITTLE_GREBE, LittleGrebeEntity::createAttributes);
        EntityAttributeRegistry.register(CNBEntityTypes.LILYTAD, LilytadEntity::createAttributes);
        EntityAttributeRegistry.register(CNBEntityTypes.LIZARD, LizardEntity::createAttributes);
        EntityAttributeRegistry.register(CNBEntityTypes.YETI, YetiEntity::createAttributes);
        EntityAttributeRegistry.register(CNBEntityTypes.MINIPAD, MinipadEntity::createAttributes);
        EntityAttributeRegistry.register(CNBEntityTypes.END_WHALE, EndWhaleEntity::createAttributes);
        EntityAttributeRegistry.register(CNBEntityTypes.CACTEM, CactemEntity::createAttributes);
    }

    public EventResult onRightClickBlock(Player player) {
        if (player.isSecondaryUseActive() && player.getFirstPassenger() instanceof SporelingEntity sporelingEntity) {
            sporelingEntity.stopRiding();
            return EventResult.interruptTrue();
        }

        return EventResult.pass();
    }

    public int onLootingCalculate(DamageSource damageSource) {
        if (damageSource.getDirectEntity() instanceof ThrownCactemSpearEntity thrownSpear) {
            return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, thrownSpear.getSpear());
        }

        return -1;
    }

    public void onLivingTick(LivingEntity entity) {
        if (entity instanceof Player player && player.getFirstPassenger() instanceof SporelingEntity sporelingEntity && !player.getItemBySlot(EquipmentSlot.CHEST).is(CNBItems.SPORELING_BACKPACK.get())) {
            sporelingEntity.stopRiding();
        }
    }

	public void onItemAttributeModifierCalculate(ItemStack input, EquipmentSlot slotType, BiConsumer<Attribute, AttributeModifier> modifierConsumer) {
        CompoundTag tag = input.getTag();
        EquipmentSlot equipmentSlot = null;
        if (input.getItem() instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem) input.getItem();
            equipmentSlot = armorItem.getEquipmentSlot();
        }

		if (equipmentSlot != null && tag != null && slotType.equals(equipmentSlot) && tag.contains("HideAmount")) {
            int hideAmount = tag.getInt("HideAmount");

            if (equipmentSlot.equals(EquipmentSlot.HEAD)) {
                modifierConsumer.accept(Attributes.ARMOR, new AttributeModifier(UUID.fromString("96a6b318-81f1-475a-b4a4-b3da41d2711e"), "yeti_hide", CNBConfig.hideMultiplier * hideAmount, AttributeModifier.Operation.MULTIPLY_TOTAL));
            } else if (equipmentSlot.equals(EquipmentSlot.CHEST)) {
                modifierConsumer.accept(Attributes.ARMOR, new AttributeModifier(UUID.fromString("3f3136ff-4f04-4d62-a9cc-8d1f4175c1e2"), "yeti_hide", CNBConfig.hideMultiplier * hideAmount, AttributeModifier.Operation.MULTIPLY_TOTAL));
            } else if (equipmentSlot.equals(EquipmentSlot.LEGS)) {
                modifierConsumer.accept(Attributes.ARMOR, new AttributeModifier(UUID.fromString("f49d078c-2740-4283-8255-5d1f106efea0"), "yeti_hide", CNBConfig.hideMultiplier * hideAmount, AttributeModifier.Operation.MULTIPLY_TOTAL));
            } else {
                modifierConsumer.accept(Attributes.ARMOR, new AttributeModifier(UUID.fromString("b16e7c3f-508d-461d-8868-de6ee2a1314c"), "yeti_hide", CNBConfig.hideMultiplier * hideAmount, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
		}
	}

    public Triple<Integer, Integer, ItemStack> onAnvilChange(ItemStack left, ItemStack right) {
        if (left.getItem() instanceof ArmorItem && right.is(CNBItems.YETI_HIDE.get())) {
            ItemStack output = left.copy();
            CompoundTag nbt = output.getOrCreateTag();
            int hideAmount = 1;

            if (nbt.contains("HideAmount")) {
                hideAmount += nbt.getInt("HideAmount");

                if (hideAmount > CNBConfig.hideAmount) {
                    return null;
                }
            }

            nbt.putInt("HideAmount", hideAmount);
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

            output.setTag(left.getOrCreateTag());
            return new ImmutableTriple<>(cost, 1, output);
        }

        return null;
    }
}
