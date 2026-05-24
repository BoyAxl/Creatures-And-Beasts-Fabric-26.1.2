package com.cgessinger.creaturesandbeasts.entities;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import com.cgessinger.creaturesandbeasts.init.CNBSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.animation.object.PlayState;
import com.geckolib.cache.animation.keyframeevent.ParticleKeyframeData;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import com.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class YetiEntity extends TamableAnimal implements GeoEntity, Enemy, NeutralMob {
    public static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(YetiEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> EATING = SynchedEntityData.defineId(YetiEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> PASSIVE = SynchedEntityData.defineId(YetiEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<ItemStack> HELD_ITEM = SynchedEntityData.defineId(YetiEntity.class, EntityDataSerializers.ITEM_STACK);

    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);
    private static final String STAY_MESSAGE_KEY = "entity.cnb.yeti.client_message.stay";
    private static final String FOLLOW_MESSAGE_KEY = "entity.cnb.yeti.client_message.follow";
    private static final Identifier HEALTH_REDUCTION_ID = Identifier.fromNamespaceAndPath(CreaturesAndBeasts.MOD_ID, "yeti_health_reduction");
    private static final Identifier ATTACK_DAMAGE_BONUS_ID = Identifier.fromNamespaceAndPath(CreaturesAndBeasts.MOD_ID, "yeti_attack_damage_bonus");
    private static final float SWEET_BERRY_MIN_HEAL_AMOUNT = 2.0F;
    private static final float SWEET_BERRY_MAX_HEAL_AMOUNT = 4.0F;
    private static final float WILD_ADULT_HEALTH = 40.0F;
    private static final float TAMED_ADULT_HEALTH = 60.0F;
    private static final float BABY_HEALTH = 24.0F;
    private static final float MIN_ATTACK_DAMAGE = 12.0F;
    private static final float MAX_ATTACK_DAMAGE = 16.0F;
    private static final double BABY_PROTECTION_RANGE = 8.0D;
    private static final double BABY_PROTECTION_VERTICAL_RANGE = 4.0D;
    private static final double BABY_THREAT_RANGE = 4.0D;
    private static final double BABY_THREAT_VERTICAL_RANGE = 2.0D;
    private static final int MELON_TAME_RETRY_INTERVAL_TICKS = 20 * 60;

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private long persistentAngerEndTime;
    @Nullable
    private EntityReference<LivingEntity> persistentAngerTarget;
    @Nullable
    private EntityReference<LivingEntity> melonFeeder;

    private int eatTimer;
    private int attackTimer;
    private long nextMelonTameRetryGameTime;

    public YetiEntity(EntityType<YetiEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setTame(false, false);
        this.eatTimer = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
        builder.define(EATING, false);
        builder.define(PASSIVE, false);
        builder.define(HELD_ITEM, ItemStack.EMPTY);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Passive", this.isPassive());
        output.storeNullable("MelonFeeder", EntityReference.codec(), this.melonFeeder);
        this.addPersistentAngerSaveData(output);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setPassive(input.getBooleanOr("Passive", false));
        this.melonFeeder = EntityReference.read(input, "MelonFeeder");
        this.migrateUntamedBabyOwnerReference();
        this.refreshMaxHealth(true);
        this.readPersistentAngerSaveData(this.level(), input);
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, WILD_ADULT_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, MIN_ATTACK_DAMAGE)
                .add(Attributes.ATTACK_SPEED, 0.1D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new YetiAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1.0D, 0.01F));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new YetiHurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(4, new ProtectBabyGoal(this));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        this.clearInvalidTarget();

        if (this.level() instanceof ServerLevel serverLevel) {
            this.updatePersistentAnger(serverLevel, true);
            if (this.melonFeeder != null && !this.isBaby() && this.level().getGameTime() >= this.nextMelonTameRetryGameTime) {
                this.tryCompleteMelonTame(serverLevel);
            }
        }

        if (this.isEating()) {
            this.navigation.stop();
            this.eatTimer--;
        }

        if (this.isAttacking()) {
            this.navigation.stop();
            this.attackTimer--;
        }

        if (this.eatTimer == 40) {
            boolean holdingMelon = this.getHolding().is(Items.MELON_SLICE);
            boolean holdingSweetBerries = this.getHolding().is(Items.SWEET_BERRIES);

            if (this.isBaby()) {
                if (holdingMelon) {
                    this.setPassive(true);
                }
                this.ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(-this.getAge()), true);
                if (!this.level().isClientSide() && (holdingMelon || holdingSweetBerries)) {
                    this.pacifyNearbyWildAdults();
                }
            }
            if (holdingMelon) {
                if (!this.isTame()) {
                    if (this.isBaby()) {
                        this.setTarget(null);
                        this.stopBeingAngry();
                        this.setPassive(true);
                    } else {
                        this.pacify();
                    }
                } else {
                    this.setTarget(null);
                    this.stopBeingAngry();
                }
            }
            this.setHolding(ItemStack.EMPTY);
        } else if (this.eatTimer == 0) {
            this.setEating(false);
        }

        if (this.attackTimer == 10 && !this.isDeadOrDying()) {
            this.performAttack();
        } else if (this.attackTimer == 0) {
            this.setAttacking(false);
        }
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }

    @Override
    public void setPersistentAngerEndTime(long angerEndTime) {
        this.persistentAngerEndTime = angerEndTime;
    }

    @Nullable
    @Override
    public EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> target) {
        this.persistentAngerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        return this.isTame() ? super.getOwner() : null;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.shouldRejectTarget(target)) {
            super.setTarget(null);
            return;
        }

        super.setTarget(target);
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        return !this.shouldRejectTarget(target) && super.wantsToAttack(target, owner);
    }

    @Override
    protected void applyTamingSideEffects() {
        super.applyTamingSideEffects();
        this.refreshMaxHealth(true);
    }

    private void refreshMaxHealth(boolean preserveHealthPercent) {
        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        float oldMaxHealth = this.getMaxHealth();
        float healthPercent = oldMaxHealth > 0.0F ? Math.min(1.0F, Math.max(0.0F, this.getHealth() / oldMaxHealth)) : 1.0F;

        maxHealth.removeModifier(HEALTH_REDUCTION_ID);
        maxHealth.setBaseValue(this.isTame() ? TAMED_ADULT_HEALTH : WILD_ADULT_HEALTH);

        if (this.isBaby()) {
            maxHealth.addOrUpdateTransientModifier(new AttributeModifier(HEALTH_REDUCTION_ID, BABY_HEALTH - maxHealth.getBaseValue(), AttributeModifier.Operation.ADD_VALUE));
        }

        float newHealth = preserveHealthPercent ? this.getMaxHealth() * healthPercent : Math.min(this.getHealth(), this.getMaxHealth());
        this.setHealth(newHealth);
    }

    private boolean isHostileProtectiveTarget(LivingEntity target) {
        return !(target instanceof YetiEntity) && target instanceof Enemy;
    }

    private boolean isBabyProtectionThreat(LivingEntity target) {
        return target instanceof Player || this.isHostileProtectiveTarget(target);
    }

    private boolean isOwnedBySameOwner(@Nullable LivingEntity entity, @Nullable LivingEntity owner) {
        return owner != null && entity instanceof TamableAnimal tamableAnimal && tamableAnimal.isOwnedBy(owner);
    }

    private boolean isPacifiedWildYetiTarget(@Nullable LivingEntity entity) {
        return this.isTame() && entity instanceof YetiEntity yeti && !yeti.isTame() && yeti.isPassive();
    }

    private boolean shouldRejectTarget(@Nullable LivingEntity target) {
        if (target == null) {
            return false;
        }

        LivingEntity owner = this.getOwner();
        return target == owner
                || this.isOwnedBySameOwner(target, owner)
                || this.isPacifiedWildYetiTarget(target)
                || (target instanceof YetiEntity yeti && yeti.isBaby())
                || (!this.isTame() && this.isPassive() && !this.isHostileProtectiveTarget(target));
    }

    private void clearInvalidTarget() {
        if (this.shouldRejectTarget(this.getTarget())) {
            super.setTarget(null);
            this.setAttacking(false);
            this.navigation.stop();
        }
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 10.0F;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, EntitySpawnReason reason, SpawnGroupData spawnDataIn) {
        if (spawnDataIn == null) {
            spawnDataIn = new AgeableMobGroupData(1.0F);
        }

        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (!this.isEating() && this.canUseMelonSlice(item)) {
            if (this.level().isClientSide()) {
                this.showBabyGrowthParticles();
                return InteractionResult.CONSUME;
            }

            if (this.isBaby()) {
                this.setPassive(true);
                this.melonFeeder = EntityReference.of(player);
                this.setOwnerReference(null);
            } else {
                this.pacify();
            }
            return this.startEat(player, hand, item);
        }

        if (!(this.isEating() || this.isAttacking())) {
            if (item.is(Items.SWEET_BERRIES)) {
                if (this.level().isClientSide()) {
                    this.showBabyGrowthParticles();
                    return InteractionResult.CONSUME;
                }

                if (this.canHealWithSweetBerries(item)) {
                    this.heal(this.getSweetBerryHealAmount());
                    return this.startEat(player, hand, item);
                } else if (this.canBreedWithSweetBerries(player)) {
                    this.setInLove(player);
                    return this.startEat(player, hand, item);
                } else if (this.isBaby()) {
                    return this.startEat(player, hand, item);
                }
            }
        }

        if (this.canToggleOrderedSitting(player)) {
            if (this.level().isClientSide()) {
                return InteractionResult.CONSUME;
            }

            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            this.setAttacking(false);
            this.sendOrderedSittingMessage(player);
            return InteractionResult.SUCCESS;
        }

        if (this.level().isClientSide()) {
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    private boolean canToggleOrderedSitting(Player player) {
        return this.isTame() && !this.isBaby() && this.isOwnedBy(player) && !this.isEating();
    }

    private void sendOrderedSittingMessage(Player player) {
        String message = this.isOrderedToSit() ? STAY_MESSAGE_KEY : FOLLOW_MESSAGE_KEY;
        player.sendOverlayMessage(Component.translatable(message, this.getName()));
    }

    private void showBabyGrowthParticles() {
        if (this.isBaby()) {
            this.ageUp(0, true);
        }
    }

    private void migrateUntamedBabyOwnerReference() {
        EntityReference<LivingEntity> ownerReference = this.getOwnerReference();
        if (!this.isTame() && ownerReference != null) {
            if (this.isBaby() && this.melonFeeder == null) {
                this.melonFeeder = ownerReference;
            }
            this.setOwnerReference(null);
        }
    }

    private void tryCompleteMelonTame(ServerLevel serverLevel) {
        if (this.isTame() || this.isBaby() || this.melonFeeder == null) {
            return;
        }

        UUID feederUuid = this.melonFeeder.getUUID();
        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(feederUuid);
        if (player != null) {
            this.tame(player);
            this.melonFeeder = null;
            this.setPassive(false);
            this.navigation.stop();
            this.setTarget(null);
            this.level().broadcastEntityEvent(this, (byte)7);
        } else {
            this.nextMelonTameRetryGameTime = this.level().getGameTime() + MELON_TAME_RETRY_INTERVAL_TICKS;
        }
    }

    private boolean canUseMelonSlice(ItemStack stack) {
        return stack.is(Items.MELON_SLICE) && !this.isTame() && !this.isPassive();
    }

    private boolean canHealWithSweetBerries(ItemStack stack) {
        return stack.is(Items.SWEET_BERRIES) && this.isTame() && !this.isBaby() && this.getHealth() < this.getMaxHealth();
    }

    private float getSweetBerryHealAmount() {
        return this.random.nextBoolean() ? SWEET_BERRY_MAX_HEAL_AMOUNT : SWEET_BERRY_MIN_HEAL_AMOUNT;
    }

    private boolean canBreedWithSweetBerries(Player player) {
        return this.getAge() == 0 && this.canFallInLove() && !this.isAngryAtPlayer(player);
    }

    private boolean isAngryAtPlayer(Player player) {
        if (this.getTarget() == player || this.getLastHurtByMob() == player) {
            return true;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            return this.isAngryAt(player, serverLevel);
        }
        return false;
    }

    @Override
    public void setAge(int age) {
        super.setAge(age);
        if (this.isBaby() && this.getMaxHealth() != BABY_HEALTH) {
            this.refreshMaxHealth(false);
        }
    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        this.refreshMaxHealth(true);
        this.setEating(false);
        this.setHolding(ItemStack.EMPTY);

        if (this.level() instanceof ServerLevel serverLevel) {
            this.tryCompleteMelonTame(serverLevel);
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return CNBEntityTypes.YETI.get().create(level, EntitySpawnReason.BREEDING);
    }

    public void setEating(boolean isEating) {
        this.eatTimer = isEating ? 60 : 0;
        this.entityData.set(EATING, isEating);
    }

    public boolean isEating() {
        return this.entityData.get(EATING);
    }

    public void setAttacking(boolean isAttacking) {
        this.entityData.set(ATTACKING, isAttacking);
        this.attackTimer = isAttacking ? 24 : 0;
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public boolean isPassive() {
        return this.entityData.get(PASSIVE);
    }

    public void setPassive(boolean isPassive) {
        this.entityData.set(PASSIVE, isPassive);
    }

    public ItemStack getHolding() {
        return this.entityData.get(HELD_ITEM);
    }

    public void setHolding(ItemStack stack) {
        this.entityData.set(HELD_ITEM, stack);
    }

    private InteractionResult startEat(Player player, InteractionHand hand, ItemStack stack) {
        this.setHolding(stack.copyWithCount(1));
        if (!this.level().isClientSide()) {
            this.usePlayerItem(player, hand, stack);
        }
        this.setEating(true);
        this.gameEvent(GameEvent.ENTITY_INTERACT, player);
        SoundEvent sound = this.isBaby() ? CNBSoundEvents.YETI_BABY_EAT.get() : CNBSoundEvents.YETI_ADULT_EAT.get();
        this.playSound(sound, 1.1F, 1F);
        return InteractionResult.SUCCESS;
    }

    private void pacifyNearbyWildAdults() {
        List<YetiEntity> nearbyYetis = this.level().getEntitiesOfClass(YetiEntity.class, this.getBoundingBox().inflate(BABY_PROTECTION_RANGE, BABY_PROTECTION_VERTICAL_RANGE, BABY_PROTECTION_RANGE));

        for (YetiEntity yeti : nearbyYetis) {
            if (!yeti.isBaby() && !yeti.isTame()) {
                yeti.pacify();
            }
        }
    }

    private void pacify() {
        this.setTarget(null);
        this.setLastHurtByMob(null);
        this.stopBeingAngry();
        this.setPassive(true);
        this.setAttacking(false);
        this.setOwnerReference(null);
        this.navigation.stop();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    private void performAttack() {
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.5D, 1.0D, 1.5D));

        for (LivingEntity entity : list) {
            if (this.shouldSkipAreaAttackTarget(entity)) {
                continue;
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                this.doHurtTarget(serverLevel, entity);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        AttributeInstance attackDamage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return super.doHurtTarget(level, target);
        }

        attackDamage.removeModifier(ATTACK_DAMAGE_BONUS_ID);
        attackDamage.addOrUpdateTransientModifier(new AttributeModifier(ATTACK_DAMAGE_BONUS_ID, this.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE));

        try {
            return super.doHurtTarget(level, target);
        } finally {
            attackDamage.removeModifier(ATTACK_DAMAGE_BONUS_ID);
        }
    }

    private float getAttackDamageBonus() {
        return this.random.nextInt((int) (MAX_ATTACK_DAMAGE - MIN_ATTACK_DAMAGE) + 1);
    }

    private boolean shouldSkipAreaAttackTarget(LivingEntity entity) {
        if (entity == this || (entity instanceof Player && this.isOwnedBy(entity))) {
            return true;
        }

        if (this.shouldRejectTarget(entity)) {
            return true;
        }

        if (entity instanceof YetiEntity yeti) {
            return this.getTarget() != yeti;
        }

        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean breaksPacification = this.shouldDamageBreakPacification(source);

        if (this.isBaby()) {
            List<YetiEntity> list = this.level().getEntitiesOfClass(YetiEntity.class, this.getBoundingBox().inflate(BABY_PROTECTION_RANGE, BABY_PROTECTION_VERTICAL_RANGE, BABY_PROTECTION_RANGE));

            for (YetiEntity yeti : list) {
                if (breaksPacification && !yeti.isBaby() && !yeti.isTame()) {
                    yeti.setPassive(false);
                    yeti.setOwnerReference(null);
                }
            }
        }

        if (breaksPacification && !this.isTame() && !this.isBaby()) {
            this.setPassive(false);
            this.setOwnerReference(null);
        }
        return super.hurtServer(level, source, amount);
    }

    private boolean shouldDamageBreakPacification(DamageSource source) {
        Entity attacker = source.getEntity();
        return attacker != null && !(attacker instanceof Enemy);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        if (!blockIn.getFluidState().isEmpty()) {
            this.playSound(CNBSoundEvents.YETI_STEP.get(), this.getSoundVolume() * 0.3F, this.getVoicePitch());
        }
    }

    @Override
    public float getVoicePitch() {
        float pitch = super.getVoicePitch();
        return this.isBaby() ? pitch * 1.5F : pitch;
    }

    @Override
    public int getMaxHeadYRot() {
        return 50;
    }

    @Override
    public int getMaxHeadXRot() {
        return 25;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? null : CNBSoundEvents.YETI_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isBaby() ? null : CNBSoundEvents.YETI_HURT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return this.isBaby() ? null : CNBSoundEvents.YETI_HURT.get();
    }

    private static final RawAnimation BABY_EAT_ANIMATION = RawAnimation.begin().thenPlay("yeti_baby_eat");
    private static final RawAnimation ADULT_EAT_ANIMATION = RawAnimation.begin().thenPlay("yeti_adult_eat");
    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("yeti_attack");
    private static final RawAnimation BABY_WALK_ANIMATION = RawAnimation.begin().thenLoop("yeti_baby_walk");
    private static final RawAnimation ADULT_WALK_ANIMATION = RawAnimation.begin().thenLoop("yeti_adult_walk");

    private <E extends GeoAnimatable> PlayState animationPredicate(AnimationTest<E> event) {
        double xMovement = this.getX() - this.xo;
        double zMovement = this.getZ() - this.zo;
        boolean isMoving = xMovement * xMovement + zMovement * zMovement > 1.0E-6D;

        if (this.isEating()) {
            event.controller().setAnimation(this.isBaby() ? BABY_EAT_ANIMATION : ADULT_EAT_ANIMATION);
        } else if (this.isAttacking()) {
            event.controller().setAnimation(ATTACK_ANIMATION);
        } else if (isMoving) {
            event.controller().setAnimation(this.isBaby() ? BABY_WALK_ANIMATION : ADULT_WALK_ANIMATION);
        } else {
            event.controller().reset();
            return PlayState.STOP;
        }

        return PlayState.CONTINUE;
    }

    private <E extends GeoAnimatable> void soundListener(KeyFrameEvent<E, SoundKeyframeData> event) {
        if (event.keyframeData().getSound().equals("hit.ground.sound")) {
            this.playSound(CNBSoundEvents.YETI_HIT.get(), 0.4F, 1.0F);
        } else if (event.keyframeData().getSound().equals("yeti_ambient")) {
            this.playSound(CNBSoundEvents.YETI_AMBIENT.get(), 1.0F, 1.0F);
        }
    }

    private <E extends GeoAnimatable> void particleListener(KeyFrameEvent<E, ParticleKeyframeData> event) {
        BlockPos pos = this.blockPosition();

        if (event.keyframeData().getEffect().equals("hit.ground.particle")) {
            for (int x = pos.getX() - 1; x <= pos.getX() + 1; x++) {
                for (int z = pos.getZ() - 1; z <= pos.getZ() + 1; z++) {
                    BlockPos newPos = new BlockPos(x, pos.getY() - 1, z);
                    this.level().addDestroyBlockEffect(newPos, this.level().getBlockState(newPos));
                }
            }
        } else if (event.keyframeData().getEffect().equals("eat.particle")) {
            spawnParticles(ParticleTypes.HAPPY_VILLAGER);
        }
    }

    public void spawnParticles(ParticleOptions data) {
        for (int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(data, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar animationData) {
        AnimationController<YetiEntity> controller = new AnimationController<>("controller", 0, this::animationPredicate);

        controller.setSoundKeyframeHandler(this::soundListener);
        controller.setParticleKeyframeHandler(this::particleListener);

        animationData.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    static class YetiHurtByTargetGoal extends HurtByTargetGoal {
        public YetiHurtByTargetGoal(YetiEntity yeti) {
            super(yeti);
        }

        @Override
        protected void alertOther(Mob mob, LivingEntity target) {
            if (mob instanceof YetiEntity yeti && (yeti.isTame() || yeti.shouldRejectTarget(target))) {
                return;
            }

            super.alertOther(mob, target);
        }
    }

    static class ProtectBabyGoal extends TargetGoal {
        private static final TargetingConditions THREAT_TARGETING = TargetingConditions.forCombat().range(BABY_PROTECTION_RANGE + BABY_THREAT_RANGE);
        private final YetiEntity yeti;
        @Nullable
        private LivingEntity threat;

        public ProtectBabyGoal(YetiEntity yeti) {
            super(yeti, true);
            this.yeti = yeti;
        }

        @Override
        public boolean canUse() {
            if (!this.canProtectBabies()) {
                return false;
            }

            this.threat = this.findThreatNearBaby();
            return this.threat != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canProtectBabies() && super.canContinueToUse();
        }

        @Override
        public void start() {
            this.yeti.setTarget(this.threat);
            super.start();
        }

        private boolean canProtectBabies() {
            return !this.yeti.isBaby() && !this.yeti.isTame();
        }

        @Nullable
        private LivingEntity findThreatNearBaby() {
            LivingEntity closestThreat = null;
            double closestDistance = Double.MAX_VALUE;
            List<YetiEntity> nearbyYetis = this.yeti.level().getEntitiesOfClass(YetiEntity.class, this.yeti.getBoundingBox().inflate(BABY_PROTECTION_RANGE, BABY_PROTECTION_VERTICAL_RANGE, BABY_PROTECTION_RANGE));

            for (YetiEntity baby : nearbyYetis) {
                if (!baby.isBaby()) {
                    continue;
                }

                List<LivingEntity> threats = baby.level().getEntitiesOfClass(LivingEntity.class, baby.getBoundingBox().inflate(BABY_THREAT_RANGE, BABY_THREAT_VERTICAL_RANGE, BABY_THREAT_RANGE));

                for (LivingEntity threat : threats) {
                    if (!this.isThreat(threat)) {
                        continue;
                    }

                    double distance = baby.distanceToSqr(threat);
                    if (distance < closestDistance) {
                        closestThreat = threat;
                        closestDistance = distance;
                    }
                }
            }

            return closestThreat;
        }

        private boolean isThreat(LivingEntity entity) {
            if (entity == this.yeti || !this.yeti.isBabyProtectionThreat(entity)) {
                return false;
            }

            return (!this.yeti.isPassive() || this.yeti.isHostileProtectiveTarget(entity)) && this.canAttack(entity, THREAT_TARGETING);
        }
    }

    static class YetiAttackGoal extends MeleeAttackGoal {
        private final YetiEntity yeti;

        public YetiAttackGoal(YetiEntity yeti, double speedModifier, boolean requiresLineOfSight) {
            super(yeti, speedModifier, requiresLineOfSight);
            this.yeti = yeti;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.yeti.isBaby() && !this.yeti.shouldRejectTarget(this.yeti.getTarget()) && super.canContinueToUse();
        }

        @Override
        public boolean canUse() {
            if (this.yeti.isBaby() || this.yeti.shouldRejectTarget(this.yeti.getTarget())) {
                return false;
            }
            return super.canUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity entity) {
            if (this.canPerformAttack(entity) && this.yeti.attackTimer <= 0) {
                this.resetAttackCooldown();
            }
        }

        @Override
        public void stop() {
            super.stop();
            this.yeti.setAttacking(false);
        }

        @Override
        protected void resetAttackCooldown() {
            this.ticksUntilNextAttack = this.adjustedTickDelay(25);
            this.yeti.setAttacking(true);
        }
    }
}
