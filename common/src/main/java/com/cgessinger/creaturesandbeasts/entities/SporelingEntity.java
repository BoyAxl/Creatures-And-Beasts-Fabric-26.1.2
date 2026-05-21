package com.cgessinger.creaturesandbeasts.entities;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.ai.ConvertItemGoal;
import com.cgessinger.creaturesandbeasts.entities.extensions.MultiCategoryEntity;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import com.cgessinger.creaturesandbeasts.init.CNBSoundEvents;
import com.cgessinger.creaturesandbeasts.init.CNBSporelingTypes;
import com.cgessinger.creaturesandbeasts.util.SporelingType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
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
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

import static com.cgessinger.creaturesandbeasts.init.CNBTags.Items.SPORELING_FOOD;
import static com.cgessinger.creaturesandbeasts.util.SporelingType.SporelingHostility.*;

public class SporelingEntity extends TamableAnimal implements GeoEntity, MultiCategoryEntity {
    private static final EntityDataAccessor<String> TYPE = SynchedEntityData.defineId(SporelingEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(SporelingEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WAVING = SynchedEntityData.defineId(SporelingEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> INSPECTING = SynchedEntityData.defineId(SporelingEntity.class, EntityDataSerializers.BOOLEAN);

    private final SporelingAttackGoal attackGoal = new SporelingAttackGoal(this, 1.3D, false);
    private final NearestAttackableTargetGoal<Player> nearestAttackableTargetGoal = new NearestAttackableTargetGoal<>(this, Player.class, true);
    private final HurtByTargetGoal hurtByTargetGoal = new HurtByTargetGoal(this);
    private final WaveGoal waveGoal = new WaveGoal(this, Player.class, 8.0F);
    private final TemptGoal temptGoal = new SporelingTemptGoal(this, 1.0D, Ingredient.of(this.registryAccess().lookupOrThrow(Registries.ITEM).getOrThrow(SPORELING_FOOD)), false);
    private final PanicGoal panicGoal = new PanicGoal(this, 1.25D);
    private final ConvertItemGoal convertItemGoal = new ConvertItemGoal(this, 16.0D, 1.3D);

    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);
    private int attackTimer;
    private int waveTimer;

    public SporelingEntity(EntityType<SporelingEntity> entityType, Level level) {
        super(entityType, level);
        this.attackTimer = 0;
        this.waveTimer = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, CNBSporelingTypes.RED_OVERWORLD.getId().toString());
        builder.define(ATTACKING, false);
        builder.define(WAVING, false);
        builder.define(INSPECTING, false);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("SporelingType", this.getSporelingType().getId().toString());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        SporelingType type = SporelingType.getById(input.getStringOr("SporelingType", CNBSporelingTypes.RED_OVERWORLD.getId().toString()));
        if (type == null) {
            type = CNBSporelingTypes.RED_OVERWORLD;
        }
        this.setSporelingType(type);
        this.reassessGoals();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    private void reassessGoals() {
        this.goalSelector.removeGoal(panicGoal);
        this.goalSelector.removeGoal(convertItemGoal);
        this.goalSelector.removeGoal(temptGoal);
        this.goalSelector.removeGoal(waveGoal);
        this.goalSelector.removeGoal(attackGoal);
        this.targetSelector.removeGoal(nearestAttackableTargetGoal);
        this.targetSelector.removeGoal(hurtByTargetGoal);

        if (this.getSporelingType().getHostility() == FRIENDLY) {
            this.goalSelector.addGoal(2, panicGoal);
            this.goalSelector.addGoal(2, convertItemGoal);
            this.goalSelector.addGoal(3, temptGoal);
            this.goalSelector.addGoal(6, waveGoal);
        } else if (this.getSporelingType().getHostility() == HOSTILE) {
            this.goalSelector.addGoal(2, attackGoal);
            this.targetSelector.addGoal(2, nearestAttackableTargetGoal);
        } else {
            this.goalSelector.addGoal(2, attackGoal);
            this.targetSelector.addGoal(1, hurtByTargetGoal);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isAttacking()) {
            this.attackTimer--;
        }

        if (this.isWaving()) {
            this.waveTimer--;
            this.navigation.stop();
        }

        if (this.attackTimer == 0) {
            this.setAttacking(false);
        }

        if (this.waveTimer == 0) {
            this.setWaving(false);
        }
    }

    @Override
    public boolean isNoAi() {
        return super.isNoAi() || this.getVehicle() != null;
    }

    @Nullable
    public static SporelingEntity getBackpackPassenger(Player player) {
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof SporelingEntity sporeling) {
                return sporeling;
            }
        }

        return null;
    }

    public boolean canRideInBackpack(Player player) {
        return this.isTame()
                && this.isOwnedBy(player)
                && player.isSecondaryUseActive()
                && player.getPassengers().isEmpty()
                && player.getItemBySlot(EquipmentSlot.CHEST).is(CNBItems.SPORELING_BACKPACK.get());
    }

    private boolean tryRideInBackpack(Player player) {
        return this.canRideInBackpack(player) && this.startRiding(player);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.level().isClientSide()) {
            if (this.isTame()) {
                InteractionResult interactionresult = super.mobInteract(player, hand);
                if (!interactionresult.consumesAction() && this.isOwnedBy(player)) {
                    if (this.tryRideInBackpack(player)) {
                        return InteractionResult.SUCCESS;
                    }
                }
            }

            boolean flag = this.isOwnedBy(player) || this.isTame() || itemstack.is(Items.BONE) && !this.isTame();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            if (this.isTame()) {
                if ((itemstack.is(Items.RED_MUSHROOM) || itemstack.is(Items.BROWN_MUSHROOM)) && this.getHealth() < this.getMaxHealth()) {
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }

                    this.heal(2);
                    this.gameEvent(GameEvent.EAT, this);
                    return InteractionResult.SUCCESS;
                }

                InteractionResult interactionresult = super.mobInteract(player, hand);
                if (!interactionresult.consumesAction() && this.isOwnedBy(player)) {
                    if (this.tryRideInBackpack(player)) {
                        return InteractionResult.SUCCESS;
                    } else {
                        this.setOrderedToSit(!this.isOrderedToSit());
                        this.jumping = false;
                        this.navigation.stop();
                        this.setTarget(null);
                    }
                    return InteractionResult.SUCCESS;
                }

                return interactionresult;
            } else if (this.getSporelingType().getHostility() == FRIENDLY && itemstack.is(Items.BONE_MEAL)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                if (this.random.nextInt(3) == 0 && !CreaturesAndBeasts.getInstance().getProxy().callAnimalTameEvent(this, player)) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte)7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte)6);
                }

                return InteractionResult.SUCCESS;
            }

            return super.mobInteract(player, hand);
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        if (this.getSporelingType().getHostility().equals(FRIENDLY)) {
            return new ItemStack(CNBItems.SPORELING_OVERWORLD_EGG.get());
        } else {
            return new ItemStack(CNBItems.SPORELING_NETHER_EGG.get());
        }
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource damageSource, float damage) {
        if (damageSource.is(DamageTypeTags.IS_FIRE) && this.getSporelingType().getHostility() != FRIENDLY) {
            return;
        }
        super.actuallyHurt(level, damageSource, damage);
    }

    @Override
    public boolean fireImmune() {
        SporelingType.SporelingHostility hostility = this.getSporelingType().getHostility();
        return hostility.equals(HOSTILE) || hostility.equals(NEUTRAL) || super.fireImmune();
    }

    @Override
    public MobCategory getClassification(boolean forSpawnCount) {
        return this.getSporelingType().getHostility() == FRIENDLY ? MobCategory.CREATURE : MobCategory.MONSTER;
    }

    public static boolean checkSporelingSpawnRules(EntityType<SporelingEntity> entity, LevelAccessor worldIn, EntitySpawnReason mobSpawnType, BlockPos pos, RandomSource rand) {
        if (worldIn.getBiome(pos).is(BiomeTags.IS_NETHER)) {
            return worldIn.getDifficulty() != Difficulty.PEACEFUL;
        } else {
            return worldIn.getRawBrightness(pos, 0) > 8;
        }
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        if (this.getSporelingType().getHostility() == FRIENDLY) {
            return level.getBlockState(pos.below()).is(Blocks.MYCELIUM) ? 10.0F : level.getPathfindingCostFromLightLevels(pos);
        } else {
            return 10.0F;
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, EntitySpawnReason reason, @Nullable SpawnGroupData spawnDataIn) {
        Holder<Biome> biome = worldIn.getBiome(this.blockPosition());

        if (biome.is(Biomes.CRIMSON_FOREST)) {
            this.setSporelingType(CNBSporelingTypes.CRIMSON_FUNGUS);
        } else if (biome.is(Biomes.WARPED_FOREST)) {
            this.setSporelingType(CNBSporelingTypes.WARPED_FUNGUS);
        } else if (biome.is(BiomeTags.IS_NETHER)) {
            if (random.nextBoolean()) {
                this.setSporelingType(CNBSporelingTypes.RED_NETHER);
            } else {
                this.setSporelingType(CNBSporelingTypes.BROWN_NETHER);
            }
        } else {
            if (random.nextBoolean()) {
                this.setSporelingType(CNBSporelingTypes.RED_OVERWORLD);
            } else {
                this.setSporelingType(CNBSporelingTypes.BROWN_OVERWORLD);
            }
        }

        this.reassessGoals();

        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
    }

    public void setSporelingTypeFromSpawnEgg(ServerLevelAccessor worldIn, boolean netherEgg) {
        Holder<Biome> biome = worldIn.getBiome(this.blockPosition());

        if (netherEgg) {
            if (biome.is(Biomes.CRIMSON_FOREST)) {
                this.setSporelingType(CNBSporelingTypes.CRIMSON_FUNGUS);
            } else if (biome.is(Biomes.WARPED_FOREST)) {
                this.setSporelingType(CNBSporelingTypes.WARPED_FUNGUS);
            } else if (random.nextBoolean()) {
                this.setSporelingType(CNBSporelingTypes.RED_NETHER);
            } else {
                this.setSporelingType(CNBSporelingTypes.BROWN_NETHER);
            }
        } else if (random.nextBoolean()) {
            this.setSporelingType(CNBSporelingTypes.RED_OVERWORLD);
        } else {
            this.setSporelingType(CNBSporelingTypes.BROWN_OVERWORLD);
        }

        this.reassessGoals();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return null;
    }

    public boolean isRunning() {
        return this.getMoveControl().getSpeedModifier() >= 1.3D;
    }

    public void setAttacking(boolean isAttacking) {
        this.entityData.set(ATTACKING, isAttacking);
        this.attackTimer = isAttacking ? 7 : 0;
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public boolean isWaving() {
        return this.entityData.get(WAVING);
    }

    public void setWaving(boolean isWaving) {
        this.entityData.set(WAVING, isWaving);
        this.waveTimer = isWaving ? 41 : 0;
    }

    public boolean isInspecting() {
        return this.entityData.get(INSPECTING);
    }

    public void setInspecting(boolean isInspecting) {
        this.entityData.set(INSPECTING, isInspecting);
    }

    public ItemStack getHolding() {
        return this.getItemBySlot(EquipmentSlot.MAINHAND);
    }

    public void setHolding(ItemStack stack) {
        this.setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        switch (this.getSporelingType().getHostility()) {
            case HOSTILE:
                return CNBSoundEvents.SPORELING_NETHER_HURT.get();
            case NEUTRAL:
                return CNBSoundEvents.SPORELING_WARPED_HURT.get();
            case FRIENDLY:
            default:
                return CNBSoundEvents.SPORELING_OVERWORLD_HURT.get();
        }
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        switch (this.getSporelingType().getHostility()) {
            case HOSTILE:
                return CNBSoundEvents.SPORELING_NETHER_HURT.get();
            case NEUTRAL:
                return CNBSoundEvents.SPORELING_WARPED_HURT.get();
            case FRIENDLY:
            default:
                return CNBSoundEvents.SPORELING_OVERWORLD_HURT.get();
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        switch (this.getSporelingType().getHostility()) {
            case HOSTILE:
                return CNBSoundEvents.SPORELING_NETHER_AMBIENT.get();
            case NEUTRAL:
                return CNBSoundEvents.SPORELING_WARPED_AMBIENT.get();
            case FRIENDLY:
            default:
                return CNBSoundEvents.SPORELING_OVERWORLD_AMBIENT.get();
        }
    }

    protected boolean shouldDespawnInPeaceful() {
        return this.getSporelingType().getHostility() == SporelingType.SporelingHostility.HOSTILE;
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (this.isPassenger() && (this.getVehicle().isSpectator() || this.getFluidHeight(FluidTags.WATER) > this.getFluidJumpThreshold() || this.getVehicle().getVehicle() instanceof EndWhaleEntity || this.getVehicle().isVisuallySwimming())) {
            this.stopRiding();
            this.setOrderedToSit(false);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.getSporelingType().getHostility().equals(FRIENDLY);
    }

    public void setSporelingType(SporelingType shroomloinType) {
        this.entityData.set(TYPE, shroomloinType.getId().toString());
    }

    public SporelingType getSporelingType() {
        return SporelingType.getById(this.entityData.get(TYPE));
    }

    private static final RawAnimation SIT_ANIMATION = RawAnimation.begin().thenPlay("sporeling_sit").thenLoop("sporeling_sitting");
    private static final RawAnimation STAND_ANIMATION = RawAnimation.begin().thenPlay("sporeling_stand");
    private static final RawAnimation BITE_ANIMATION = RawAnimation.begin().thenPlay("sporeling_bite");
    private static final RawAnimation WAVE_ANIMATION = RawAnimation.begin().thenPlay("sporeling_wave");
    private static final RawAnimation CONVERT_ANIMATION = RawAnimation.begin().thenPlay("sporeling_convert");
    private static final RawAnimation RUN_ANIMATION = RawAnimation.begin().thenLoop("sporeling_run");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("sporeling_walk");
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("sporeling_idle");

    private static final RawAnimation BACKPACK_AIR_ANIMATION = RawAnimation.begin().thenLoop("sporeling_backpack_air");
    private static final RawAnimation BACKPACK_IDLE_ANIMATION = RawAnimation.begin().thenLoop("sporeling_backpack_idle");

    public <E extends GeoAnimatable> PlayState animationPredicate(AnimationTest<E> event) {
        RawAnimation currentAnimation = event.controller().getCurrentRawAnimation();
        boolean isSittingAnimation = currentAnimation != null && currentAnimation.getAnimationStages().stream().anyMatch(stage -> stage.animationName().equals("sporeling_sitting"));
        boolean isStandingAnimation = currentAnimation != null && currentAnimation.getAnimationStages().stream().anyMatch(stage -> stage.animationName().equals("sporeling_stand"));

        if (this.isPassenger()) {
            event.controller().reset();
            return PlayState.STOP;
        }

        double xMovement = this.getX() - this.xo;
        double zMovement = this.getZ() - this.zo;
        boolean isMoving = xMovement * xMovement + zMovement * zMovement > 1.0E-6D;

        if (this.isInSittingPose()) {
            event.controller().setAnimation(SIT_ANIMATION);
        } else if ((isSittingAnimation || (isStandingAnimation && event.controller().isAnimatingBones())) && !this.isInSittingPose()) {
            event.controller().setAnimation(STAND_ANIMATION);
        } else if (this.isAttacking()) {
            event.controller().setAnimation(BITE_ANIMATION);
        } else if (this.isWaving() && this.getHolding().isEmpty()) {
            event.controller().setAnimation(WAVE_ANIMATION);
        } else if (this.isInspecting()) {
            event.controller().setAnimation(CONVERT_ANIMATION);
        } else if (isMoving) {
            if (this.isRunning()) {
                event.controller().setAnimation(RUN_ANIMATION);
            } else {
                event.controller().setAnimation(WALK_ANIMATION);
            }
        } else {
            event.controller().setAnimation(IDLE_ANIMATION);
        }

        return PlayState.CONTINUE;
    }

    public <E extends GeoAnimatable> PlayState backpackAnimationPredicate(AnimationTest<E> event) {
        Entity vehicle = this.getVehicle();

        if (vehicle != null) {
            if (!vehicle.onGround() && vehicle.fallDistance > 0.1F) {
                event.controller().setAnimation(BACKPACK_AIR_ANIMATION);
            } else {
                event.controller().setAnimation(BACKPACK_IDLE_ANIMATION);
            }
            return PlayState.CONTINUE;
        }

        event.controller().reset();
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar animationData) {
        animationData.add(new AnimationController<>("controller", 0, this::animationPredicate));
        animationData.add(new AnimationController<>("backpackController", 6, this::backpackAnimationPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    static class WaveGoal extends LookAtPlayerGoal {
        private final SporelingEntity sporeling;

        public WaveGoal(SporelingEntity entityIn, Class<? extends LivingEntity> watchTargetClass, float maxDistance) {
            super(entityIn, watchTargetClass, maxDistance);
            sporeling = entityIn;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !sporeling.isInspecting() && sporeling.random.nextDouble() <= 0.25D && !sporeling.isInSittingPose();
        }

        @Override
        public void start() {
            super.start();
            this.sporeling.setWaving(true);
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.sporeling.isWaving();
        }
    }

    static class SporelingAttackGoal extends MeleeAttackGoal {
        private final SporelingEntity goalOwner;

        public SporelingAttackGoal(SporelingEntity sporeling, double speedModifier, boolean followWithoutLineOfSight) {
            super(sporeling, speedModifier, followWithoutLineOfSight);
            this.goalOwner = sporeling;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity entity) {
            if (this.canPerformAttack(entity) && this.goalOwner.attackTimer <= 0) {
                this.resetAttackCooldown();
                this.goalOwner.playSound(CNBSoundEvents.SPORELING_BITE.get(), 1.0F, 1.0F);
                if (this.goalOwner.level() instanceof ServerLevel serverLevel) {
                    this.goalOwner.doHurtTarget(serverLevel, entity);
                }
            }
        }

        @Override
        protected void resetAttackCooldown() {
            super.resetAttackCooldown();
            this.goalOwner.setAttacking(true);
        }
    }

    static class SporelingTemptGoal extends TemptGoal {
        private final SporelingEntity sporelingEntity;

        public SporelingTemptGoal(SporelingEntity sporeling, double speedModifier, Ingredient temptItems, boolean canScare) {
            super(sporeling, speedModifier, temptItems, canScare);
            this.sporelingEntity = sporeling;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.sporelingEntity.isTame();
        }
    }
}
