package com.cgessinger.creaturesandbeasts.entities;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ThrownCactemSpearEntity extends AbstractArrow {
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownCactemSpearEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> IS_FOIL = SynchedEntityData.defineId(ThrownCactemSpearEntity.class, EntityDataSerializers.BOOLEAN);
    private boolean dealtDamage;
    public int clientSideReturnSpearTickCount;

    public ThrownCactemSpearEntity(EntityType<? extends ThrownCactemSpearEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownCactemSpearEntity(Level level, LivingEntity entity, ItemStack itemStack) {
        super(CNBEntityTypes.THROWN_CACTEM_SPEAR.get(), entity, level, copySpearOrDefault(itemStack), null);
        ItemStack spear = this.getSpear();
        this.entityData.set(IS_FOIL, spear.hasFoil());
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(spear));
        if (itemStack.isEmpty()) {
            CreaturesAndBeasts.LOGGER.info(
                    "Normalized empty Cactem spear projectile item at {} for {}",
                    this.blockPosition(),
                    entity.getScoreboardName()
            );
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_LOYALTY, (byte) 0);
        builder.define(IS_FOIL, false);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        if (this.getPickupItemStackOrigin().isEmpty()) {
            this.setPickupItemStack(this.getDefaultPickupItem());
            CreaturesAndBeasts.LOGGER.info(
                    "Repaired empty Cactem spear projectile item while loading at {}",
                    this.blockPosition()
            );
        }
        ItemStack spear = this.getSpear();
        this.dealtDamage = input.getBooleanOr("DealtDamage", false);
        this.entityData.set(IS_FOIL, spear.hasFoil());
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(spear));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("DealtDamage", this.dealtDamage);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity entity = this.getOwner();
        int i = this.entityData.get(ID_LOYALTY);
        if (i > 0 && (this.dealtDamage || this.isNoPhysics()) && entity != null) {
            if (!this.isAcceptibleReturnOwner()) {
                if (this.level() instanceof ServerLevel serverLevel && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(serverLevel, this.getPickupItem(), 0.1F);
                }

                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 vec3 = entity.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015D * (double)i, this.getZ());

                double d0 = 0.05D * (double)i;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(vec3.normalize().scale(d0)));
                if (this.clientSideReturnSpearTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                ++this.clientSideReturnSpearTickCount;
            }
        }
        super.tick();
    }

    @Override
    protected ItemStack getPickupItem() {
        return this.getSpear().copy();
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(CNBItems.CACTEM_SPEAR.get());
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    public boolean isFoil() {
        return this.entityData.get(IS_FOIL);
    }

    public ItemStack getSpear() {
        return this.getPickupItemStackOrigin();
    }

    private static ItemStack copySpearOrDefault(ItemStack stack) {
        return stack.isEmpty() ? new ItemStack(CNBItems.CACTEM_SPEAR.get()) : stack.copy();
    }

    @Nullable
    protected EntityHitResult findHitEntity(Vec3 vec1, Vec3 vec2) {
        return this.dealtDamage ? null : super.findHitEntity(vec1, vec2);
    }

    private boolean isAcceptibleReturnOwner() {
        Entity entity = this.getOwner();
        if (entity != null && entity.isAlive()) {
            return !(entity instanceof ServerPlayer) || !entity.isSpectator();
        } else {
            return false;
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        Entity hitEntity = hitResult.getEntity();
        float f = 5.0F;
        Entity projectileThrower = this.getOwner();
        DamageSource damagesource = this.damageSources().trident(this, (projectileThrower == null ? this : projectileThrower));
        if (this.level() instanceof ServerLevel serverLevel) {
            f = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), hitEntity, damagesource, f);
        }
        this.dealtDamage = true;
        SoundEvent soundevent = SoundEvents.TRIDENT_HIT;
        if (hitEntity.hurtOrSimulate(damagesource, f)) {
            if (hitEntity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffectsWithItemSourceOnBreak(serverLevel, hitEntity, damagesource, this.getWeaponItem(), item -> this.kill(serverLevel));
            }

            if (hitEntity instanceof LivingEntity hitLivingEntity) {
                this.doKnockback(hitLivingEntity, damagesource);
                this.doPostHurtEffects(hitLivingEntity);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        float f1 = 1.0F;

        this.playSound(soundevent, f1, 1.0F);
    }

    private byte getLoyaltyFromItem(ItemStack stack) {
        if (this.level() instanceof ServerLevel serverLevel) {
            return (byte) Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, stack, this), 0, 127);
        }
        return 0;
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    @Override
    public void tickDespawn() {
        int i = this.entityData.get(ID_LOYALTY);
        if (this.pickup != AbstractArrow.Pickup.ALLOWED || i <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    public boolean shouldRender(double p_37588_, double p_37589_, double p_37590_) {
        return true;
    }
}
