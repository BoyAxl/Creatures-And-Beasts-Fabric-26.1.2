package com.cgessinger.creaturesandbeasts.items;

import com.cgessinger.creaturesandbeasts.entities.ThrownCactemSpearEntity;
import com.cgessinger.creaturesandbeasts.init.CNBSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SpearItem extends Item {
    private static final int THROW_THRESHOLD_TICKS = 10;
    private static final float PROJECTILE_SPEED = 1.6F;
    private static final float PROJECTILE_INACCURACY = 1.0F;
    private static final float MULTISHOT_SPREAD_DEGREES = 10.0F;

    public SpearItem(Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.9D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TRIDENT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int useTicks) {
        if (entity instanceof Player player) {
            int i = this.getUseDuration(stack, entity) - useTicks;
            if (i >= THROW_THRESHOLD_TICKS && !level.isClientSide()) {
                stack.hurtAndBreak(1, player, entity.getUsedItemHand());
                spawnSpears(stack, player, level);

                if (!player.getAbilities().instabuild) {
                    player.getInventory().removeItem(stack);
                }
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            return true;
        }

        return false;
    }

    private void spawnSpears(ItemStack stack, Player player, Level level) {
        Holder<Enchantment> multishot = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT);
        Holder<Enchantment> loyalty = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOYALTY);
        int multishotLevel = EnchantmentHelper.getItemEnchantmentLevel(multishot, stack);
        int numberOfSpears = multishotLevel == 0 ? 1 : 3;
        float[] shotPitches = getShotPitches(player.getRandom());

        ItemStack noLoyaltyStack = stack.copy();
        EnchantmentHelper.updateEnchantments(noLoyaltyStack, enchantments -> enchantments.set(loyalty, 0));

        for (int i = 0; i < numberOfSpears; i++) {
            if (i == 0) {
                shootProjectile(level, player, stack, shotPitches[i], 0.0F, true);
            } else if (i == 1) {
                shootProjectile(level, player, noLoyaltyStack, shotPitches[i], -MULTISHOT_SPREAD_DEGREES, false);
            } else {
                shootProjectile(level, player, noLoyaltyStack, shotPitches[i], MULTISHOT_SPREAD_DEGREES, false);
            }
        }
    }

    private void shootProjectile(Level level, Player player, ItemStack stack, float soundVariation, float spreadDegrees, boolean canPickup) {
        ThrownCactemSpearEntity thrownSpear = new ThrownCactemSpearEntity(level, player, stack);
        Vec3 upVector = player.getUpVector(1.0F);
        float radians = spreadDegrees * Mth.DEG_TO_RAD;
        float halfAngleSin = Mth.sin(radians / 2.0F);
        Quaternionf quaternion = new Quaternionf(
                upVector.x() * halfAngleSin,
                upVector.y() * halfAngleSin,
                upVector.z() * halfAngleSin,
                Mth.cos(radians / 2.0F)
        );
        Vec3 viewVector = player.getViewVector(1.0F);
        Vector3f shotVector = viewVector.toVector3f();
        shotVector.rotate(quaternion);
        thrownSpear.shoot(shotVector.x(), shotVector.y(), shotVector.z(), PROJECTILE_SPEED, PROJECTILE_INACCURACY);

        if (player.getAbilities().instabuild) {
            thrownSpear.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        } else {
            thrownSpear.pickup = canPickup ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
        }

        level.addFreshEntity(thrownSpear);
        level.playSound(null, thrownSpear, CNBSoundEvents.SPEAR_THROW.get(), SoundSource.PLAYERS, 1.0F, soundVariation);
    }

    private static float[] getShotPitches(RandomSource rand) {
        boolean flag = rand.nextBoolean();
        return new float[]{1.0F, getRandomShotPitch(flag, rand), getRandomShotPitch(!flag, rand)};
    }

    private static float getRandomShotPitch(boolean chance, RandomSource rand) {
        float f = chance ? 0.63F : 0.43F;
        return 1.0F / (rand.nextFloat() * 0.5F + 1.8F) + f;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity hurtEntity, LivingEntity owner) {
        stack.hurtAndBreak(1, owner, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if ((double) state.getDestroySpeed(level, pos) != 0.0D) {
            stack.hurtAndBreak(2, entity, EquipmentSlot.MAINHAND);
        }

        return true;
    }
}
