package com.cgessinger.creaturesandbeasts.events;

import com.cgessinger.creaturesandbeasts.config.CNBConfig;
import com.cgessinger.creaturesandbeasts.entities.*;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import com.cgessinger.creaturesandbeasts.items.HealSpellBookItem;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
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

    public InteractionResult onBackpackSporelingUse(Player player) {
        SporelingEntity sporelingEntity = SporelingEntity.getBackpackPassenger(player);
        if (player.isSecondaryUseActive() && sporelingEntity != null) {
            sporelingEntity.stopRiding();
            return player.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.PASS;
    }

    public int onLootingCalculate(DamageSource damageSource) {
        if (damageSource.getDirectEntity() instanceof ThrownCactemSpearEntity thrownSpear) {
            Holder<Enchantment> looting = thrownSpear.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
            return EnchantmentHelper.getItemEnchantmentLevel(looting, thrownSpear.getSpear());
        }

        return -1;
    }

    public void onLivingTick(LivingEntity entity) {
        if (entity instanceof Player player && !player.getItemBySlot(EquipmentSlot.CHEST).is(CNBItems.SPORELING_BACKPACK.get())) {
            SporelingEntity sporelingEntity = SporelingEntity.getBackpackPassenger(player);
            if (sporelingEntity != null) {
                sporelingEntity.stopRiding();
            }
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
