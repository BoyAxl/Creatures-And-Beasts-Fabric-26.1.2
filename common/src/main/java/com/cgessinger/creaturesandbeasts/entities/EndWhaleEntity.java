package com.cgessinger.creaturesandbeasts.entities;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.init.CNBSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

import java.util.EnumSet;
import java.util.List;

import static com.cgessinger.creaturesandbeasts.init.CNBTags.Items.END_WHALE_FOOD;

public class EndWhaleEntity extends TamableAnimal implements FlyingAnimal, GeoEntity {
    private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(EndWhaleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double RIDING_HEIGHT_SCALE = 0.55D;
    private static final float JUMP_VERTICAL_INPUT = 0.5F;
    private static final double FREE_FLIGHT_SPEED_SCALE = 0.18D;
    private static final double FREE_FLIGHT_ACCELERATION = 0.08D;
    private static final double FREE_FLIGHT_ARRIVAL_DISTANCE_SQR = 4.0D;
    private static final float FREE_FLIGHT_YAW_LERP = 0.04F;
    private static final float FREE_FLIGHT_PITCH_LERP = 0.04F;
    private static final float FREE_FLIGHT_MAX_PITCH = 35.0F;
    private static final double TEMPT_STOP_DISTANCE = 2.5D;
    private static final double TEMPT_STOP_DISTANCE_SQR = TEMPT_STOP_DISTANCE * TEMPT_STOP_DISTANCE;
    private static final double TEMPT_SEARCH_RANGE = 50.0D;
    private static final double TEMPT_SEARCH_RANGE_SQR = TEMPT_SEARCH_RANGE * TEMPT_SEARCH_RANGE;
    private static final double TEMPT_VERTICAL_OFFSET = 1.0D;
    private static final int ISLAND_SURFACE_TARGET_CHANCE = 5;
    private static final int ISLAND_SURFACE_SEARCH_ATTEMPTS = 12;
    private static final int ISLAND_SURFACE_HORIZONTAL_RADIUS = 32;
    private static final int ISLAND_SURFACE_MIN_HEIGHT_ABOVE = 8;
    private static final int ISLAND_SURFACE_RANDOM_HEIGHT = 12;
    private static final int SURFACE_SPAWN_MIN_HEIGHT_ABOVE = 20;
    private static final int SURFACE_SPAWN_MAX_HEIGHT_ABOVE = 50;
    private static final int SURFACE_SPAWN_RANDOM_HEIGHT = SURFACE_SPAWN_MAX_HEIGHT_ABOVE - SURFACE_SPAWN_MIN_HEIGHT_ABOVE;
    private static final int MAX_SPAWN_CLUSTER_SIZE = 1;
    private static final int HERD_CHECK_INTERVAL = 60;
    private static final double HERD_SEARCH_RANGE = 40.0D;
    private static final double HERD_SEARCH_RANGE_SQR = HERD_SEARCH_RANGE * HERD_SEARCH_RANGE;
    private static final double HERD_FOLLOW_DISTANCE = 30.0D;
    private static final double HERD_FOLLOW_DISTANCE_SQR = HERD_FOLLOW_DISTANCE * HERD_FOLLOW_DISTANCE;
    private static final double HERD_STOP_DISTANCE = 22.0D;
    private static final double HERD_STOP_DISTANCE_SQR = HERD_STOP_DISTANCE * HERD_STOP_DISTANCE;
    private static final double HERD_SPEED_MODIFIER = 0.9D;

    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);

    public EndWhaleEntity(EntityType<EndWhaleEntity> entityType, Level level) {
        super(entityType, level);
        this.setTame(false, false);
        this.setNoGravity(true);
        this.moveControl = new EndWhaleMoveControl(this);
        this.lookControl = new EndWhaleLookControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 160.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 100.0D)
                .add(Attributes.FLYING_SPEED, 1.0D);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new EndWhaleTemptGoal(this, 1.25D, Ingredient.of(this.registryAccess().lookupOrThrow(Registries.ITEM).getOrThrow(END_WHALE_FOOD))));
        this.goalSelector.addGoal(1, new EndWhaleFollowHerdGoal(this));
        this.goalSelector.addGoal(2, new EndWhaleWanderGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SADDLED, false);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Saddled", this.isSaddled());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        boolean isSaddled = input.getBooleanOr("Saddled", false);
        if (isSaddled) {
            this.equipSaddle(SoundSource.PLAYERS);
        }
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new EndWhaleBodyRotationControl(this);
    }

    @Override
    public int getMaxHeadYRot() {
        return 0;
    }

    @Override
    public int getHeadRotSpeed() {
        return 30;
    }

    public boolean isSaddleable() {
        return this.isTame();
    }

    public void equipSaddle(@Nullable SoundSource soundSource) {
        this.entityData.set(SADDLED, true);
        this.playSound(SoundEvents.HORSE_SADDLE.value(), 1.0F, 1.0F);
    }

    public void removeSaddle() {
        this.entityData.set(SADDLED, false);
        if (this.level() instanceof ServerLevel serverLevel) {
            ItemStack equippedSaddle = this.getItemBySlot(EquipmentSlot.SADDLE);
            if (equippedSaddle.isEmpty()) {
                this.spawnAtLocation(serverLevel, Items.SADDLE);
            } else {
                ItemStack saddleToDrop = equippedSaddle.copy();
                this.setItemSlot(EquipmentSlot.SADDLE, ItemStack.EMPTY);
                this.spawnAtLocation(serverLevel, saddleToDrop);
            }
        }
        this.playSound(SoundEvents.HORSE_SADDLE.value(), 0.8F, 1.0F);
    }

    @Override
    public boolean isSaddled() {
        return this.entityData.get(SADDLED) || this.hasItemInSlot(EquipmentSlot.SADDLE);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return super.canUseSlot(slot) && (slot != EquipmentSlot.SADDLE || this.isSaddleable());
    }

    private void mountWhale(Player player) {
        if (!this.level().isClientSide()) {
            player.setYRot(this.getYRot());
            player.setXRot(this.getXRot());
            player.startRiding(this);
        }
    }

    @Override
    protected void positionRider(Entity rider, MoveFunction callback) {
        if (this.hasPassenger(rider)) {
            double verticalOffset = this.getWhaleRidingOffset();
            float whaleRoll = this.getWhaleRoll(rider) * Mth.PI/180;
            float whalePitch = this.getWhalePitch(rider) * Mth.PI/180;
            callback.accept(rider, this.getX() + Mth.cos(this.getYRot() * Mth.PI/180) * verticalOffset * Mth.sin(whaleRoll) + Mth.sin(this.getYRot() * Mth.PI/180) * verticalOffset * Mth.sin(whalePitch),
                this.getY() + verticalOffset * Mth.cos(whaleRoll) * Mth.cos(whalePitch),
                this.getZ() + Mth.sin(this.getYRot() * Mth.PI/180) * verticalOffset * Mth.sin(whaleRoll) - Mth.cos(this.getYRot() * Mth.PI/180) * verticalOffset * Mth.sin(whalePitch));

            this.clampRotation(rider);
        }
    }

    protected void clampRotation(Entity rider) {
        rider.setYBodyRot(this.getYRot());
        float f = Mth.wrapDegrees(rider.getYRot() - this.getYRot());
        float f1 = Mth.clamp(f, -90.0F, 90.0F);
        rider.yRotO += f1 - f;
        rider.setYRot(rider.getYRot() + f1 - f);
        rider.setYHeadRot(rider.getYRot());
    }

    private double getWhaleRidingOffset() {
        return (double)this.getDimensions(this.getPose()).height() * RIDING_HEIGHT_SCALE;
    }

    private float getWhaleRoll(Entity rider) {
        float whaleRotY = this.getYRot();
        float riderRotY = rider.getYRot();

        return Mth.wrapDegrees(whaleRotY - riderRotY) / 2;
    }

    private float getWhalePitch(Entity rider) {
        float whaleRotY = this.getXRot();
        float riderRotY = rider.getXRot();

        return Mth.wrapDegrees(whaleRotY - riderRotY);
    }

    public boolean isControlledByLocalInstance() {
        return this.getControllingPassenger() instanceof LivingEntity;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive()) {
            if (this.isVehicle() && this.isControlledByLocalInstance() && this.isSaddled()) {
                LivingEntity livingentity = (LivingEntity)this.getControllingPassenger();
                this.setYRot(Mth.rotLerp(0.05F, this.getYRot(), livingentity.getYRot()));
                this.yRotO = this.getYRot();
                this.setXRot(livingentity.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.yBodyRot;
                float forwardMovement = livingentity.zza;
                if (forwardMovement <= 0.0F) {
                    forwardMovement *= 0.25F;
                }

                float verticalMovement = 0;

                if (Mth.abs(livingentity.getXRot()) > 7.0F) {
                    verticalMovement = Mth.rotLerp(0.01F, this.getXRot(), livingentity.getXRot()) * -forwardMovement/50;
                }

                if (livingentity.isJumping()) {
                    verticalMovement = Math.max(verticalMovement, JUMP_VERTICAL_INPUT);
                }

                if (this.isControlledByLocalInstance()) {
                    this.setSpeed((float)this.getAttributeValue(Attributes.FLYING_SPEED));

                    Vec3 proposedMovement = new Vec3(0, verticalMovement, forwardMovement);

                    if (this.isInLava()) {
                        this.moveRelative(0.02F, proposedMovement);
                        this.move(MoverType.SELF, this.getDeltaMovement());
                        this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
                    } else {
                        BlockPos ground = BlockPos.containing(this.getX(), this.getY() - 1.0D, this.getZ());
                        float f = 0.91F;
                        if (this.onGround()) {
                            f = this.level().getBlockState(ground).getBlock().getFriction() * 0.91F;
                        }

                        float f1 = 0.16277137F / (f * f * f);

                        this.moveRelative(this.onGround() ? 0.1F * f1 : 0.1F, proposedMovement);
                        this.move(MoverType.SELF, this.getDeltaMovement());
                        this.setDeltaMovement(this.getDeltaMovement().scale(f));
                    }
                } else if (livingentity instanceof Player) {
                    this.setDeltaMovement(Vec3.ZERO);
                }

                this.calculateEntityAnimation(false);
                this.applyEffectsFromBlocks();
            } else {
                if (this.isInLava()) {
                    this.moveRelative(0.02F, travelVector);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
                } else {
                    BlockPos ground = BlockPos.containing(this.getX(), this.getY() - 1.0D, this.getZ());
                    float f = 0.91F;
                    if (this.onGround()) {
                        f = this.level().getBlockState(ground).getBlock().getFriction() * 0.91F;
                    }

                    float f1 = 0.16277137F / (f * f * f);

                    this.moveRelative(this.onGround() ? 0.1F * f1 : 0.02F, travelVector);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.setDeltaMovement(this.getDeltaMovement().scale(f));
                }

                this.calculateEntityAnimation(false);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.level().isClientSide()) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || itemstack.is(END_WHALE_FOOD) && !this.isTame();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else if (this.isSaddled() && player.isSecondaryUseActive()) {
            this.removeSaddle();
            return InteractionResult.CONSUME;
        } else if (this.isSaddled()) {
            this.mountWhale(player);
            return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        } else if (!this.isTame()) {
            if (itemstack.is(END_WHALE_FOOD)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                if (this.random.nextInt(10) == 0 && !CreaturesAndBeasts.getInstance().getProxy().callAnimalTameEvent(this, player)) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void onPassengerTurned(Entity entity) {
        this.clampRotation(entity);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob endWhale) {
        return null;
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return 12 + this.getRandom().nextInt(5);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return MAX_SPAWN_CLUSTER_SIZE;
    }

    public static boolean checkEndWhaleSpawnRules(EntityType<EndWhaleEntity> animal, LevelAccessor worldIn, EntitySpawnReason reason, BlockPos pos, RandomSource randomIn) {
        return findIslandSurface(worldIn, pos) != null;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData spawnGroup = super.finalizeSpawn(level, difficulty, reason, spawnGroupData);
        BlockPos surface = findIslandSurface(level, this.blockPosition());
        if (surface != null) {
            BlockPos spawnPos = this.findSurfaceSpawnPos(level, surface);
            this.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, this.getYRot(), this.getXRot());
        }

        return spawnGroup;
    }

    @Nullable
    private static BlockPos findIslandSurface(LevelReader level, BlockPos pos) {
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        BlockPos ground = surface.below();
        BlockState groundState = level.getBlockState(ground);
        return groundState.isFaceSturdy(level, ground, Direction.UP) ? surface : null;
    }

    private BlockPos findSurfaceSpawnPos(LevelReader level, BlockPos surface) {
        int heightAboveSurface = SURFACE_SPAWN_MIN_HEIGHT_ABOVE + this.random.nextInt(SURFACE_SPAWN_RANDOM_HEIGHT + 1);
        int y = Mth.clamp(surface.getY() + heightAboveSurface, level.getMinY() + 1, level.getMaxY() - 1);
        return new BlockPos(surface.getX(), y, surface.getZ());
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 10.0F;
    }

    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double p_148750_, float p_148751_, DamageSource p_148752_) {
        return false;
    }

    @Override
    protected void checkFallDamage(double p_27754_, boolean p_27755_, BlockState p_27756_, BlockPos p_27757_) {
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(false);
        return flyingpathnavigation;
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound() {
        return CNBSoundEvents.END_WHALE_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 800;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }

    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("whale_fly");

    private <E extends GeoAnimatable> PlayState animationPredicate(AnimationTest<E> event) {
        event.controller().setAnimation(FLY_ANIMATION);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>("controller", 0, this::animationPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    static class EndWhaleMoveControl extends MoveControl {
        private final EndWhaleEntity endWhale;

        EndWhaleMoveControl(EndWhaleEntity endWhale) {
            super(endWhale);
            this.endWhale = endWhale;
        }

        @Override
        public void tick() {
            if (this.operation != Operation.MOVE_TO || this.endWhale.isVehicle()) {
                return;
            }

            Vec3 offset = new Vec3(
                    this.wantedX - this.endWhale.getX(),
                    this.wantedY - this.endWhale.getY(),
                    this.wantedZ - this.endWhale.getZ()
            );
            double distanceSqr = offset.lengthSqr();
            if (distanceSqr < FREE_FLIGHT_ARRIVAL_DISTANCE_SQR) {
                this.operation = Operation.WAIT;
                return;
            }

            double distance = Math.sqrt(distanceSqr);
            Vec3 direction = offset.scale(1.0D / distance);
            double speed = this.speedModifier * this.endWhale.getAttributeValue(Attributes.FLYING_SPEED) * FREE_FLIGHT_SPEED_SCALE;
            Vec3 targetMovement = direction.scale(speed);
            Vec3 currentMovement = this.endWhale.getDeltaMovement();
            Vec3 nextMovement = currentMovement.add(targetMovement.subtract(currentMovement).scale(FREE_FLIGHT_ACCELERATION));
            this.endWhale.setDeltaMovement(nextMovement);

            float targetYaw = (float)(Mth.atan2(offset.z, offset.x) * (180.0F / Mth.PI)) - 90.0F;
            this.endWhale.setYRot(Mth.rotLerp(FREE_FLIGHT_YAW_LERP, this.endWhale.getYRot(), targetYaw));
            this.endWhale.yBodyRot = this.endWhale.getYRot();
            this.endWhale.yHeadRot = this.endWhale.yBodyRot;

            double horizontalDistance = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
            float targetPitch = (float)(-(Mth.atan2(offset.y, horizontalDistance) * (180.0F / Mth.PI)));
            targetPitch = Mth.clamp(targetPitch, -FREE_FLIGHT_MAX_PITCH, FREE_FLIGHT_MAX_PITCH);
            this.endWhale.setXRot(Mth.rotLerp(FREE_FLIGHT_PITCH_LERP, this.endWhale.getXRot(), targetPitch));
        }
    }

    static class EndWhaleLookControl extends LookControl {
        private final EndWhaleEntity endWhale;

        public EndWhaleLookControl(EndWhaleEntity endWhale) {
            super(endWhale);
            this.endWhale = endWhale;
        }

        @Override
        public void tick() {
            if (this.endWhale.yBodyRot != this.endWhale.getYHeadRot()) {
                this.endWhale.yHeadRot = Mth.rotLerp(0.05F, this.endWhale.getYHeadRot(), this.endWhale.yBodyRot);
            }
        }
    }

    static class EndWhaleBodyRotationControl extends BodyRotationControl {
        private final EndWhaleEntity endWhale;
        private int headStableTime;
        private float lastStableYHeadRot;


        public EndWhaleBodyRotationControl(EndWhaleEntity endWhale) {
            super(endWhale);
            this.endWhale = endWhale;
        }

        @Override
        public void clientTick() {
            if (this.isMoving()) {
                this.endWhale.yBodyRot = Mth.rotLerp(0.05F, this.endWhale.yBodyRot, this.endWhale.getYRot());
                this.rotateHeadIfNecessary();
                this.lastStableYHeadRot = this.endWhale.yHeadRot;
                this.headStableTime = 0;
            } else {
                if (this.notCarryingMobPassengers()) {
                    if (Math.abs(this.endWhale.yHeadRot - this.lastStableYHeadRot) > 15.0F) {
                        this.headStableTime = 0;
                        this.lastStableYHeadRot = this.endWhale.yHeadRot;
                        this.rotateHeadIfNecessary();
                    } else {
                        ++this.headStableTime;
                        if (this.headStableTime > 10) {
                            this.rotateHeadTowardsFront();
                        }
                    }
                }
            }
        }

        private void rotateHeadIfNecessary() {
            this.endWhale.yHeadRot = Mth.rotLerp(0.05F, this.endWhale.yHeadRot, this.endWhale.yBodyRot);
        }

        private void rotateHeadTowardsFront() {
            this.endWhale.yHeadRot = Mth.rotLerp(0.05F, this.endWhale.yHeadRot, this.endWhale.yBodyRot);
        }

        private boolean notCarryingMobPassengers() {
            return !(this.endWhale.getFirstPassenger() instanceof Mob);
        }


        private boolean isMoving() {
            double d0 = this.endWhale.getX() - this.endWhale.xo;
            double d1 = this.endWhale.getZ() - this.endWhale.zo;
            return d0 * d0 + d1 * d1 > (double)2.5000003E-7F;
        }
    }

    static class EndWhaleWanderGoal extends Goal {
        private static final int DIRECT_TRAVEL_TICKS = 200;

        private final EndWhaleEntity endWhale;
        @Nullable
        private Vec3 target;
        private int travelTicks;

        EndWhaleWanderGoal(EndWhaleEntity endWhale) {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
            this.endWhale = endWhale;
        }

        @Override
        public boolean canUse() {
            if (this.endWhale.random.nextInt(3) != 0 || this.endWhale.isVehicle() || this.endWhale.isLeashed()) {
                return false;
            }

            this.target = this.findPos();
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.travelTicks > 0 && !this.endWhale.isVehicle() && !this.endWhale.isLeashed() && this.endWhale.distanceToSqr(this.target) > FREE_FLIGHT_ARRIVAL_DISTANCE_SQR;
        }

        @Override
        public void start() {
            this.travelTicks = adjustedTickDelay(DIRECT_TRAVEL_TICKS);
            this.updateWantedPosition();
        }

        @Override
        public void tick() {
            --this.travelTicks;
            if (this.travelTicks % 20 == 0) {
                this.updateWantedPosition();
            }
        }

        @Override
        public void stop() {
            this.endWhale.navigation.stop();
            this.endWhale.getMoveControl().setWantedPosition(this.endWhale.getX(), this.endWhale.getY(), this.endWhale.getZ(), 0.0D);
            this.target = null;
            this.travelTicks = 0;
        }

        private void updateWantedPosition() {
            if (this.target != null) {
                this.endWhale.getMoveControl().setWantedPosition(this.target.x, this.target.y, this.target.z, 1.0D);
            }
        }

        @Nullable
        private Vec3 findPos() {
            if (this.endWhale.random.nextInt(ISLAND_SURFACE_TARGET_CHANCE) == 0) {
                Vec3 islandSurfacePos = this.findIslandSurfacePos();
                if (islandSurfacePos != null) {
                    return islandSurfacePos;
                }
            }

            Vec3 vec3 = this.endWhale.getViewVector(0.5F);

            Vec3 vec32 = HoverRandomPos.getPos(this.endWhale, 20, 20, vec3.x, vec3.z, (float)Math.PI, 50, 15);
            vec32 = vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(this.endWhale, 20, 20, -2, vec3.x, vec3.z, ((float)Math.PI));

            if (this.endWhale.isSaddled() && vec32 != null && this.endWhale.getOwner() != null && vec32.distanceTo(this.endWhale.getOwner().position()) > 100.0D) {
                vec32 = null;
            }

            return vec32;
        }

        @Nullable
        private Vec3 findIslandSurfacePos() {
            Level level = this.endWhale.level();
            BlockPos origin = this.endWhale.blockPosition();

            for (int attempt = 0; attempt < ISLAND_SURFACE_SEARCH_ATTEMPTS; ++attempt) {
                int x = origin.getX() + this.endWhale.random.nextInt(ISLAND_SURFACE_HORIZONTAL_RADIUS * 2 + 1) - ISLAND_SURFACE_HORIZONTAL_RADIUS;
                int z = origin.getZ() + this.endWhale.random.nextInt(ISLAND_SURFACE_HORIZONTAL_RADIUS * 2 + 1) - ISLAND_SURFACE_HORIZONTAL_RADIUS;
                BlockPos column = new BlockPos(x, origin.getY(), z);
                if (!level.hasChunkAt(column)) {
                    continue;
                }

                BlockPos surface = EndWhaleEntity.findIslandSurface(level, column);
                if (surface == null) {
                    continue;
                }

                int y = surface.getY() + ISLAND_SURFACE_MIN_HEIGHT_ABOVE + this.endWhale.random.nextInt(ISLAND_SURFACE_RANDOM_HEIGHT + 1);
                y = Mth.clamp(y, level.getMinY() + 1, level.getMaxY() - 1);
                Vec3 target = new Vec3(x + 0.5D, y, z + 0.5D);
                if (this.canFitAt(target)) {
                    return target;
                }
            }

            return null;
        }

        private boolean canFitAt(Vec3 target) {
            return this.endWhale.level().noCollision(
                    this.endWhale,
                    this.endWhale.getBoundingBox().move(target.x - this.endWhale.getX(), target.y - this.endWhale.getY(), target.z - this.endWhale.getZ())
            );
        }
    }

    static class EndWhaleFollowHerdGoal extends Goal {
        private final EndWhaleEntity endWhale;
        @Nullable
        private EndWhaleEntity herdMate;

        EndWhaleFollowHerdGoal(EndWhaleEntity endWhale) {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
            this.endWhale = endWhale;
        }

        @Override
        public boolean canUse() {
            if (this.endWhale.isVehicle() || this.endWhale.isLeashed() || this.endWhale.random.nextInt(reducedTickDelay(HERD_CHECK_INTERVAL)) != 0) {
                return false;
            }

            this.herdMate = this.findHerdMate();
            return this.herdMate != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.herdMate != null
                    && this.herdMate.isAlive()
                    && !this.endWhale.isVehicle()
                    && !this.endWhale.isLeashed()
                    && this.endWhale.distanceToSqr(this.herdMate) > HERD_STOP_DISTANCE_SQR
                    && this.endWhale.distanceToSqr(this.herdMate) < HERD_SEARCH_RANGE_SQR;
        }

        @Override
        public void start() {
            this.updateWantedPosition();
        }

        @Override
        public void tick() {
            this.updateWantedPosition();
        }

        @Override
        public void stop() {
            this.endWhale.getMoveControl().setWantedPosition(this.endWhale.getX(), this.endWhale.getY(), this.endWhale.getZ(), 0.0D);
            this.herdMate = null;
        }

        @Nullable
        private EndWhaleEntity findHerdMate() {
            List<EndWhaleEntity> whales = this.endWhale.level().getEntitiesOfClass(
                    EndWhaleEntity.class,
                    this.endWhale.getBoundingBox().inflate(HERD_SEARCH_RANGE),
                    whale -> whale != this.endWhale && whale.isAlive() && !whale.isVehicle()
            );

            EndWhaleEntity closest = null;
            double closestDistance = Double.MAX_VALUE;
            for (EndWhaleEntity whale : whales) {
                double distance = this.endWhale.distanceToSqr(whale);
                if (distance > HERD_FOLLOW_DISTANCE_SQR && distance < closestDistance) {
                    closest = whale;
                    closestDistance = distance;
                }
            }

            return closest;
        }

        private void updateWantedPosition() {
            if (this.herdMate == null) {
                return;
            }

            Vec3 target = this.getLooseHerdTarget();
            this.endWhale.getMoveControl().setWantedPosition(target.x, target.y, target.z, HERD_SPEED_MODIFIER);
        }

        private Vec3 getLooseHerdTarget() {
            Vec3 offsetFromMate = this.endWhale.position().subtract(this.herdMate.position());
            if (offsetFromMate.lengthSqr() < 1.0E-6D) {
                return this.herdMate.position();
            }

            return this.herdMate.position().add(offsetFromMate.normalize().scale(HERD_STOP_DISTANCE));
        }
    }

    static class EndWhaleTemptGoal extends Goal {
        private static final TargetingConditions TEMP_TARGETING = TargetingConditions.forNonCombat().range(TEMPT_SEARCH_RANGE).ignoreLineOfSight();
        private final TargetingConditions targetingConditions;
        protected final EndWhaleEntity endWhale;
        private final double speedModifier;
        @Nullable
        protected Player player;
        private int calmDown;
        private final Ingredient items;

        public EndWhaleTemptGoal(EndWhaleEntity endWhale, double speedModifier, Ingredient temptIngredient) {
            this.endWhale = endWhale;
            this.speedModifier = speedModifier;
            this.items = temptIngredient;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
            this.targetingConditions = TEMP_TARGETING.copy().selector(this::shouldFollow);
        }

        @Override
        public boolean canUse() {
            if (this.calmDown > 0) {
                --this.calmDown;
                return false;
            } else {
                if (!(this.endWhale.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }
                this.player = serverLevel.getNearestPlayer(this.targetingConditions, this.endWhale);
                return this.player != null && !this.endWhale.isVehicle();
            }
        }

        private boolean shouldFollow(LivingEntity entity, ServerLevel level) {
            return this.isHoldingFood(entity);
        }

        private boolean isHoldingFood(LivingEntity entity) {
            return this.items.test(entity.getMainHandItem()) || this.items.test(entity.getOffhandItem());
        }

        @Override
        public boolean canContinueToUse() {
            return this.player != null && this.player.isAlive() && !this.endWhale.isVehicle() && this.endWhale.distanceToSqr(this.player) <= TEMPT_SEARCH_RANGE_SQR && this.isHoldingFood(this.player);
        }

        @Override
        public void stop() {
            this.player = null;
            this.endWhale.getNavigation().stop();
            this.endWhale.getMoveControl().setWantedPosition(this.endWhale.getX(), this.endWhale.getY(), this.endWhale.getZ(), 0.0D);
            this.calmDown = reducedTickDelay(100);
        }

        @Override
        public void tick() {
            if (this.player == null) {
                return;
            }

            this.endWhale.getLookControl().setLookAt(this.player, (float)(this.endWhale.getMaxHeadYRot() + 20), (float)this.endWhale.getMaxHeadXRot());
            if (this.endWhale.distanceToSqr(this.player) < TEMPT_STOP_DISTANCE_SQR) {
                this.endWhale.getNavigation().stop();
                this.endWhale.getMoveControl().setWantedPosition(this.endWhale.getX(), this.endWhale.getY(), this.endWhale.getZ(), 0.0D);
                this.endWhale.setDeltaMovement(this.endWhale.getDeltaMovement().scale(0.9D));
            } else {
                Vec3 target = this.getTemptTarget();
                this.endWhale.getMoveControl().setWantedPosition(target.x, target.y, target.z, this.speedModifier);
            }
        }

        private Vec3 getTemptTarget() {
            Vec3 playerTarget = this.player.getEyePosition().add(0.0D, TEMPT_VERTICAL_OFFSET, 0.0D);
            Vec3 toPlayer = playerTarget.subtract(this.endWhale.position());
            if (toPlayer.lengthSqr() < 1.0E-6D) {
                return playerTarget;
            }

            return playerTarget.subtract(toPlayer.normalize().scale(TEMPT_STOP_DISTANCE));
        }
    }
}
