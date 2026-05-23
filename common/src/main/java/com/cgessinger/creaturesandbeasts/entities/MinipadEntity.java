package com.cgessinger.creaturesandbeasts.entities;

import com.cgessinger.creaturesandbeasts.entities.ai.ReachableBlockTargetGoal;
import com.cgessinger.creaturesandbeasts.init.CNBMinipadTypes;
import com.cgessinger.creaturesandbeasts.init.CNBSoundEvents;
import com.cgessinger.creaturesandbeasts.util.MinipadGlow;
import com.cgessinger.creaturesandbeasts.util.MinipadType;
import com.cgessinger.creaturesandbeasts.util.ShearableMobInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
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

import java.util.Comparator;
import java.util.List;

public class MinipadEntity extends Animal implements Shearable, GeoEntity {
    public static final EntityDataAccessor<String> TYPE = SynchedEntityData.defineId(MinipadEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> SHEARED = SynchedEntityData.defineId(MinipadEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> GLOWING = SynchedEntityData.defineId(MinipadEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation SWIM_ANIMATION = RawAnimation.begin().thenLoop("minipad_swim");
    private static final RawAnimation FLOAT_ANIMATION = RawAnimation.begin().thenLoop("minipad_float");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("minipad_walk");

    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);
    private int shearedTimer;
    private boolean returningToWater;

    public MinipadEntity(EntityType<MinipadEntity> type, Level worldIn) {
        super(type, worldIn);
        this.shearedTimer = 0;
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.getNavigation().setCanFloat(true);

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                MinipadEntity minipad = (MinipadEntity) this.mob;
                if (minipad.shouldLookAround()) {
                    super.tick();
                }
            }
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, CNBMinipadTypes.PINK.getId().toString());
        builder.define(SHEARED, false);
        builder.define(GLOWING, false);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        MinipadType type = MinipadType.getById(input.getStringOr("MinipadType", CNBMinipadTypes.PINK.getId().toString()));
        if (type == null) {
            type = CNBMinipadTypes.PINK;
        }
        this.setMinipadType(type);
        this.shearedTimer = input.getIntOr("ShearedTimer", 0);
        this.setSheared(this.shearedTimer > 0);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("ShearedTimer", this.shearedTimer);
        output.putString("MinipadType", this.getMinipadType().getId().toString());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new MinipadFloatGoal(this));
        this.goalSelector.addGoal(1, new MinipadPanicGoal(this, 1.25D));
        this.goalSelector.addGoal(2, new MinipadReturnToWaterGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new MinipadRandomStrollGoal(this, 1.0D, 60, 240));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && --this.shearedTimer == 0) {
            this.setSheared(false);
        }

        if (!this.level().isClientSide()) {
            this.updateGlowingState();
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.floatMinipad();

        SimpleParticleType particle = this.getMinipadType().getParticle();
        if (particle != null && this.isGlowing() && !this.getSheared()) {
            if (this.random.nextDouble() < 0.1) {
                this.level().addParticle(particle, this.getX() + (this.random.nextDouble() * 0.5D - 0.25), this.getY() + 0.8 + (this.random.nextDouble() * 0.1D - 0.05), this.getZ() + (this.random.nextDouble() * 0.5D - 0.25), this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
            }
            if (this.random.nextDouble() < 0.07) {
                this.level().addParticle(particle, this.getX() + (this.random.nextDouble() * 24D - 12), this.getY() + this.random.nextDouble() * 7.5D, this.getZ() + (this.random.nextDouble() * 24D - 12), this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
            }
        }

    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        switch (this.random.nextInt(3)) {
            case 0:
            default:
                this.setMinipadType(CNBMinipadTypes.PINK);
                break;
            case 1:
                this.setMinipadType(CNBMinipadTypes.LIGHT_PINK);
                break;
            case 2:
                this.setMinipadType(CNBMinipadTypes.YELLOW);
                break;
        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public static boolean checkMinipadSpawnRules(EntityType<MinipadEntity> animal, LevelAccessor worldIn, EntitySpawnReason reason, BlockPos pos, RandomSource randomIn) {
        return true;
    }

    @Override
    protected void pushEntities() {
        List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(0.2, 0, 0.2), EntitySelector.pushableBy(this));
        if (!list.isEmpty()) {
            int i = this.level() instanceof ServerLevel serverLevel ? serverLevel.getGameRules().get(GameRules.MAX_ENTITY_CRAMMING) : 0;
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
                int j = 0;

                for (Entity entity : list) {
                    if (!entity.isPassenger()) {
                        ++j;
                    }
                }

                if (j > i - 1) {
                    this.hurt(this.damageSources().cramming(), 6.0F);
                }
            }

            for (Entity entity : list) {
                this.doPush(entity);
            }
        }

    }

    @Override
    public boolean canBeCollidedWith(Entity entity) {
        return this.isAlive();
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState) {
        return !this.returningToWater && fluidState.is(FluidTags.WATER);
    }

    private void floatMinipad() {
        if (this.isInWater()) {
            CollisionContext collisionContext = CollisionContext.of(this);
            if (collisionContext.isAbove(this.getLiquidCollisionShape(), this.blockPosition(), true)
                    && !this.level().getFluidState(this.blockPosition().above()).is(FluidTags.WATER)) {
                this.setOnGround(true);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D).add(0.0D, 0.1D, 0.0D));
            }
        }

    }

    @Override
    public VoxelShape getLiquidCollisionShape() {
        return Block.column(16.0D, 0.0D, 8.0D);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob entity) {
        return null;
    }

    public boolean getSheared() {
        return this.entityData.get(SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.shearedTimer = sheared ? 18000 : 0;
        this.entityData.set(SHEARED, sheared);
    }

    public void setGlowing(boolean isGlowing) {
        this.entityData.set(GLOWING, isGlowing);
    }

    public boolean isGlowing() {
        return this.entityData.get(GLOWING);
    }

    private void updateGlowingState() {
        boolean shouldGlow = MinipadGlow.isNightGlowTime(this.level().getDefaultClockTime());
        if (this.isGlowing() != shouldGlow) {
            this.setGlowing(shouldGlow);
        }
    }

    public void setMinipadType(MinipadType minipadType) {
        this.entityData.set(TYPE, minipadType.getId().toString());
    }

    public MinipadType getMinipadType() {
        return MinipadType.getById(this.entityData.get(TYPE));
    }

    @Override
    public double getFluidJumpThreshold() {
        return 0.45D;
    }

    @Override
    public void shear(ServerLevel level, SoundSource source, ItemStack stack) {
        level.playSound(null, this, SoundEvents.SHEEP_SHEAR, source, 1.0F, 1.0F);
        this.setSheared(true);

        ItemEntity item = this.spawnAtLocation(level, this.createShearedFlowerStack(level), 1);

        if (item != null) {
            item.addDeltaMovement(new Vec3((this.random.nextFloat() - this.random.nextFloat()) * 0.1F, this.random.nextFloat() * 0.05F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
        }
    }

    private ItemStack createShearedFlowerStack(ServerLevel level) {
        if (MinipadGlow.dropsGlowingFlowerAt(level.getDefaultClockTime())) {
            return new ItemStack(this.getMinipadType().getGlowShearItem());
        }

        return new ItemStack(this.getMinipadType().getShearItem());
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.getSheared();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return ShearableMobInteraction.tryShear(this, player, hand)
                .orElseGet(() -> super.mobInteract(player, hand));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    public boolean shouldLookAround() {
        return !this.isInWaterHabitat();
    }

    private boolean isTouchingWater() {
        BlockPos pos = this.blockPosition();
        return this.getFluidHeight(FluidTags.WATER) > 0.0D
                || this.isInWater()
                || this.level().getFluidState(pos).is(FluidTags.WATER);
    }

    private boolean isStandingOnWaterSurface() {
        BlockPos pos = this.blockPosition();
        return this.level().getBlockState(pos).isAir() && this.level().getFluidState(pos.below()).is(FluidTags.WATER);
    }

    private boolean isInWaterHabitat() {
        return this.isTouchingWater() || this.isStandingOnWaterSurface();
    }

    private boolean isWaterStrollTarget(BlockPos pos) {
        return this.level().getFluidState(pos).is(FluidTags.WATER);
    }

    private void setReturningToWater(boolean returningToWater) {
        this.returningToWater = returningToWater;
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return 2 + this.getRandom().nextInt(3);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        if (!blockIn.getFluidState().isEmpty()) {
            this.playSound(CNBSoundEvents.MINIPAD_STEP.get(), this.getSoundVolume() * 0.3F, this.getVoicePitch());
        }
    }

    @Override
    protected SoundEvent getSwimSound() {
        return CNBSoundEvents.MINIPAD_SWIM.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return CNBSoundEvents.MINIPAD_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return CNBSoundEvents.MINIPAD_HURT.get();
    }

    private <E extends GeoAnimatable> PlayState animationPredicate(AnimationTest<E> event) {
        double xMovement = this.getX() - this.xo;
        double zMovement = this.getZ() - this.zo;
        boolean isMoving = xMovement * xMovement + zMovement * zMovement > 1.0E-4D;

        if (this.isInWater() && isMoving) {
            event.controller().setAnimation(SWIM_ANIMATION);
            return PlayState.CONTINUE;
        } else if (this.isInWater()) {
            event.controller().setAnimation(FLOAT_ANIMATION);
            return PlayState.CONTINUE;
        } else if (isMoving) {
            event.controller().setAnimation(WALK_ANIMATION);
            return PlayState.CONTINUE;
        }
        event.controller().reset();
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar animationData) {
        animationData.add(new AnimationController<>("controller", 0, this::animationPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    static class MinipadFloatGoal extends FloatGoal {
        private final MinipadEntity minipad;

        public MinipadFloatGoal(MinipadEntity minipad) {
            super(minipad);
            this.minipad = minipad;
        }

        @Override
        public void tick() {
            if (this.minipad.getFluidHeight(FluidTags.WATER) > 0.5D) {
                this.minipad.getJumpControl().jump();
            } else {
                this.minipad.setDeltaMovement(this.minipad.getDeltaMovement().add(0.0D, 0.005D, 0.0D));
            }
        }
    }

    static class MinipadPanicGoal extends PanicGoal {
        private final MinipadEntity minipad;

        public MinipadPanicGoal(MinipadEntity minipad, double speedModifier) {
            super(minipad, speedModifier);
            this.minipad = minipad;
        }

        @Override
        public void start() {
            this.minipad.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
            this.isRunning = true;
        }

        @Override
        protected boolean findRandomPosition() {
            boolean flag = GoalUtils.mobRestricted(this.minipad, 5);
            Vec3 vec3 = RandomPos.generateRandomPos(this.minipad, () -> {
                BlockPos blockpos = RandomPos.generateRandomDirection(this.minipad.getRandom(), 5, 4);
                return generateRandomPosTowardDirection(this.minipad, 5, flag, blockpos);
            });
            if (vec3 == null) {
                return false;
            }

            this.posX = vec3.x;
            this.posY = vec3.y;
            this.posZ = vec3.z;
            return true;
        }

        @Nullable
        private static BlockPos generateRandomPosTowardDirection(MinipadEntity minipad, int horizontalRange, boolean flag, BlockPos posTowards) {
            BlockPos blockpos = RandomPos.generateRandomPosTowardDirection(minipad, horizontalRange, minipad.getRandom(), posTowards);
            return !GoalUtils.isOutsideLimits(blockpos, minipad) && !GoalUtils.isRestricted(flag, minipad, blockpos) && !GoalUtils.hasMalus(minipad, blockpos) && (!GoalUtils.isNotStable(minipad.getNavigation(), blockpos) || GoalUtils.isWater(minipad, blockpos)) ? blockpos : null;
        }
    }

    static class MinipadRandomStrollGoal extends RandomStrollGoal {
        private final MinipadEntity minipad;
        private final int intervalLand;
        private final int intervalWater;
        private final boolean checkNoActionTime;

        public MinipadRandomStrollGoal(MinipadEntity minipad, double speedModifier) {
            this(minipad, speedModifier, 60, 120);
        }

        public MinipadRandomStrollGoal(MinipadEntity minipad, double speedModifier, int intervalLand, int intervalWater) {
            this(minipad, speedModifier, intervalLand, intervalWater, true);
        }

        public MinipadRandomStrollGoal(MinipadEntity minipad, double speedModifier, int intervalLand, int intervalWater, boolean checkNoActionTime) {
            super(minipad, speedModifier, intervalLand, checkNoActionTime);
            this.minipad = minipad;
            this.intervalLand = intervalLand;
            this.intervalWater = intervalWater;
            this.checkNoActionTime = checkNoActionTime;
        }

        @Override
        public void start() {
            this.minipad.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        }

        @Override
        public boolean canUse() {
            if (this.mob.isVehicle()) {
                return false;
            } else {
                if (!this.forceTrigger) {
                    if (this.checkNoActionTime && this.mob.getNoActionTime() >= 100) {
                        return false;
                    }

                    int i = this.minipad.isInWaterHabitat() ? this.intervalWater : this.intervalLand;
                    if (this.mob.getRandom().nextInt(reducedTickDelay(i)) != 0) {
                        return false;
                    }
                }

                Vec3 vec3 = this.getPosition();
                if (vec3 == null) {
                    return false;
                } else {
                    this.wantedX = vec3.x;
                    this.wantedY = vec3.y;
                    this.wantedZ = vec3.z;
                    this.forceTrigger = false;
                    return true;
                }
            }
        }

        @Override
        protected Vec3 getPosition() {
            boolean flag = GoalUtils.mobRestricted(this.minipad, 10);

            if (this.minipad.isInWaterHabitat()) {
                return RandomPos.generateRandomPos(this.minipad, () -> {
                    BlockPos blockpos = RandomPos.generateRandomDirection(this.minipad.getRandom(), 10, 1);
                    return generateWaterPosTowardDirection(this.minipad, 10, flag, blockpos);
                });
            }

            return RandomPos.generateRandomPos(this.minipad, () -> {
                BlockPos blockpos = RandomPos.generateRandomDirection(this.minipad.getRandom(), 10, 7);
                return generateRandomPosTowardDirection(this.minipad, 10, flag, blockpos);
            });
        }

        @Nullable
        private static BlockPos generateWaterPosTowardDirection(MinipadEntity minipad, int horizontalRange, boolean flag, BlockPos posTowards) {
            BlockPos blockpos = generateRandomPosTowardDirection(minipad, horizontalRange, flag, posTowards);
            return blockpos != null && minipad.isWaterStrollTarget(blockpos) ? blockpos : null;
        }

        @Nullable
        private static BlockPos generateRandomPosTowardDirection(MinipadEntity minipad, int horizontalRange, boolean flag, BlockPos posTowards) {
            BlockPos blockpos = RandomPos.generateRandomPosTowardDirection(minipad, horizontalRange, minipad.getRandom(), posTowards);
            return !GoalUtils.isOutsideLimits(blockpos, minipad) && !GoalUtils.isRestricted(flag, minipad, blockpos) && !GoalUtils.hasMalus(minipad, blockpos) && (!GoalUtils.isNotStable(minipad.getNavigation(), blockpos) || GoalUtils.isWater(minipad, blockpos)) ? blockpos : null;
        }
    }

    static class MinipadReturnToWaterGoal extends ReachableBlockTargetGoal {
        private static final int LAND_SEARCH_DELAY = 200;
        private static final int SEARCH_RETRY_INTERVAL = 3600;
        private static final int SEARCH_RANGE = 20;
        private static final int PATH_RECALC_INTERVAL = 20;
        private static final int MAX_RETURN_TICKS = 200;

        private final MinipadEntity minipad;
        private int landStartTick = -1;
        private int nextSearchTick;

        public MinipadReturnToWaterGoal(MinipadEntity minipad, double speedModifier) {
            super(minipad, speedModifier, PATH_RECALC_INTERVAL, MAX_RETURN_TICKS);
            this.minipad = minipad;
        }

        @Override
        public boolean canUse() {
            if (this.minipad.isInWaterHabitat()) {
                this.landStartTick = -1;
                return false;
            }

            if (!this.minipad.onGround()) {
                this.landStartTick = -1;
                return false;
            }

            if (!this.hasWaitedOnLand()) {
                return false;
            }

            if (this.minipad.tickCount < this.nextSearchTick) {
                return false;
            }

            return this.searchReachableWater();
        }

        @Override
        protected boolean canContinueMoving() {
            return !this.minipad.isInWater();
        }

        @Override
        public void start() {
            this.minipad.setReturningToWater(true);
            super.start();
        }

        @Override
        public void stop() {
            if (this.minipad.isInWater()) {
                this.landStartTick = -1;
            } else {
                this.nextSearchTick = this.minipad.tickCount + this.adjustedTickDelay(SEARCH_RETRY_INTERVAL);
            }

            this.minipad.setReturningToWater(false);
            super.stop();
        }

        private boolean hasWaitedOnLand() {
            if (this.landStartTick < 0) {
                this.landStartTick = this.minipad.tickCount;
            }

            return this.minipad.tickCount - this.landStartTick >= LAND_SEARCH_DELAY;
        }

        private boolean searchReachableWater() {
            boolean wasReturningToWater = this.minipad.returningToWater;
            this.minipad.setReturningToWater(true);

            try {
                if (this.setReachableTarget(this.collectSurfaceWaterCandidates())) {
                    return true;
                }
            } finally {
                this.minipad.setReturningToWater(wasReturningToWater);
            }

            this.nextSearchTick = this.minipad.tickCount + this.adjustedTickDelay(SEARCH_RETRY_INTERVAL);
            return false;
        }

        private List<BlockPos> collectSurfaceWaterCandidates() {
            BlockPos origin = this.minipad.blockPosition();
            return this.collectCandidates(SEARCH_RANGE, SEARCH_RANGE, this::isSurfaceWaterTarget, Comparator.comparingDouble(origin::distSqr));
        }

        private boolean isSurfaceWaterTarget(BlockPos pos) {
            LevelReader level = this.minipad.level();
            return level.getFluidState(pos).is(FluidTags.WATER)
                    && !level.getFluidState(pos.above()).is(FluidTags.WATER)
                    && level.getBlockState(pos.above()).isAir();
        }
    }
}
