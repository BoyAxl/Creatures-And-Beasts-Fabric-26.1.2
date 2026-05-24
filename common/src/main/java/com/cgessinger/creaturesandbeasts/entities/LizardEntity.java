package com.cgessinger.creaturesandbeasts.entities;

import com.cgessinger.creaturesandbeasts.blocks.LizardEggBlock;
import com.cgessinger.creaturesandbeasts.init.CNBBlocks;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import com.cgessinger.creaturesandbeasts.init.CNBLizardTypes;
import com.cgessinger.creaturesandbeasts.util.LizardType;
import com.cgessinger.creaturesandbeasts.util.Netable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

public class LizardEntity extends Animal implements GeoEntity, Netable {
    private static final double PANIC_SPEED_MODIFIER = 1.8D;
    private static final int BREEDING_COOLDOWN_TICKS = 6000;

    private static final EntityDataAccessor<String> TYPE = SynchedEntityData.defineId(LizardEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> PARTYING = SynchedEntityData.defineId(LizardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SAD = SynchedEntityData.defineId(LizardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(LizardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(LizardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROM_NET = SynchedEntityData.defineId(LizardEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);
    private LizardEntity partner;

    public BlockPos jukeboxPosition;
    int layEggCounter;

    public LizardEntity(EntityType<LizardEntity> type, Level worldIn) {
        super(type, worldIn);

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                LizardEntity lizard = (LizardEntity) this.mob;
                if (lizard.shouldLookAround()) {
                    super.tick();
                }
            }
        };

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, CNBLizardTypes.DESERT.getId().toString());
        builder.define(HAS_EGG, false);
        builder.define(LAYING_EGG, false);
        builder.define(FROM_NET, false);
        builder.define(PARTYING, false);
        builder.define(SAD, false);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("LizardType", this.getLizardType().getId().toString());
        output.putBoolean("Sad", this.getSad());
        output.putBoolean("FromNet", this.fromNet());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        LizardType type = LizardType.getById(input.getStringOr("LizardType", CNBLizardTypes.DESERT.getId().toString()));
        if (type == null) {
            type = CNBLizardTypes.DESERT;
        }
        this.setLizardType(type);
        setSad(input.getBooleanOr("Sad", false));
        setFromNet(input.getBooleanOr("FromNet", false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, PANIC_SPEED_MODIFIER));
        this.goalSelector.addGoal(2, new LizardBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LizardLayEggGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0D, this::isFood, false) {
            @Override
            public boolean canUse() {
                return LizardEntity.this.shouldLookAround() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return LizardEntity.this.shouldLookAround() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !((LizardEntity) this.mob).isPartying() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !((LizardEntity) this.mob).isPartying() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }


    @Override
    public void aiStep() {
        super.aiStep();

        if (this.jukeboxPosition != null) {
            BlockEntity te = this.level().getBlockEntity(this.jukeboxPosition);
            Vec3 pos = this.position();
            if (!this.jukeboxPosition.closerThan(BlockPos.containing(pos), 10.0D) || !(te instanceof JukeboxBlockEntity)) {
                this.setPartying(false, null);
            }
        }

        if (this.isPartying() || this.entityData.get(LAYING_EGG)) {
            this.navigation.stop();
        }

        if (this.isAlive() && this.isLayingEgg() && this.layEggCounter >= 1 && this.layEggCounter % 5 == 0) {
            BlockPos blockpos = this.blockPosition().below();
            this.level().levelEvent(2001, blockpos, Block.getId(this.level().getBlockState(blockpos)));
        }
    }

    public static boolean checkLizardSpawnRules(EntityType<LizardEntity> animal, LevelAccessor worldIn, EntitySpawnReason reason, BlockPos pos, RandomSource randomIn) {
        return worldIn.getRawBrightness(pos, 0) > 8;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, EntitySpawnReason reason, @Nullable SpawnGroupData spawnDataIn) {
        Holder<Biome> biome = worldIn.getBiome(this.blockPosition());

        if (biome.is(Biomes.DESERT) || biome.is(BiomeTags.IS_BADLANDS)) {
            if (random.nextBoolean()) {
                this.setLizardType(CNBLizardTypes.DESERT);
            } else {
                this.setLizardType(CNBLizardTypes.DESERT_2);
            }
        } else if (biome.is(BiomeTags.IS_JUNGLE)) {
            if (random.nextBoolean()) {
                this.setLizardType(CNBLizardTypes.JUNGLE);
            } else {
                this.setLizardType(CNBLizardTypes.JUNGLE_2);
            }
        }  else if (biome.is(Biomes.MUSHROOM_FIELDS)) {
            this.setLizardType(CNBLizardTypes.MUSHROOM);
        } else {
            switch (random.nextInt(4)) {
                case 0 -> this.setLizardType(CNBLizardTypes.DESERT);
                case 1 -> this.setLizardType(CNBLizardTypes.DESERT_2);
                case 2 -> this.setLizardType(CNBLizardTypes.JUNGLE);
                default -> this.setLizardType(CNBLizardTypes.JUNGLE_2);
            }
        }

        this.setSad(this.getRandom().nextInt(10) == 0);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
    }

    @Override
    public void setRecordPlayingNearby(BlockPos pos, boolean isPartying) {
        this.setPartying(isPartying, pos);
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource damageSrc, float damageAmount) {
        super.actuallyHurt(level, damageSrc, damageAmount);
        this.setPartying(false, null);
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (item.is(CNBItems.APPLE_SLICE.get()) && this.getSad()) {
            this.setSad(false);
            this.usePlayerItem(player, hand, item);
            spawnParticles(ParticleTypes.HEART);
            return InteractionResult.SUCCESS;
        }

        return Netable.netMobPickup(player, hand, this).orElse(super.mobInteract(player, hand));
    }

    @Override
    public boolean fromNet() {
        return this.entityData.get(FROM_NET);
    }

    @Override
    public void setFromNet(boolean fromNet) {
        this.entityData.set(FROM_NET, fromNet);
    }

    @Override
    public void saveToNetTag(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (this.hasCustomName()) {
            stack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        }
        if (this.isNoAi()) {
            tag.putBoolean("NoAI", this.isNoAi());
        }

        if (this.isSilent()) {
            tag.putBoolean("Silent", this.isSilent());
        }

        if (this.isNoGravity()) {
            tag.putBoolean("NoGravity", this.isNoGravity());
        }

        if (this.hasGlowingTag()) {
            tag.putBoolean("Glowing", this.hasGlowingTag());
        }

        if (this.isInvulnerable()) {
            tag.putBoolean("Invulnerable", this.isInvulnerable());
        }

        tag.putFloat("Health", this.getHealth());
        tag.putBoolean("Sad", this.getSad());
        tag.putBoolean("FromNet", true);
        tag.putString("LizardType", this.getLizardType().getId().toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void loadFromNetTag(CompoundTag compound) {
        if (compound.contains("NoAI")) {
            this.setNoAi(compound.getBooleanOr("NoAI", false));
        }

        if (compound.contains("Silent")) {
            this.setSilent(compound.getBooleanOr("Silent", false));
        }

        if (compound.contains("NoGravity")) {
            this.setNoGravity(compound.getBooleanOr("NoGravity", false));
        }

        if (compound.contains("Glowing")) {
            this.setGlowingTag(compound.getBooleanOr("Glowing", false));
        }

        if (compound.contains("Invulnerable")) {
            this.setInvulnerable(compound.getBooleanOr("Invulnerable", false));
        }

        if (compound.contains("Health")) {
            this.setHealth(compound.getFloatOr("Health", this.getHealth()));
        }

        if (compound.contains("Sad")) {
            this.setSad(compound.getBooleanOr("Sad", false));
        }

        if (compound.contains("LizardType")) {
            LizardType type = LizardType.getById(compound.getStringOr("LizardType", CNBLizardTypes.DESERT.getId().toString()));
            if (type != null) {
                this.setLizardType(type);
            }
        }

        if (compound.contains("FromNet")) {
            this.setFromNet(compound.getBooleanOr("FromNet", false));
        }
    }

    @Override
    public ItemStack getItemStack() {
        if (!this.isBaby()) {
            return new ItemStack(this.getLizardType().getSpawnItem());
        }
        return null;
    }

    @Override
    public SoundEvent getPickupSound() {
        return SoundEvents.ITEM_PICKUP;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob entity) {
        LizardEntity baby = CNBEntityTypes.LIZARD.get().create(world, EntitySpawnReason.BREEDING);
        if (baby != null) {
            baby.setLizardType(((LizardEntity) entity).getLizardType());
        }
        return baby;
    }

    public void setPartying(boolean isPartying, @Nullable BlockPos jukeboxPos) {
        if (!this.getSad()) {
            this.entityData.set(PARTYING, isPartying);
            this.jukeboxPosition = jukeboxPos;
        }
    }

    public boolean isPartying() {
        return this.entityData.get(PARTYING);
    }

    public void setSad(boolean sad) {
        this.entityData.set(SAD, sad);
    }

    public boolean getSad() {
        return this.entityData.get(SAD);
    }

    public boolean hasEgg() {
        return this.entityData.get(HAS_EGG);
    }

    void setHasEgg(boolean hasEgg) {
        this.entityData.set(HAS_EGG, hasEgg);
    }

    public boolean isLayingEgg() {
        return this.entityData.get(LAYING_EGG);
    }

    void setLayingEgg(boolean layingEgg) {
        this.layEggCounter = layingEgg ? 1 : 0;
        this.entityData.set(LAYING_EGG, layingEgg);
    }

    public boolean shouldLookAround() {
        return !this.isPartying() && !this.entityData.get(LAYING_EGG);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CNBItems.APPLE_SLICE.get());
    }

    public void spawnParticles(ParticleOptions data) {
        for (int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(data, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
        }
    }

    public void setLizardType(LizardType lizardType) {
        this.entityData.set(TYPE, lizardType.getId().toString());
    }

    public LizardType getLizardType() {
        return LizardType.getById(this.entityData.get(TYPE));
    }

    @Override
    public int getMaxHeadYRot() {
        return 50;
    }

    @Override
    public int getMaxHeadXRot() {
        return 35;
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return new ItemStack(this.getLizardType().getSpawnItem());
    }

    private static final RawAnimation DIG_ANIMATION = RawAnimation.begin().thenLoop("lizard_dig");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("lizard_walk");
    private static final RawAnimation DANCE_ANIMATION = RawAnimation.begin().thenLoop("lizard_dance");

    private PlayState animationPredicate(AnimationTest<LizardEntity> event) {
        double xMovement = this.getX() - this.xo;
        double zMovement = this.getZ() - this.zo;
        boolean isMoving = xMovement * xMovement + zMovement * zMovement > 1.0E-4D;

        if (this.entityData.get(LAYING_EGG)) {
            event.controller().setAnimation(DIG_ANIMATION);
            return PlayState.CONTINUE;
        } else if (isMoving) {
            event.controller().setAnimation(WALK_ANIMATION);
            return PlayState.CONTINUE;
        } else if (this.isPartying()) {
            event.controller().setAnimation(DANCE_ANIMATION);
            return PlayState.CONTINUE;
        }

        event.controller().reset();
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar animationData) {
        animationData.add(new AnimationController<LizardEntity>("controller", 0, this::animationPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
    }

    static class LizardBreedGoal extends BreedGoal {
        private final LizardEntity lizard;

        LizardBreedGoal(LizardEntity lizard, double speedModifier) {
            super(lizard, speedModifier);
            this.lizard = lizard;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.lizard.hasEgg();
        }

        @Override
        protected void breed() {
            ServerPlayer serverplayer = this.animal.getLoveCause();
            if (serverplayer == null && this.partner.getLoveCause() != null) {
                serverplayer = this.partner.getLoveCause();
            }

            if (serverplayer != null) {
                serverplayer.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(serverplayer, this.animal, this.partner, null);
            }

            this.lizard.setHasEgg(true);
            this.lizard.partner = (LizardEntity) this.partner;
            this.animal.resetLove();
            this.partner.resetLove();
            this.animal.setAge(BREEDING_COOLDOWN_TICKS);
            this.partner.setAge(BREEDING_COOLDOWN_TICKS);
            RandomSource random = this.animal.getRandom();
            if (this.level.getGameRules().get(GameRules.MOB_DROPS)) {
                this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), random.nextInt(7) + 1));
            }

        }
    }

    static class LizardLayEggGoal extends MoveToBlockGoal {
        private final LizardEntity lizard;

        LizardLayEggGoal(LizardEntity lizard, double speedModifier) {
            super(lizard, speedModifier, 16);
            this.lizard = lizard;
        }

        @Override
        public boolean canUse() {
            return this.lizard.hasEgg() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.lizard.hasEgg();
        }

        @Override
        public void stop() {
            super.stop();
            this.lizard.setLayingEgg(false);
        }

        @Override
        public void tick() {
            super.tick();
            BlockPos blockpos = this.lizard.blockPosition();
            if (!this.lizard.isInWater() && this.isReachedTarget()) {
                if (this.lizard.layEggCounter < 1) {
                    this.lizard.setLayingEgg(true);
                } else if (this.lizard.layEggCounter > this.adjustedTickDelay(200)) {
                    Level level = this.lizard.level();
                    BlockPos eggPos = this.getEggPlacementPos(level, this.blockPos);
                    if (eggPos == null) {
                        this.lizard.setLayingEgg(false);
                        return;
                    }

                    level.playSound(null, blockpos, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.getRandom().nextFloat() * 0.2F);
                    BlockState eggState = CNBBlocks.LIZARD_EGGS.get().defaultBlockState().setValue(LizardEggBlock.EGGS, this.lizard.random.nextInt(6) + 1);
                    if (!level.setBlock(eggPos, eggState, 3)) {
                        this.lizard.setLayingEgg(false);
                        return;
                    }

                    if (level.getBlockState(eggPos).getBlock() instanceof LizardEggBlock lizardEggBlock) {
                        lizardEggBlock.setParents(this.lizard.getLizardType(), this.lizard.partner.getLizardType());
                    }

                    this.lizard.setHasEgg(false);
                    this.lizard.setLayingEgg(false);
                }

                if (this.lizard.isLayingEgg()) {
                    ++this.lizard.layEggCounter;
                }
            } else if (!this.isReachedTarget()) {
                this.lizard.setLayingEgg(false);
                this.moveMobToBlock();
            }
        }

        @Override
        protected boolean isValidTarget(LevelReader levelReader, BlockPos pos) {
            return this.getEggPlacementPos(levelReader, pos) != null;
        }

        @Nullable
        private BlockPos getEggPlacementPos(LevelReader levelReader, BlockPos surfacePos) {
            BlockPos eggPos = surfacePos.above();
            BlockState replacedState = levelReader.getBlockState(eggPos);
            BlockState eggState = CNBBlocks.LIZARD_EGGS.get().defaultBlockState();
            return canReplaceWithEgg(replacedState) && eggState.canSurvive(levelReader, eggPos) ? eggPos : null;
        }

        private boolean canReplaceWithEgg(BlockState state) {
            return state.isAir() || state.canBeReplaced();
        }
    }
}
