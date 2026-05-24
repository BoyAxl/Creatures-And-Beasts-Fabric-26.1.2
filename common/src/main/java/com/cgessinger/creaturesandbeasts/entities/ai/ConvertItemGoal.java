package com.cgessinger.creaturesandbeasts.entities.ai;

import com.cgessinger.creaturesandbeasts.entities.SporelingEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.pathfinder.Path;

import java.util.List;

public class ConvertItemGoal extends Goal {
    private static final int CONVERT_DURATION_TICKS = 54;
    private static final int CONVERT_COOLDOWN_TICKS = 20;
    private static final int CONVERT_PARTICLE_INTERVAL_TICKS = 3;

    protected Path path;
    protected ItemEntity itemInstance;
    protected double convertTime;
    protected double convertDelay;

    protected final double speed;
    protected final SporelingEntity entityIn;
    protected final double range;
    protected final PathNavigation navigation;

    public ConvertItemGoal(SporelingEntity entityIn, double range, double speedIn) {
        this.entityIn = entityIn;
        this.range = range;
        this.speed = speedIn;
        this.navigation = entityIn.getNavigation();
    }

    @Override
    public boolean canUse() {
        if (this.entityIn.isInSittingPose()) {
            return false;
        }
        if (this.itemInstance == null) {
            List<ItemEntity> list = this.entityIn.level().getEntitiesOfClass(ItemEntity.class, this.entityIn.getBoundingBox().inflate(this.range, 3.0D, this.range));

            for (ItemEntity item : list) {
                ItemStack stack = item.getItem();
                if (isConvertible(stack)) {
                    this.path = this.navigation.createPath(item.getOnPos(), 0);
                    this.itemInstance = item;
                    return path != null;
                }
            }
        }

        return false;
    }

    private boolean isConvertible(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(Items.DIRT) || (stack.isEnchanted() && hasCurse(stack)));
    }

    private boolean hasCurse(ItemStack stack) {
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            if (enchantment.is(EnchantmentTags.CURSE)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void stop() {
        this.itemInstance = null;
        this.path = null;
        this.navigation.stop();
        entityIn.setHolding(ItemStack.EMPTY);
        entityIn.setInspecting(false);
    }

    @Override
    public void start() {
        this.navigation.moveTo(this.path, this.speed);
    }

    @Override
    public boolean canContinueToUse() {
        return this.itemInstance != null
                && (!this.navigation.isDone() || this.convertTime > 0)
                && (this.entityIn.isInspecting() || !this.itemInstance.isRemoved());
    }

    public void convertItem() {
        entityIn.setInspecting(false);

        if (!(entityIn.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack heldItem = entityIn.getHolding();
        if (heldItem.isEmpty()) {
            return;
        }

        if (heldItem.is(Items.DIRT)) {
            entityIn.spawnAtLocation(serverLevel, new ItemStack(Items.MYCELIUM, 1));
        } else {
            ItemStack returnItem = heldItem.copy();

            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(returnItem);
            Holder<Enchantment> curse = null;
            for (Holder<Enchantment> enchantment : enchantments.keySet()) {
                if (enchantment.is(EnchantmentTags.CURSE)) {
                    curse = enchantment;
                    if (returnItem.isDamageableItem()) {
                        float percent = entityIn.getRandom().nextFloat() * 0.5F;
                        int damage = (int) (percent * returnItem.getMaxDamage() + returnItem.getDamageValue());
                        int setDamage = Math.min(damage, (int) (returnItem.getMaxDamage() * 0.9F));
                        returnItem.setDamageValue(Math.max(returnItem.getDamageValue(), setDamage));
                    }
                    break;
                }
            }
            if (curse != null) {
                Holder<Enchantment> curseToRemove = curse;
                EnchantmentHelper.updateEnchantments(returnItem, mutable -> mutable.removeIf(enchantment -> enchantment.equals(curseToRemove)));
            }

            entityIn.spawnAtLocation(serverLevel, returnItem);
            ExperienceOrb.award(serverLevel, entityIn.position(), entityIn.getRandom().nextInt(16) + 1);
        }

        entityIn.setHolding(ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        if (convertDelay <= 0) {
            if (this.entityIn.distanceToSqr(itemInstance) < 2.0D || this.entityIn.isInspecting()) {
                this.navigation.setSpeedModifier(0.0D);

                if (!this.entityIn.isInspecting() && !itemInstance.isRemoved()) {
                    this.entityIn.setHolding(itemInstance.getItem().copy());
                    itemInstance.getItem().shrink(1);

                    if (itemInstance.getItem().isEmpty()) {
                        itemInstance.discard();
                    }

                    entityIn.setInspecting(true);
                    entityIn.lookAt(EntityAnchorArgument.Anchor.EYES, itemInstance.position());
                    this.convertTime = CONVERT_DURATION_TICKS;

                } else {
                    if (--this.convertTime <= 0) {
                        convertItem();
                        convertDelay = CONVERT_COOLDOWN_TICKS;

                    } else if (convertTime % CONVERT_PARTICLE_INTERVAL_TICKS == 0) {
                        ItemStack heldItem = this.entityIn.getHolding();
                        if (heldItem.isEmpty()) {
                            return;
                        }

                        entityIn.lookAt(EntityAnchorArgument.Anchor.EYES, itemInstance.position());
                        entityIn.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, heldItem.getItem()), entityIn.getRandomX(0.5F) + entityIn.getLookAngle().x / 2.0D, entityIn.getRandomY(), entityIn.getRandomZ(0.5F) + entityIn.getLookAngle().z / 2.0D, 4D, 0D, 0D);
                    }
                }

            } else {
                this.navigation.setSpeedModifier(speed);
            }

        } else {
            convertDelay--;
        }
    }
}
