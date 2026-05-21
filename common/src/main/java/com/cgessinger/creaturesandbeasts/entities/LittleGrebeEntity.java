package com.cgessinger.creaturesandbeasts.entities;

import com.cgessinger.creaturesandbeasts.entities.ai.GoToWaterGoal;
import com.cgessinger.creaturesandbeasts.entities.ai.MountAdultGoal;
import com.cgessinger.creaturesandbeasts.entities.ai.SmoothSwimGoal;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import com.cgessinger.creaturesandbeasts.init.CNBSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
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

import java.util.Random;
import static com.cgessinger.creaturesandbeasts.init.CNBTags.Items.LITTLE_GREBE_FOOD;

public class LittleGrebeEntity extends Animal implements GeoEntity {
    private static final EntityDataAccessor<BlockPos> TRAVEL_POS = SynchedEntityData.defineId(LittleGrebeEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final Identifier HEALTH_REDUCTION_ID = Identifier.fromNamespaceAndPath("cnb", "little_grebe_baby_health_reduction");
    public float flapSpeed;
    private float nextFlap = 1.0F;
    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);

    public LittleGrebeEntity(EntityType<LittleGrebeEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MountAdultGoal(this, 1.2D));
        this.goalSelector.addGoal(2, new SmoothSwimGoal(this));
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0D, this::isFood, false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(6, new LittleGrebeEntity.LittleGrebeRandomStrollGoal(this, 1.0D, 60, 1));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new GoToWaterGoal(this, 0.8D));
    }

    public static boolean checkGrebeSpawnRules(EntityType<LittleGrebeEntity> animal, LevelAccessor worldIn, EntitySpawnReason reason, BlockPos pos, RandomSource randomIn) {
        return worldIn.getRawBrightness(pos, 0) > 8;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, EntitySpawnReason reason, @Nullable SpawnGroupData spawnDataIn) {
        if (spawnDataIn == null) {
            spawnDataIn = new AgeableMobGroupData(0.6F);
        }
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
    }

    @Override
    public void setAge(int age) {
        super.setAge(age);
        double MAX_HEALTH = this.getAttribute(Attributes.MAX_HEALTH).getValue();
        float babyHealth = 5.0F;
        if (isBaby() && MAX_HEALTH > babyHealth) {
            this.getAttribute(Attributes.MAX_HEALTH).addOrUpdateTransientModifier(new AttributeModifier(HEALTH_REDUCTION_ID, babyHealth - MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE));
            this.setHealth(babyHealth);
        }
    }

    @Override
    protected void ageBoundaryReached() {
        this.stopRiding();
        this.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_REDUCTION_ID);
        this.setHealth((float) this.getAttribute(Attributes.MAX_HEALTH).getValue());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.flapSpeed += (this.onGround() ? -1.0F : 4.0F) * 0.3F;
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);

        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && !this.isBaby() && vec3.y < 0.0D) {
            this.setDeltaMovement(vec3.multiply(1.0D, 0.6D, 1.0D));
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_) {
        return CNBEntityTypes.LITTLE_GREBE.get().create(p_241840_1_, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean causeFallDamage(double distance, float damageMultiplier, DamageSource source) {
        return this.isBaby() && super.causeFallDamage(distance, damageMultiplier, source);
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isBaby()) {
            return CNBSoundEvents.LITTLE_GREBE_CHICK_AMBIENT.get();
        }
        return CNBSoundEvents.LITTLE_GREBE_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return CNBSoundEvents.LITTLE_GREBE_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return CNBSoundEvents.LITTLE_GREBE_HURT.get();
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        if (passenger instanceof LittleGrebeEntity grebe && grebe.isBaby()) {
            return new Vec3(0.0D, 5.0D / 16.0D * scale, 0.0D);
        }
        return super.getPassengerAttachmentPoint(passenger, dimensions, scale);
    }

    private BlockPos getTravelPos() {
        return this.entityData.get(TRAVEL_POS);
    }

    @Override
    public int getMaxHeadYRot() {
        return 60;
    }

    @Override
    public int getMaxHeadXRot() {
        return 35;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TRAVEL_POS, new BlockPos(0, 2, 0));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(LITTLE_GREBE_FOOD);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.1F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            if (this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
            }
        } else {
            super.travel(travelVector);
        }

    }

    private static final RawAnimation FALL_ANIMATION = RawAnimation.begin().thenLoop("little_grebe.fall");
    private static final RawAnimation SWIM_ANIMATION = RawAnimation.begin().thenLoop("little_grebe.swim");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("little_grebe.walk");
    private static final RawAnimation CHICK_WALK_ANIMATION = RawAnimation.begin().thenLoop("little_grebe_chick.walk");

    private <E extends GeoAnimatable> PlayState animationPredicate(AnimationTest<E> event) {
        double xMovement = this.getX() - this.xo;
        double zMovement = this.getZ() - this.zo;
        boolean isMoving = xMovement * xMovement + zMovement * zMovement > 1.0E-4D;

        if (!(this.onGround() || this.isInWater() || this.isBaby())) {
            event.controller().setAnimation(FALL_ANIMATION);
            return PlayState.CONTINUE;
        } else if (this.isInWater()) {
            event.controller().setAnimation(SWIM_ANIMATION);
            return PlayState.CONTINUE;
        } else if (isMoving) {
            if (this.isBaby()) {
                event.controller().setAnimation(CHICK_WALK_ANIMATION);
            } else {
                event.controller().setAnimation(WALK_ANIMATION);
            }
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

    static class LittleGrebeRandomStrollGoal extends RandomStrollGoal {
        private static final Random rand = new Random();
        private final LittleGrebeEntity littleGrebe;
        private final int intervalLand;
        private final int intervalWater;
        private final boolean checkNoActionTime;

        public LittleGrebeRandomStrollGoal(LittleGrebeEntity littleGrebe, double speedModifier) {
            this(littleGrebe, speedModifier, 60, 120);
        }

        public LittleGrebeRandomStrollGoal(LittleGrebeEntity littleGrebe, double speedModifier, int intervalLand, int intervalWater) {
            this(littleGrebe, speedModifier, intervalLand, intervalWater, true);
        }

        public LittleGrebeRandomStrollGoal(LittleGrebeEntity littleGrebe, double speedModifier, int intervalLand, int intervalWater, boolean checkNoActionTime) {
            super(littleGrebe, speedModifier, intervalLand, checkNoActionTime);
            this.littleGrebe = littleGrebe;
            this.intervalLand = intervalLand;
            this.intervalWater = intervalWater;
            this.checkNoActionTime = checkNoActionTime;
        }

        @Override
        public void start() {
            this.littleGrebe.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
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

                    int i = this.littleGrebe.isInWater() ? this.intervalWater : this.intervalLand;
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
            boolean flag = GoalUtils.mobRestricted(this.littleGrebe, 10);

            return RandomPos.generateRandomPos(this.littleGrebe, () -> {
                BlockPos blockpos = this.littleGrebe.isInWater() ? RandomPos.generateRandomDirection(this.littleGrebe.getRandom(), 10, 1) : RandomPos.generateRandomDirection(this.littleGrebe.getRandom(), 10, 7);
                return generateRandomPosTowardDirection(this.littleGrebe, 10, flag, blockpos);
            });
        }

        @Nullable
        private static BlockPos generateRandomPosTowardDirection(LittleGrebeEntity littleGrebe, int horizontalRange, boolean flag, BlockPos posTowards) {
            BlockPos blockpos = RandomPos.generateRandomPosTowardDirection(littleGrebe, horizontalRange, littleGrebe.getRandom(), posTowards);
            return !GoalUtils.isOutsideLimits(blockpos, littleGrebe) && !GoalUtils.isRestricted(flag, littleGrebe, blockpos) && !GoalUtils.hasMalus(littleGrebe, blockpos) && (!GoalUtils.isNotStable(littleGrebe.getNavigation(), blockpos) || GoalUtils.isWater(littleGrebe, blockpos)) ? blockpos : null;
        }
    }
}
