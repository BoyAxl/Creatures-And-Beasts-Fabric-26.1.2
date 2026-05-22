package com.cgessinger.creaturesandbeasts.entities;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import com.cgessinger.creaturesandbeasts.init.CNBParticleTypes;
import com.cgessinger.creaturesandbeasts.init.CNBSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.*;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.animation.object.PlayState;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import com.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class CactemEntity extends AgeableMob implements RangedAttackMob, GeoEntity {
    private static final EntityDataAccessor<Boolean> ELDER = SynchedEntityData.defineId(CactemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(CactemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SPEAR_SHOWN = SynchedEntityData.defineId(CactemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEALING = SynchedEntityData.defineId(CactemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TRADING = SynchedEntityData.defineId(CactemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEAL_FACING_LOCKED = SynchedEntityData.defineId(CactemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> HEAL_FACING_Y_ROT = SynchedEntityData.defineId(CactemEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEAL_FACING_X_ROT = SynchedEntityData.defineId(CactemEntity.class, EntityDataSerializers.FLOAT);

    private static final Identifier HEALTH_REDUCTION_ID = Identifier.fromNamespaceAndPath(CreaturesAndBeasts.MOD_ID, "cactem_health_reduction");
    private static final float ELDER_HEAL_TRIGGER_HEALTH_RATIO = 0.5F;
    private static final double ELDER_HEAL_VERTICAL_RANGE = 4.0D;
    private static final int ELDER_HEAL_EFFECT_DURATION = 100;
    private static final int ELDER_HEAL_EFFECT_AMPLIFIER = 1;
    private static final int ELDER_TRANSFORMATION_TICKS = 40;
    private static final int ELDER_TRANSFORMATION_EFFECT_TICK = 20;
    private static final double CACTEM_ALERT_VERTICAL_RANGE = 10.0D;
    private static final int CACTEM_TARGET_SHARE_INTERVAL = 10;
    private static final int CACTEM_ATTACK_DEBUG_INTERVAL = 40;
    private static final int THROW_ANIMATION_FINISH_GRACE_TICKS = 24;
    private static final int IDLE_2_ANIMATION_DELAY_TICKS = 10;
    private static final float WALK_ANIMATION_MOVING_SPEED = 0.075F;
    private static final float BABY_HEALTH = 20.0F;

    // Elder Goals
    private final RandomStrollGoal elderStrollGoal = new RandomStrollGoal(this, 0.65D);
    private final TradeGoal tradeGoal = new TradeGoal(this, 16.0D, 0.65D);
    private final HealGoal healGoal = new HealGoal(this, 0.65D, 100, 160, 16.0F, 7.0F);

    // Other Goals
    private final RandomStrollGoal randomStrollGoal = new RandomStrollGoal(this, 1.0D);
    private final FollowElderGoal followElderGoal = new FollowElderGoal(this, 1.0D);
    private final RangedSpearAttackGoal spearAttackGoal = new RangedSpearAttackGoal(this, 1.0D, 60, 16.0F);
    private final BecomeElderGoal becomeElderGoal = new BecomeElderGoal(this, 32.0F);

    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);

    private int healCooldown = 0;
    private int elderTransformationTicks = 0;
    private int lastWarriorDebugTick = Integer.MIN_VALUE;
    private int throwAnimationFinishTicks = 0;
    private int peacefulWarriorStationaryAnimationStartTick = Integer.MIN_VALUE;

    private boolean shouldUpdateGoals = false;

    public CactemEntity(EntityType<CactemEntity> entity, Level level) {
        super(entity, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ELDER, false);
        builder.define(ATTACKING, false);
        builder.define(SPEAR_SHOWN, true);
        builder.define(HEALING, false);
        builder.define(TRADING, false);
        builder.define(HEAL_FACING_LOCKED, false);
        builder.define(HEAL_FACING_Y_ROT, 0.0F);
        builder.define(HEAL_FACING_X_ROT, 0.0F);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        this.setElder(input.getBooleanOr("IsElder", false));
        if (!this.isElder() && input.getIntOr("Age", -24000) >= 0) {
            this.setItemInHand(this.getUsedItemHand(), new ItemStack(CNBItems.CACTEM_SPEAR.get()));
        }

        this.reassessGoals();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("IsElder", this.entityData.get(ELDER));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this, CactemEntity.class).setAlertOthers());
    }

    private void reassessGoals() {
        this.goalSelector.removeGoal(elderStrollGoal);
        this.goalSelector.removeGoal(randomStrollGoal);
        this.goalSelector.removeGoal(spearAttackGoal);
        this.goalSelector.removeGoal(followElderGoal);
        this.goalSelector.removeGoal(becomeElderGoal);
        this.goalSelector.removeGoal(tradeGoal);
        this.goalSelector.removeGoal(healGoal);

        if (this.isElder()) {
            this.goalSelector.addGoal(1, tradeGoal);
            this.goalSelector.addGoal(1, healGoal);
            this.goalSelector.addGoal(2, elderStrollGoal);
        } else if (!this.isBaby()){
            this.goalSelector.addGoal(1, spearAttackGoal);
            this.goalSelector.addGoal(1, followElderGoal);
            this.goalSelector.addGoal(2, randomStrollGoal);
            this.goalSelector.addGoal(5, becomeElderGoal);
        } else {
            this.goalSelector.addGoal(1, followElderGoal);
            this.goalSelector.addGoal(2, randomStrollGoal);
        }
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.shouldUpdateGoals) {
            this.reassessGoals();
            this.shouldUpdateGoals = false;
        }

        if (!this.level().isClientSide()) {
            this.clearInvalidCombatTarget();
            this.shareWarriorCombatTarget();
        }
        
        super.tick();

        if (!this.level().isClientSide()) {
            this.clearInvalidCombatTarget();
            this.clearStaleWarriorCombatState();
            this.logActiveWarriorCombatState();
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity previousTarget = this.getTarget();

        if (target != null && !this.canKeepCactemCombatTarget(target)) {
            this.forgetCombatTarget(target, "rejected");
            return;
        }

        super.setTarget(target);

        LivingEntity currentTarget = this.getTarget();
        if (!this.level().isClientSide() && previousTarget != currentTarget) {
            if (currentTarget == null) {
                if (previousTarget != null) {
                    this.resetWarriorCombatState();
                    CreaturesAndBeasts.LOGGER.info("Cactem at {} cleared combat target {}", this.blockPosition(), this.describeEntity(previousTarget));
                }
            } else {
                CreaturesAndBeasts.LOGGER.info("Cactem at {} accepted combat target {} distanceSqr={} followRange={}", this.blockPosition(), this.describeEntity(currentTarget), this.distanceToSqr(currentTarget), this.getAttributeValue(Attributes.FOLLOW_RANGE));
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isElder() && !this.getItemInHand(this.getUsedItemHand()).is(CNBItems.HEAL_SPELL_BOOK_1.get())) {
            this.setItemInHand(this.getUsedItemHand(), new ItemStack(CNBItems.HEAL_SPELL_BOOK_1.get()));
        }

        if (this.isHealing()) {
            spawnHealParticles();
        }

        if (!this.level().isClientSide()) {
            this.tickElderTransformationEffect();
        }

        if (this.healCooldown > 0) {
            this.healCooldown--;
        }
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return 3 + this.getRandom().nextInt(4);
    }

    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource damageSource, boolean hitByPlayer) {
        this.getLootTable().ifPresent(lootTable -> this.dropFromLootTable(level, damageSource, hitByPlayer, lootTable, itemStack -> {
            if (this.isElder() && itemStack.is(CNBItems.CACTEM_SPEAR.get())) {
                CreaturesAndBeasts.LOGGER.info("Skipped elder Cactem spear death drop at {}", this.blockPosition());
                return;
            }

            this.spawnAtLocation(level, itemStack);
        }));
    }

    @Override
    public boolean removeWhenFarAway(double p_21542_) {
        return !this.hasCustomName();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroup) {
        double elderChance = level.getRandom().nextDouble();

        if (spawnGroup == null) {
            spawnGroup = new AgeableMobGroupData(0.5F);
        }

        if (!this.isBaby()) {
            if (elderChance < 0.25) {
                this.setElder(true);
                this.setItemInHand(this.getUsedItemHand(), new ItemStack(CNBItems.HEAL_SPELL_BOOK_1.get()));
            } else {
                this.setItemInHand(this.getUsedItemHand(), new ItemStack(CNBItems.CACTEM_SPEAR.get()));
            }
        }

        this.reassessGoals();

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroup);
    }

    @Override
    public void performRangedAttack(LivingEntity entity, float damage) {
        ItemStack itemstack = this.getItemInHand(this.getUsedItemHand());
        ThrownCactemSpearEntity spearEntity = new ThrownCactemSpearEntity(this.level(), this, itemstack);
        double d0 = entity.getX() - this.getX();
        double d1 = entity.getY(0.3333333333333333D) - spearEntity.getY();
        double d2 = entity.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        spearEntity.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
        this.level().addFreshEntity(spearEntity);
    }

    private void performHeal(float range) {
        for (CactemEntity nearbyCactem : this.getCactemsInHealRange(range)) {
            nearbyCactem.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ELDER_HEAL_EFFECT_DURATION, ELDER_HEAL_EFFECT_AMPLIFIER));
        }
    }

    private List<CactemEntity> getCactemsInHealRange(float range) {
        return this.level().getEntitiesOfClass(CactemEntity.class, this.getHealArea(range));
    }

    private boolean isInHealRange(CactemEntity cactem, float range) {
        return this.getHealArea(range).intersects(cactem.getBoundingBox());
    }

    private AABB getHealArea(float range) {
        return this.getBoundingBox().inflate(range, ELDER_HEAL_VERTICAL_RANGE, range);
    }

    private boolean canTriggerElderHeal() {
        return !this.isElder() && this.getHealth() / this.getMaxHealth() <= ELDER_HEAL_TRIGGER_HEALTH_RATIO;
    }

    private boolean canFightAsWarrior() {
        return this.isAlive()
                && !this.isBaby()
                && !this.isElder()
                && this.getItemInHand(this.getUsedItemHand()).is(CNBItems.CACTEM_SPEAR.get());
    }

    private void alertNearbyCactemWarriors(LivingEntity target) {
        if (this.level().isClientSide() || !this.canFightAsWarrior() || !this.canKeepCactemCombatTarget(target)) {
            return;
        }

        double followDistance = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB alertArea = AABB.unitCubeFromLowerCorner(this.position()).inflate(followDistance, CACTEM_ALERT_VERTICAL_RANGE, followDistance);
        int alertedCount = 0;

        for (CactemEntity nearbyCactem : this.level().getEntitiesOfClass(CactemEntity.class, alertArea)) {
            if (nearbyCactem == this
                    || !nearbyCactem.canFightAsWarrior()
                    || nearbyCactem.getTarget() != null
                    || nearbyCactem.isAlliedTo(target)) {
                continue;
            }

            nearbyCactem.setTarget(target);
            if (nearbyCactem.getTarget() == target) {
                alertedCount++;
            }
        }

        if (alertedCount > 0) {
            CreaturesAndBeasts.LOGGER.info("Cactem warrior at {} alerted {} nearby warrior(s) toward {}", this.blockPosition(), alertedCount, target.getScoreboardName());
        }
    }

    private void clearInvalidCombatTarget() {
        LivingEntity target = this.getTarget();
        if (target == null || this.canKeepCactemCombatTarget(target)) {
            return;
        }

        this.forgetCombatTarget(target, "forgot");
    }

    private void shareWarriorCombatTarget() {
        if (!this.canFightAsWarrior() || this.tickCount % CACTEM_TARGET_SHARE_INTERVAL != 0) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target != null) {
            this.alertNearbyCactemWarriors(target);
            return;
        }

        LivingEntity sharedTarget = this.findNearbyWarriorCombatTarget();
        if (sharedTarget != null) {
            this.setTarget(sharedTarget);
            if (this.getTarget() == sharedTarget) {
                CreaturesAndBeasts.LOGGER.info("Cactem warrior at {} adopted nearby warrior target {}", this.blockPosition(), this.describeEntity(sharedTarget));
            }
        }
    }

    @Nullable
    private LivingEntity findNearbyWarriorCombatTarget() {
        double followDistance = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB alertArea = AABB.unitCubeFromLowerCorner(this.position()).inflate(followDistance, CACTEM_ALERT_VERTICAL_RANGE, followDistance);
        LivingEntity closestTarget = null;
        double closestWarriorDistance = Double.MAX_VALUE;

        for (CactemEntity nearbyCactem : this.level().getEntitiesOfClass(CactemEntity.class, alertArea)) {
            if (nearbyCactem == this || !nearbyCactem.canFightAsWarrior()) {
                continue;
            }

            LivingEntity nearbyTarget = nearbyCactem.getTarget();
            if (nearbyTarget == null || !nearbyCactem.canKeepCactemCombatTarget(nearbyTarget) || !this.canKeepCactemCombatTarget(nearbyTarget)) {
                continue;
            }

            double distance = this.distanceToSqr(nearbyCactem);
            if (distance < closestWarriorDistance) {
                closestWarriorDistance = distance;
                closestTarget = nearbyTarget;
            }
        }

        return closestTarget;
    }

    private void clearStaleWarriorCombatState() {
        if (!this.canFightAsWarrior() || this.getTarget() != null) {
            return;
        }

        if (!this.isAggressive() && !this.isAttacking() && !this.isUsingItem() && this.entityData.get(SPEAR_SHOWN)) {
            return;
        }

        this.resetWarriorCombatState();
        CreaturesAndBeasts.LOGGER.info("Cactem warrior at {} cleared stale combat state with no target state={}", this.blockPosition(), this.getWarriorDebugState());
    }

    private void logActiveWarriorCombatState() {
        if (!this.canFightAsWarrior() || this.getTarget() == null) {
            return;
        }

        this.logWarriorDebugState("active-target");
    }

    private void forgetCombatTarget(LivingEntity target, String reason) {
        super.setTarget(null);
        this.setLastHurtByMob(null);
        this.resetWarriorCombatState();
        CreaturesAndBeasts.LOGGER.info("Cactem at {} {} invalid combat target {} state={}", this.blockPosition(), reason, this.describeEntity(target), this.getWarriorDebugState());
    }

    private void resetWarriorCombatState() {
        this.setAggressive(false);
        this.setAttacking(false);
        this.setSpearShown(true);
        this.stopUsingItem();
        this.getNavigation().stop();
        this.getMoveControl().strafe(0.0F, 0.0F);
        this.spearAttackGoal.resetState();
    }

    private boolean canKeepCactemCombatTarget(@Nullable LivingEntity target) {
        if (!isValidCactemCombatTarget(target) || !this.canAttack(target)) {
            return false;
        }

        double followRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        return this.distanceToSqr(target) <= followRange * followRange;
    }

    private static boolean isValidCactemCombatTarget(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }

        return !(target instanceof Player player) || (!player.isSpectator() && !player.isCreative());
    }

    private void logWarriorDebugState(String reason) {
        if (this.level().isClientSide() || this.tickCount - this.lastWarriorDebugTick < CACTEM_ATTACK_DEBUG_INTERVAL) {
            return;
        }

        this.lastWarriorDebugTick = this.tickCount;
        CreaturesAndBeasts.LOGGER.info("Cactem warrior debug [{}] at {} {}", reason, this.blockPosition(), this.getWarriorDebugState());
    }

    private String getWarriorDebugState() {
        LivingEntity target = this.getTarget();
        return "target=" + this.describeEntity(target)
                + ", validTarget=" + (target != null && this.canKeepCactemCombatTarget(target))
                + ", aggressive=" + this.isAggressive()
                + ", attacking=" + this.isAttacking()
                + ", usingItem=" + this.isUsingItem()
                + ", spearShownData=" + this.entityData.get(SPEAR_SHOWN)
                + ", expectedAnim=" + this.getExpectedWarriorAnimationState()
                + ", navigation=" + this.getNavigation().isInProgress()
                + ", delta=" + this.getDeltaMovement();
    }

    private String getExpectedWarriorAnimationState() {
        if (this.isAttacking() || !this.entityData.get(SPEAR_SHOWN)) {
            return "attack_horizontal";
        }

        if (this.isWalkAnimationMoving()) {
            return "peaceful_moving_horizontal";
        }

        return "peaceful_idle_short_then_vertical";
    }

    private String describeEntity(@Nullable Entity entity) {
        if (entity == null) {
            return "none";
        }

        return entity.getScoreboardName() + "#" + entity.getId() + "@" + entity.blockPosition();
    }

    private void spawnHealParticles() {
        for (float i = 0; i < Mth.TWO_PI; i += this.random.nextFloat() * 0.8F + 0.5F) {
            this.level().addParticle(CNBParticleTypes.CACTEM_HEAL_PARTICLE.get(), this.getX() + Mth.cos(i) * 1.25D, this.getY(), this.getZ() + Mth.sin(i) * 1.25D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void startElderTransformationEffect() {
        this.elderTransformationTicks = ELDER_TRANSFORMATION_TICKS;
        this.setHealing(true);
        this.startUsingItem(this.getUsedItemHand());
        CreaturesAndBeasts.LOGGER.info("Cactem at {} became an elder and started transformation healing", this.blockPosition());
    }

    private void tickElderTransformationEffect() {
        if (this.elderTransformationTicks <= 0) {
            return;
        }

        int elapsedTicks = ELDER_TRANSFORMATION_TICKS - this.elderTransformationTicks;
        if (elapsedTicks == ELDER_TRANSFORMATION_EFFECT_TICK) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ELDER_HEAL_EFFECT_DURATION, ELDER_HEAL_EFFECT_AMPLIFIER));
        }

        this.elderTransformationTicks--;
        if (this.elderTransformationTicks <= 0) {
            this.setHealing(false);
            this.stopUsingItem();
        }
    }

    private boolean isPlayingElderTransformationEffect() {
        return this.elderTransformationTicks > 0;
    }

    @Override
    public void setAge(int age) {
        super.setAge(age);
        double maxHealth = this.getAttribute(Attributes.MAX_HEALTH).getValue();
        if (isBaby() && maxHealth > BABY_HEALTH) {
            this.getAttribute(Attributes.MAX_HEALTH).addOrUpdateTransientModifier(new AttributeModifier(HEALTH_REDUCTION_ID, BABY_HEALTH - maxHealth, AttributeModifier.Operation.ADD_VALUE));
            this.setHealth(BABY_HEALTH);
        }
    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();

        double elderChance = this.random.nextDouble();

        if (!this.isBaby()) {
            if (elderChance < 0.25) {
                this.setElder(true);
                this.setItemInHand(this.getUsedItemHand(), new ItemStack(CNBItems.HEAL_SPELL_BOOK_1.get()));
            } else {
                this.setItemInHand(this.getUsedItemHand(), new ItemStack(CNBItems.CACTEM_SPEAR.get()));
            }
        }

        float percentHealth = this.getHealth() / BABY_HEALTH;
        this.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_REDUCTION_ID);
        this.setHealth(percentHealth * (float) this.getAttribute(Attributes.MAX_HEALTH).getValue());

        if (!this.level().isClientSide()) {
            this.shouldUpdateGoals = true;
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob entity) {
        return CNBEntityTypes.CACTEM.get().create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.isElder() && itemStack.is(Items.TOTEM_OF_UNDYING) && !this.isTrading() && !this.isHealing()) {
            if (this.level().isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            if (this.tradeGoal.tryStartDirectTrade(player)) {
                this.usePlayerItem(player, hand, itemStack);
                this.gameEvent(GameEvent.ENTITY_INTERACT, player);
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public SoundEvent getAmbientSound() {
        return CNBSoundEvents.CACTEM_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return CNBSoundEvents.CACTEM_HURT.get();
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.75F : 1.0F;
    }

    public void setShouldUpdateGoals(boolean shouldUpdateGoals) {
        this.shouldUpdateGoals = shouldUpdateGoals;
    }

    public boolean isElder() {
        return this.entityData.get(ELDER);
    }

    public void setElder(boolean isElder) {
        this.entityData.set(ELDER, isElder);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean isAttacking) {
        boolean wasAttacking = this.isAttacking();
        this.entityData.set(ATTACKING, isAttacking);
        if (!this.level().isClientSide() && wasAttacking != isAttacking) {
            CreaturesAndBeasts.LOGGER.info("Cactem warrior at {} attacking={} state={}", this.blockPosition(), isAttacking, this.getWarriorDebugState());
        }
    }

    public boolean isSpearShown() {
        // Keep the spear bone visible and let the throw animation move it.
        return true;
    }

    public void setSpearShown(boolean isShown) {
        boolean wasShown = this.entityData.get(SPEAR_SHOWN);
        this.entityData.set(SPEAR_SHOWN, isShown);
        if (!this.level().isClientSide() && wasShown != isShown) {
            CreaturesAndBeasts.LOGGER.info("Cactem warrior at {} spearShownData={} expectedAnim={}", this.blockPosition(), isShown, this.getExpectedWarriorAnimationState());
        }
    }

    public boolean isHealing() {
        return this.entityData.get(HEALING);
    }

    public void setHealing(boolean isHealing) {
        this.entityData.set(HEALING, isHealing);
    }

    public boolean isHealFacingLocked() {
        return this.entityData.get(HEAL_FACING_LOCKED);
    }

    public float getHealFacingYRot() {
        return this.entityData.get(HEAL_FACING_Y_ROT);
    }

    public float getHealFacingXRot() {
        return this.entityData.get(HEAL_FACING_X_ROT);
    }

    private void setHealFacing(float yRot, float xRot, boolean locked) {
        this.entityData.set(HEAL_FACING_Y_ROT, yRot);
        this.entityData.set(HEAL_FACING_X_ROT, xRot);
        this.entityData.set(HEAL_FACING_LOCKED, locked);
    }

    private void clearHealFacing() {
        this.setHealFacing(0.0F, 0.0F, false);
    }

    public boolean isTrading() {
        return this.entityData.get(TRADING);
    }

    public void setTrading(boolean isTrading) {
        this.entityData.set(TRADING, isTrading);
    }

    private static final RawAnimation ELDER_HEAL_ANIMATION = RawAnimation.begin().thenLoop("cactem_elder_heal");
    private static final RawAnimation ADMIRE_ANIMATION = RawAnimation.begin().thenLoop("cactem_admire");
    private static final RawAnimation ELDER_WALK_ANIMATION = RawAnimation.begin().thenLoop("cactem_elder_walk");
    private static final RawAnimation BABY_RUN_ANIMATION = RawAnimation.begin().thenLoop("cactem_baby_run");
    private static final RawAnimation RUN_THROW_ANIMATION = RawAnimation.begin().thenPlay("cactem_run_throw");
    private static final RawAnimation THROW_ANIMATION = RawAnimation.begin().thenPlay("cactem_throw");
    private static final RawAnimation RUN_ANIMATION = RawAnimation.begin().thenLoop("cactem_run");
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("cactem_idle");
    private static final RawAnimation IDLE_ANIMATION_2 = RawAnimation.begin().thenLoop("cactem_idle_2");

    private <E extends GeoAnimatable> PlayState animationPredicate(AnimationTest<E> event) {
        boolean isMoving = this.isWalkAnimationMoving();

        if (this.isHealing()) {
            this.resetPeacefulWarriorStationaryAnimationTimer();
            event.controller().setAnimation(ELDER_HEAL_ANIMATION);
        } else if (this.isTrading()) {
            this.resetPeacefulWarriorStationaryAnimationTimer();
            event.controller().setAnimation(ADMIRE_ANIMATION);
        } else if (isMoving) {
            this.resetPeacefulWarriorStationaryAnimationTimer();
            if (this.isElder()) {
                event.controller().setAnimation(ELDER_WALK_ANIMATION);
            } else if (this.isBaby()) {
                event.controller().setAnimation(BABY_RUN_ANIMATION);
            } else if (this.isAttacking() || !this.isSpearShown()) {
                event.controller().setAnimation(RUN_THROW_ANIMATION);
            } else {
                event.controller().setAnimation(RUN_ANIMATION);
            }
        } else {
            if (this.isElder()) {
                event.controller().setAnimation(IDLE_ANIMATION_2);
            } else if (this.isBaby()) {
                event.controller().setAnimation(IDLE_ANIMATION);
            } else if (this.isAttacking() || !this.isSpearShown()) {
                this.resetPeacefulWarriorStationaryAnimationTimer();
                event.controller().setAnimation(IDLE_ANIMATION_2);
            } else {
                event.controller().setAnimation(this.getPeacefulWarriorStationaryAnimation());
            }
        }
        return PlayState.CONTINUE;
    }

    private boolean isWalkAnimationMoving() {
        return this.walkAnimation.speed() >= WALK_ANIMATION_MOVING_SPEED;
    }

    private RawAnimation getPeacefulWarriorStationaryAnimation() {
        if (this.peacefulWarriorStationaryAnimationStartTick == Integer.MIN_VALUE) {
            this.peacefulWarriorStationaryAnimationStartTick = this.tickCount;
        }

        return this.tickCount - this.peacefulWarriorStationaryAnimationStartTick > IDLE_2_ANIMATION_DELAY_TICKS
                ? IDLE_ANIMATION_2
                : IDLE_ANIMATION;
    }

    private void resetPeacefulWarriorStationaryAnimationTimer() {
        this.peacefulWarriorStationaryAnimationStartTick = Integer.MIN_VALUE;
    }

    private <E extends GeoAnimatable> PlayState attackAnimationPredicate(AnimationTest<E> event) {
        RawAnimation currentAnim = event.controller().getCurrentRawAnimation();
        boolean isThrowAnimation = currentAnim != null && currentAnim.getAnimationStages().stream().anyMatch(stage -> stage.animationName().equals("cactem_throw"));

        if (this.isAttacking()) {
            this.throwAnimationFinishTicks = 0;
            event.controller().setAnimation(THROW_ANIMATION);
            return PlayState.CONTINUE;
        }

        if (isThrowAnimation && !event.controller().hasAnimationFinished() && this.throwAnimationFinishTicks++ < THROW_ANIMATION_FINISH_GRACE_TICKS) {
            return PlayState.CONTINUE;
        }

        this.throwAnimationFinishTicks = 0;
        event.controller().reset();
        this.setSpearShown(true);
        return PlayState.STOP;
    }

    private <E extends GeoAnimatable> void soundListener(KeyFrameEvent<E, SoundKeyframeData> event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (event.keyframeData().getSound().equals("cactem_heal")) {
            player.playSound(CNBSoundEvents.CACTEM_HEAL.get(), 1.0F, 1.0F);
        } else if (event.keyframeData().getSound().equals("spear_throw")) {
            player.playSound(CNBSoundEvents.SPEAR_THROW.get(), 1.0F, 1.0F);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar animationData) {
        AnimationController<CactemEntity> controller = new AnimationController<>("controller", 0, this::animationPredicate);
        AnimationController<CactemEntity> attackController = new AnimationController<>("attackController", 0, this::attackAnimationPredicate);

        controller.setSoundKeyframeHandler(this::soundListener);
        attackController.setSoundKeyframeHandler(this::soundListener);

        animationData.add(controller);
        animationData.add(attackController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    static class FollowElderGoal extends Goal {
        public static final int HORIZONTAL_SCAN_RANGE = 32;
        public static final int VERTICAL_SCAN_RANGE = 4;
        public static final int START_FOLLOW_IF_FARTHER_THAN = 20;
        public static final double ADULT_MIN_STOP_FOLLOW_DISTANCE = 2.0D;
        public static final double ADULT_MAX_STOP_FOLLOW_DISTANCE = 5.0D;
        public static final double BABY_MIN_STOP_FOLLOW_DISTANCE = 1.0D;
        public static final double BABY_MAX_STOP_FOLLOW_DISTANCE = 2.0D;
        public static final double BABY_CLOSE_FOLLOW_CHANCE = 0.75D;
        private final CactemEntity cactem;
        @Nullable
        private CactemEntity elder;
        private final double speedModifier;
        private double stopFollowDistance = ADULT_MIN_STOP_FOLLOW_DISTANCE;
        private int timeToRecalcPath;

        public FollowElderGoal(CactemEntity cactem, double speedModifier) {
            this.cactem = cactem;
            this.speedModifier = speedModifier;
        }

        public boolean canUse() {
            if (this.cactem.getTarget() != null) {
                return false;
            }

            List<? extends CactemEntity> list = this.cactem.level().getEntitiesOfClass(CactemEntity.class, this.cactem.getBoundingBox().inflate(HORIZONTAL_SCAN_RANGE, VERTICAL_SCAN_RANGE, HORIZONTAL_SCAN_RANGE));
            CactemEntity followTarget = null;
            double closestElderDistance = Double.MAX_VALUE;

            for(CactemEntity nearbyCactem : list) {
                if (nearbyCactem.isElder()) {
                    double distanceToCactem = this.cactem.distanceToSqr(nearbyCactem);
                    if (!(distanceToCactem > closestElderDistance)) {
                        closestElderDistance = distanceToCactem;
                        followTarget = nearbyCactem;
                    }
                }
            }

            if (followTarget == null) {
                return false;
            } else if (closestElderDistance < (START_FOLLOW_IF_FARTHER_THAN * START_FOLLOW_IF_FARTHER_THAN)) {
                return false;
            } else {
                this.elder = followTarget;
                this.stopFollowDistance = this.pickStopFollowDistance();
                return true;
            }
        }

        public boolean canContinueToUse() {
            if (this.cactem.getTarget() != null || this.elder == null || !this.elder.isAlive()) {
                return false;
            } else {
                double d0 = this.cactem.distanceToSqr(this.elder);
                return !(d0 < (this.stopFollowDistance * this.stopFollowDistance)) && !(d0 > (HORIZONTAL_SCAN_RANGE * HORIZONTAL_SCAN_RANGE));
            }
        }

        public void start() {
            this.timeToRecalcPath = 0;
        }

        public void stop() {
            this.elder = null;
            this.stopFollowDistance = ADULT_MIN_STOP_FOLLOW_DISTANCE;
        }

        public void tick() {
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                int targetDistance = Math.max(1, Mth.floor(this.stopFollowDistance));
                Path path = this.cactem.getNavigation().createPath(this.elder, targetDistance);
                this.cactem.getNavigation().moveTo(path, this.speedModifier);
            }
        }

        private double pickStopFollowDistance() {
            if (this.cactem.isBaby() && this.cactem.getRandom().nextDouble() < BABY_CLOSE_FOLLOW_CHANCE) {
                return this.randomBetween(BABY_MIN_STOP_FOLLOW_DISTANCE, BABY_MAX_STOP_FOLLOW_DISTANCE);
            }

            return this.randomBetween(ADULT_MIN_STOP_FOLLOW_DISTANCE, ADULT_MAX_STOP_FOLLOW_DISTANCE);
        }

        private double randomBetween(double min, double max) {
            return min + this.cactem.getRandom().nextDouble() * (max - min);
        }
    }

    static class TradeGoal extends Goal {
        @Nullable
        protected Path path;
        @Nullable
        protected ItemEntity itemInstance;
        @Nullable
        protected Entity tradeTarget;
        protected double tradeTime;
        protected double tradeDelay;

        protected final double speed;
        protected final CactemEntity entityIn;
        protected final double range;
        protected final PathNavigation navigation;

        public TradeGoal(CactemEntity entityIn, double range, double speedIn) {
            this.entityIn = entityIn;
            this.range = range;
            this.speed = speedIn;
            this.navigation = entityIn.getNavigation();

            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.entityIn.isTrading() && this.tradeTarget != null && this.itemInstance == null) {
                return true;
            }

            if (this.itemInstance == null && this.entityIn.getTarget() == null) {
                List<ItemEntity> list = this.entityIn.level().getEntitiesOfClass(ItemEntity.class, this.entityIn.getBoundingBox().inflate(this.range, 3.0D, this.range));

                for (ItemEntity item : list) {
                    if (item.getItem().is(Items.TOTEM_OF_UNDYING)) {
                        this.path = this.navigation.createPath(item.getOnPos(), 0);
                        this.itemInstance = item;
                        return path != null;
                    }
                }
            }

            return false;
        }

        @Override
        public void stop() {
            this.itemInstance = null;
            this.tradeTarget = null;
            this.path = null;
            this.navigation.stop();
            this.entityIn.setTrading(false);
        }

        @Override
        public void start() {
            if (this.path != null) {
                this.navigation.moveTo(this.path, this.speed);
            } else {
                this.navigation.stop();
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (this.entityIn.isTrading()) {
                return this.tradeTime > 0;
            }

            return this.itemInstance != null && (!this.navigation.isDone() || this.tradeTime > 0) && !this.itemInstance.isRemoved();
        }

        @Override
        public boolean isInterruptable() {
            return this.tradeTime <= 0;
        }

        public boolean tryStartDirectTrade(Player player) {
            if (!this.entityIn.isElder() || this.entityIn.isTrading() || this.entityIn.isHealing() || this.entityIn.getTarget() != null) {
                return false;
            }

            this.itemInstance = null;
            this.path = null;
            this.tradeTarget = player;
            this.tradeTime = 54;
            this.tradeDelay = 0;
            this.navigation.stop();
            this.entityIn.setTrading(true);
            this.entityIn.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
            CreaturesAndBeasts.LOGGER.info("Cactem trade started at {} after accepting {} directly from {}", this.entityIn.blockPosition(), Items.TOTEM_OF_UNDYING, player.getScoreboardName());
            return true;
        }

        public void trade() {
            this.entityIn.setTrading(false);
            ItemStack returnItem;
            double lootChance = this.entityIn.random.nextDouble();

            if (lootChance < 0.2) {
                returnItem = new ItemStack(Items.EMERALD, 15 + this.entityIn.random.nextInt(10));
            } else if (lootChance < 0.7) {
                returnItem = new ItemStack(CNBItems.HEAL_SPELL_BOOK_1.get());
            } else {
                returnItem = new ItemStack(Items.DEAD_BUSH);
            }

            if (entityIn.level() instanceof ServerLevel) {
                BehaviorUtils.throwItem(this.entityIn, returnItem, this.getRewardTargetPosition());
            }
            CreaturesAndBeasts.LOGGER.info("Cactem trade completed at {} with reward {} toward {}", this.entityIn.blockPosition(), returnItem, this.tradeTarget == null ? "no target" : this.tradeTarget.getScoreboardName());
            this.entityIn.playSound(SoundEvents.ITEM_PICKUP, 0.8F, 1.0F);
        }

        private Vec3 getRewardTargetPosition() {
            Entity target = this.tradeTarget;

            if (target != null && target.isAlive() && !target.isRemoved()) {
                return target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            }

            return this.entityIn.position().add(this.entityIn.getLookAngle());
        }

        private Vec3 getTradeLookPosition() {
            Entity target = this.tradeTarget;

            if (target != null && !target.isRemoved()) {
                return target.getEyePosition();
            }

            if (this.itemInstance != null) {
                return this.itemInstance.position();
            }

            return this.entityIn.position().add(this.entityIn.getLookAngle());
        }

        private void tickActiveTrade() {
            if (--this.tradeTime <= 0) {
                trade();
                tradeDelay = 20;
            } else if (tradeTime % 3 == 0) {
                entityIn.lookAt(EntityAnchorArgument.Anchor.EYES, this.getTradeLookPosition());
                entityIn.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, Items.TOTEM_OF_UNDYING), entityIn.getRandomX(0.5F) + entityIn.getLookAngle().x / 2.0D, entityIn.getRandomY(), entityIn.getRandomZ(0.5F) + entityIn.getLookAngle().z / 2.0D, 4D, 0D, 0D);
            }
        }

        @Nullable
        private Entity findTradeTarget(ItemEntity itemEntity) {
            Entity owner = itemEntity.getOwner();

            if (owner instanceof Player player && player.isAlive() && !player.isSpectator()) {
                return player;
            }

            Player nearestPlayer = null;
            double nearestDistance = Double.MAX_VALUE;

            for (Player player : this.entityIn.level().getEntitiesOfClass(Player.class, this.entityIn.getBoundingBox().inflate(this.range, 4.0D, this.range))) {
                if (player.isSpectator()) {
                    continue;
                }

                double distance = player.distanceToSqr(this.entityIn);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestPlayer = player;
                }
            }

            return nearestPlayer;
        }

        @Override
        public void tick() {
            if (tradeDelay <= 0) {
                if (this.entityIn.isTrading()) {
                    this.navigation.setSpeedModifier(0.0D);
                    this.tickActiveTrade();
                } else if (this.itemInstance != null && this.entityIn.distanceToSqr(itemInstance) < 2.0D) {
                    this.navigation.setSpeedModifier(0.0D);

                    if (!this.entityIn.isTrading() && !itemInstance.isRemoved()) {
                        ItemStack offeredStack = itemInstance.getItem();
                        int offeredCount = offeredStack.getCount();
                        this.tradeTarget = this.findTradeTarget(itemInstance);
                        this.entityIn.setTrading(true);
                        offeredStack.shrink(1);

                        if (offeredStack.isEmpty()) {
                            itemInstance.discard();
                        }

                        CreaturesAndBeasts.LOGGER.info("Cactem trade started at {} after accepting {} from stack count {}", this.entityIn.blockPosition(), Items.TOTEM_OF_UNDYING, offeredCount);
                        entityIn.lookAt(EntityAnchorArgument.Anchor.EYES, itemInstance.position());
                        this.tradeTime = 54;
                    }

                } else {
                    this.navigation.setSpeedModifier(speed);
                }

            } else {
                tradeDelay--;
            }
        }
    }

    static class HealGoal extends Goal {
        private static final int MAX_HEAL_ALIGN_TICKS = 20;
        private static final int HEAL_ALIGNED_HOLD_TICKS = 3;
        private static final float HEAL_ALIGN_ROTATION_STEP = 30.0F;
        private static final float HEAL_ALIGN_PITCH_STEP = 30.0F;
        private static final float HEAL_START_YAW_TOLERANCE = 3.0F;

        private final CactemEntity cactem;
        private final double speedModifier;
        private final int healIntervalMin;
        private final int healIntervalDiff;
        private final float healRadius;
        private final float avoidDist;
        @Nullable
        private CactemEntity healTarget;
        private boolean preparingHeal = false;
        private int healAlignTicks = 0;
        private int healAlignedTicks = 0;
        private boolean healFacingLocked = false;
        private boolean loggedMovingHealTarget = false;
        private float lockedHealYRot;
        private float lockedHealXRot;

        public HealGoal(CactemEntity cactem, double speedModifier, int healIntervalMin, int healIntervalMax, float healRadius, float avoidDist) {
            this.cactem = cactem;
            this.speedModifier = speedModifier;
            this.healIntervalMin = healIntervalMin;
            this.healIntervalDiff = healIntervalMax - healIntervalMin;
            this.healRadius = healRadius;
            this.avoidDist = avoidDist;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            if (this.cactem.isPlayingElderTransformationEffect()) {
                return false;
            }

            LivingEntity livingentity = this.cactem.getTarget();

            if (livingentity != null && !livingentity.isAlive()) {
                this.cactem.setTarget(null);
                livingentity = null;
            }

            boolean cactemNeedsHealing = this.updateHealTarget();
            if (cactemNeedsHealing && this.cactem.healCooldown <= 0) {
                return true;
            } else if (livingentity == null) {
                return false;
            } else {
                return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player) livingentity).isCreative();
            }
        }

        public boolean canContinueToUse() {
            return this.cactem.isUsingItem() || this.preparingHeal || this.canUse();
        }

        public void start() {
            super.start();
            if (this.cactem.getTarget() != null) {
                this.cactem.setAggressive(true);
            }
        }

        public void stop() {
            super.stop();
            this.cactem.setAggressive(false);
            this.cactem.setHealing(false);
            this.healTarget = null;
            this.preparingHeal = false;
            this.healAlignTicks = 0;
            this.healAlignedTicks = 0;
            this.healFacingLocked = false;
            this.loggedMovingHealTarget = false;
            this.cactem.clearHealFacing();
            this.cactem.stopUsingItem();
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity targetEntity = this.cactem.getTarget();

            if (this.cactem.isUsingItem()) {
                this.cactem.getNavigation().stop();
                this.trackLockedHealFacing();
                int i = this.cactem.getTicksUsingItem();
                if (i == 20) {
                    this.cactem.performHeal(this.healRadius);
                } else if (i >= 38) {
                    this.cactem.setHealing(false);
                    this.cactem.stopUsingItem();
                    this.healFacingLocked = false;
                    this.healAlignedTicks = 0;
                    this.loggedMovingHealTarget = false;
                    this.cactem.clearHealFacing();
                    this.cactem.healCooldown = this.healIntervalMin + this.cactem.random.nextInt(this.healIntervalDiff + 1);
                }
            } else if (this.cactem.healCooldown <= 0 && (this.preparingHeal || this.updateHealTarget())) {
                this.cactem.getNavigation().stop();
                this.tickHealAlignment();
            } else if (targetEntity != null && !this.cactem.getNavigation().isInProgress() && this.cactem.distanceToSqr(this.cactem.getTarget()) <= (this.avoidDist * this.avoidDist)) {
                Vec3 vec3 = DefaultRandomPos.getPosAway(this.cactem, (int) this.avoidDist, 7, targetEntity.position());
                if (vec3 != null) {
                    Path path = this.cactem.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
                    this.cactem.getNavigation().moveTo(path, this.speedModifier);
                }
            } else if (targetEntity != null && !this.cactem.getNavigation().isInProgress()) {
                Vec3 vec3 = DefaultRandomPos.getPosTowards(this.cactem, (int) this.avoidDist, 7, targetEntity.position(), ((float)Math.PI / 2.0F));
                if (vec3 != null) {
                    Path path = this.cactem.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
                    this.cactem.getNavigation().moveTo(path, this.speedModifier);
                }
            }
        }

        private boolean updateHealTarget() {
            this.healTarget = this.findCactemToHeal(this.cactem);
            return this.healTarget != null;
        }

        private void tickHealAlignment() {
            if (!this.ensureHealTarget()) {
                this.cancelHealPreparation();
                return;
            }

            if (!this.preparingHeal) {
                this.startHealPreparation();
            }

            if (this.healFacingLocked) {
                this.trackLockedHealFacing();
                this.healAlignedTicks++;

                if (this.healAlignedTicks >= HEAL_ALIGNED_HOLD_TICKS) {
                    this.startHealSpell();
                }
                return;
            }

            this.turnTowardHealTarget();
            this.healAlignTicks++;

            if (this.isFacingHealTarget()) {
                this.lockHealFacing("aligned");
            } else if (this.healAlignTicks >= MAX_HEAL_ALIGN_TICKS) {
                this.faceHealTarget();
                this.lockHealFacing("forced");
            }
        }

        private void startHealPreparation() {
            if (this.healTarget == null) {
                return;
            }

            this.preparingHeal = true;
            this.healAlignTicks = 0;
            this.healAlignedTicks = 0;
            this.healFacingLocked = false;
            this.loggedMovingHealTarget = false;
            this.cactem.clearHealFacing();
            CreaturesAndBeasts.LOGGER.info("Elder Cactem at {} started heal alignment toward {} with yawDiff={}", this.cactem.blockPosition(), this.healTarget.blockPosition(), this.getHealTargetYawDiff());
        }

        private boolean ensureHealTarget() {
            if (this.isValidHealTarget(this.healTarget)) {
                return true;
            }

            return this.updateHealTarget();
        }

        private boolean isValidHealTarget(@Nullable CactemEntity target) {
            return target != null
                    && target.isAlive()
                    && target.canTriggerElderHeal()
                    && this.cactem.isInHealRange(target, this.healRadius);
        }

        private void turnTowardHealTarget() {
            if (this.healTarget == null) {
                return;
            }

            float yRot = Mth.approachDegrees(this.cactem.getYRot(), this.getHealTargetYRot(), HEAL_ALIGN_ROTATION_STEP);
            float xRot = Mth.approachDegrees(this.cactem.getXRot(), this.getHealTargetXRot(), HEAL_ALIGN_PITCH_STEP);

            this.cactem.setYRot(yRot);
            this.cactem.setYBodyRot(yRot);
            this.cactem.setYHeadRot(yRot);
            this.cactem.setXRot(xRot);
        }

        private void faceHealTarget() {
            if (this.healTarget == null) {
                return;
            }

            float yRot = this.getHealTargetYRot();
            float xRot = this.getHealTargetXRot();
            this.cactem.setYRot(yRot);
            this.cactem.setYBodyRot(yRot);
            this.cactem.setYHeadRot(yRot);
            this.cactem.setXRot(xRot);
        }

        private boolean isFacingHealTarget() {
            if (this.healTarget == null) {
                return false;
            }

            return this.getHealTargetYawDiff() <= HEAL_START_YAW_TOLERANCE;
        }

        private void lockHealFacing(String reason) {
            if (this.healTarget == null) {
                this.cancelHealPreparation();
                return;
            }

            this.lockedHealYRot = this.cactem.getYRot();
            this.lockedHealXRot = this.cactem.getXRot();
            this.healFacingLocked = true;
            this.healAlignedTicks = 0;
            this.cactem.setHealFacing(this.lockedHealYRot, this.lockedHealXRot, true);
            this.holdHealFacing();
            CreaturesAndBeasts.LOGGER.info("Elder Cactem at {} {} heal alignment toward {} after {} ticks with yawDiff={}; synced render yaw={} and holding {} ticks before spell", this.cactem.blockPosition(), reason, this.healTarget.blockPosition(), this.healAlignTicks, this.getHealTargetYawDiff(), this.lockedHealYRot, HEAL_ALIGNED_HOLD_TICKS);
        }

        private void startHealSpell() {
            if (this.healTarget == null) {
                this.cancelHealPreparation();
                return;
            }

            this.trackLockedHealFacing();
            this.preparingHeal = false;
            this.healAlignTicks = 0;
            this.healAlignedTicks = 0;
            this.holdHealFacing();
            this.cactem.setHealFacing(this.lockedHealYRot, this.lockedHealXRot, true);
            this.cactem.setHealing(true);
            this.cactem.startUsingItem(this.cactem.getUsedItemHand());
            CreaturesAndBeasts.LOGGER.info("Elder Cactem at {} started heal spell toward {} after visible alignment hold with final yawDiff={}", this.cactem.blockPosition(), this.healTarget.blockPosition(), this.getHealTargetYawDiff());
        }

        private void trackLockedHealFacing() {
            if (!this.healFacingLocked || this.healTarget == null) {
                return;
            }

            float yawDiff = this.getHealTargetYawDiff();
            if (yawDiff > HEAL_START_YAW_TOLERANCE && !this.loggedMovingHealTarget) {
                this.loggedMovingHealTarget = true;
                CreaturesAndBeasts.LOGGER.info("Elder Cactem at {} updated heal facing toward moving target {} with yawDiff={}", this.cactem.blockPosition(), this.healTarget.blockPosition(), yawDiff);
            }

            this.lockedHealYRot = Mth.approachDegrees(this.lockedHealYRot, this.getHealTargetYRot(), HEAL_ALIGN_ROTATION_STEP);
            this.lockedHealXRot = Mth.approachDegrees(this.lockedHealXRot, this.getHealTargetXRot(), HEAL_ALIGN_PITCH_STEP);
            this.holdHealFacing();
            this.cactem.setHealFacing(this.lockedHealYRot, this.lockedHealXRot, true);
        }

        private void holdHealFacing() {
            if (!this.healFacingLocked) {
                return;
            }

            this.cactem.setYRot(this.lockedHealYRot);
            this.cactem.setYBodyRot(this.lockedHealYRot);
            this.cactem.setYHeadRot(this.lockedHealYRot);
            this.cactem.setXRot(this.lockedHealXRot);
        }

        private void cancelHealPreparation() {
            this.preparingHeal = false;
            this.healAlignTicks = 0;
            this.healAlignedTicks = 0;
            this.healFacingLocked = false;
            this.loggedMovingHealTarget = false;
            this.cactem.clearHealFacing();
            this.healTarget = null;
        }

        private float getHealTargetYawDiff() {
            return Math.abs(Mth.wrapDegrees(this.getHealTargetYRot() - this.cactem.getYRot()));
        }

        private float getHealTargetYRot() {
            if (this.healTarget == null) {
                return this.cactem.getYRot();
            }

            Vec3 targetPos = this.healTarget.getEyePosition();
            Vec3 sourcePos = this.cactem.getEyePosition();
            double xDiff = targetPos.x - sourcePos.x;
            double zDiff = targetPos.z - sourcePos.z;
            return (float)(Math.atan2(zDiff, xDiff) * Mth.RAD_TO_DEG) - 90.0F;
        }

        private float getHealTargetXRot() {
            if (this.healTarget == null) {
                return this.cactem.getXRot();
            }

            Vec3 targetPos = this.healTarget.getEyePosition();
            Vec3 sourcePos = this.cactem.getEyePosition();
            double xDiff = targetPos.x - sourcePos.x;
            double yDiff = targetPos.y - sourcePos.y;
            double zDiff = targetPos.z - sourcePos.z;
            double horizontalDistance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            return (float)(-(Math.atan2(yDiff, horizontalDistance) * Mth.RAD_TO_DEG));
        }

        @Nullable
        private CactemEntity findCactemToHeal(CactemEntity elder) {
            List<CactemEntity> list = elder.getCactemsInHealRange(this.healRadius);
            CactemEntity closestInjuredCactem = null;
            double closestDistance = Double.MAX_VALUE;

            for(CactemEntity nearbyCactem : list) {
                if (nearbyCactem.canTriggerElderHeal()) {
                    double distance = elder.distanceToSqr(nearbyCactem);

                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestInjuredCactem = nearbyCactem;
                    }
                }
            }

            return closestInjuredCactem;
        }
    }

    static class RangedSpearAttackGoal extends Goal {
        private static final int PHASE_IDLE = 0;
        private static final int PHASE_CHASING = 1;
        private static final int PHASE_STRAFING = 2;
        private static final int PHASE_WINDUP = 3;
        private static final int PHASE_THROW = 4;
        private static final int PHASE_LOST_SIGHT = 5;

        private final CactemEntity cactem;
        private final double speedModifier;
        private final int attackIntervalMin;
        private final float attackRadiusSqr;
        private int attackTime = -1;
        private int seeTime;
        private boolean strafingClockwise;
        private boolean strafingBackwards;
        private int strafingTime = -1;
        private int phase = PHASE_IDLE;

        public RangedSpearAttackGoal(CactemEntity cactem, double speedModifier, int attackIntervalMin, float attackRadius) {
            this.cactem = cactem;
            this.speedModifier = speedModifier;
            this.attackIntervalMin = attackIntervalMin;
            this.attackRadiusSqr = attackRadius * attackRadius;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            LivingEntity livingentity = this.cactem.getTarget();
            return this.cactem.canFightAsWarrior() && this.cactem.canKeepCactemCombatTarget(livingentity);
        }

        public boolean canContinueToUse() {
                return this.canUse();
        }

        public void start() {
            super.start();
            this.cactem.setAggressive(true);
            LivingEntity target = this.cactem.getTarget();
            if (target != null) {
                this.setPhase(PHASE_CHASING, "start", target, this.cactem.distanceToSqr(target), this.cactem.getSensing().hasLineOfSight(target));
                this.cactem.alertNearbyCactemWarriors(target);
            }
        }

        public void stop() {
            super.stop();
            this.cactem.setAggressive(false);
            this.resetState();
            this.cactem.setAttacking(false);
            this.cactem.setSpearShown(true);
            this.cactem.stopUsingItem();
            this.cactem.logWarriorDebugState("spear-goal-stop");
        }

        private void resetState() {
            this.seeTime = 0;
            this.attackTime = -1;
            this.strafingTime = -1;
            this.strafingClockwise = false;
            this.strafingBackwards = false;
            this.phase = PHASE_IDLE;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity targetEntity = this.cactem.getTarget();
            if (targetEntity != null) {
                if (!this.cactem.canKeepCactemCombatTarget(targetEntity)) {
                    this.cactem.forgetCombatTarget(targetEntity, "goal-forgot");
                    return;
                }

                double d0 = this.cactem.distanceToSqr(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
                boolean flag = this.cactem.getSensing().hasLineOfSight(targetEntity);
                boolean flag1 = this.seeTime > 0;
                if (flag != flag1) {
                    this.seeTime = 0;
                }

                if (flag) {
                    ++this.seeTime;
                } else {
                    --this.seeTime;
                }

                if (d0 > (double)this.attackRadiusSqr || this.seeTime < 20) {
                    this.cactem.getNavigation().moveTo(targetEntity, this.speedModifier);
                    this.strafingTime = -1;
                    this.setPhase(PHASE_CHASING, "chasing", targetEntity, d0, flag);
                } else {
                    this.cactem.getNavigation().stop();
                    ++this.strafingTime;
                    this.setPhase(PHASE_STRAFING, "strafing", targetEntity, d0, flag);
                }

                if (this.strafingTime >= 20) {
                    if ((double)this.cactem.getRandom().nextFloat() < 0.3D) {
                        this.strafingClockwise = !this.strafingClockwise;
                    }

                    if ((double)this.cactem.getRandom().nextFloat() < 0.3D) {
                        this.strafingBackwards = !this.strafingBackwards;
                    }

                    this.strafingTime = 0;
                }

                if (this.strafingTime > -1) {
                    if (d0 > (double)(this.attackRadiusSqr * 0.75F)) {
                        this.strafingBackwards = false;
                    } else if (d0 < (double)(this.attackRadiusSqr * 0.5F)) {
                        this.strafingBackwards = true;
                    }
                    if (!this.cactem.isUsingItem()) {
                        this.cactem.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                    }
                    this.cactem.lookAt(targetEntity, 30.0F, 30.0F);
                } else {
                    this.cactem.getLookControl().setLookAt(targetEntity, 30.0F, 30.0F);
                }

                if (this.cactem.isUsingItem()) {
                    if (!flag && this.seeTime < -60) {
                        this.setPhase(PHASE_LOST_SIGHT, "lost-sight-cancel", targetEntity, d0, false);
                        this.cactem.setAttacking(false);
                        this.cactem.setSpearShown(true);
                        this.cactem.stopUsingItem();
                    } else if (flag) {
                        int i = this.cactem.getTicksUsingItem();
                        if (i >= 6) {
                            this.setPhase(PHASE_THROW, "throw", targetEntity, d0, true);
                            this.cactem.setSpearShown(false);
                            this.cactem.setAttacking(false);
                            this.cactem.stopUsingItem();
                            this.cactem.performRangedAttack(targetEntity, 1.0F);
                            this.attackTime = this.attackIntervalMin;
                        }
                    }
                } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                    this.setPhase(PHASE_WINDUP, "windup", targetEntity, d0, flag);
                    this.cactem.setAttacking(true);
                    this.cactem.startUsingItem(this.cactem.getUsedItemHand());
                }
            }
        }

        private void setPhase(int phase, String reason, LivingEntity target, double distanceSqr, boolean hasLineOfSight) {
            if (this.phase == phase) {
                return;
            }

            this.phase = phase;
            CreaturesAndBeasts.LOGGER.info(
                    "Cactem warrior at {} attackPhase={} target={} distanceSqr={} attackRadiusSqr={} seeTime={} lineOfSight={} strafingTime={} state={}",
                    this.cactem.blockPosition(),
                    reason,
                    this.cactem.describeEntity(target),
                    distanceSqr,
                    this.attackRadiusSqr,
                    this.seeTime,
                    hasLineOfSight,
                    this.strafingTime,
                    this.cactem.getWarriorDebugState()
            );
        }
    }

    static class BecomeElderGoal extends Goal {
        private final CactemEntity cactem;
        private final float elderRadius;

        public BecomeElderGoal(CactemEntity cactem, float elderRadius) {
            this.cactem = cactem;
            this.elderRadius = elderRadius;
        }

        @Override
        public void start() {
            this.cactem.setElder(true);
            this.cactem.setItemInHand(this.cactem.getUsedItemHand(), new ItemStack(CNBItems.HEAL_SPELL_BOOK_1.get()));
            this.cactem.startElderTransformationEffect();
            this.cactem.setShouldUpdateGoals(true);
        }

        @Override
        public boolean canUse() {
            return !isElderNear();
        }

        private boolean isElderNear() {
            List<? extends CactemEntity> list = this.cactem.level().getEntitiesOfClass(CactemEntity.class, this.cactem.getBoundingBox().inflate(this.elderRadius, 16.0F, this.elderRadius));
            for (CactemEntity nearbyCactem : list) {
                if (nearbyCactem.isElder()) {
                    return true;
                }
            }
            return false;
        }
    }
}
