package com.cgessinger.creaturesandbeasts.events;

import com.cgessinger.creaturesandbeasts.entities.*;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import com.cgessinger.creaturesandbeasts.items.CinderSwordItem;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.BiConsumer;

public class CNBEvents {

    private final SporelingBackpackEvents sporelingBackpackEvents = new SporelingBackpackEvents();
    private final CactemSpearEvents cactemSpearEvents = new CactemSpearEvents();
    private final YetiHideEvents yetiHideEvents = new YetiHideEvents();
    private final HealSpellBookEvents healSpellBookEvents = new HealSpellBookEvents();

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
        return this.sporelingBackpackEvents.onUseEntity(player, entity);
    }

    public InteractionResult onBackpackSporelingUseBlock(Player player, BlockHitResult hitResult) {
        return this.sporelingBackpackEvents.onUseBlock(player, hitResult);
    }

    public boolean tryMountBackpackSporeling(Player player, SporelingEntity sporelingEntity) {
        return this.sporelingBackpackEvents.tryMount(player, sporelingEntity);
    }

    public int onLootingCalculate(DamageSource damageSource) {
        return this.cactemSpearEvents.onLootingCalculate(damageSource);
    }

    public boolean onAllowLivingDeath(LivingEntity entity, DamageSource damageSource, float damageAmount) {
        CinderSwordItem.igniteKilledTarget(entity, damageSource);
        return true;
    }

    public void onLivingTick(LivingEntity entity) {
        if (entity instanceof Player player) {
            this.sporelingBackpackEvents.onPlayerTick(player);
        }
    }

    public void onItemAttributeModifierCalculate(ItemStack input, EquipmentSlot slotType, BiConsumer<Holder<Attribute>, AttributeModifier> modifierConsumer) {
        this.yetiHideEvents.onItemAttributeModifierCalculate(input, slotType, modifierConsumer);
    }

    public Triple<Integer, Integer, ItemStack> onAnvilChange(ItemStack left, ItemStack right) {
        Triple<Integer, Integer, ItemStack> yetiHideResult = this.yetiHideEvents.onAnvilChange(left, right);
        return yetiHideResult != null ? yetiHideResult : this.healSpellBookEvents.onAnvilChange(left, right);
    }
}
