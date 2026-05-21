package com.cgessinger.creaturesandbeasts.entities;

import com.cgessinger.creaturesandbeasts.containers.CinderFurnaceContainer;
import com.cgessinger.creaturesandbeasts.init.CNBBlocks;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import com.cgessinger.creaturesandbeasts.init.CNBSoundEvents;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
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
import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.animation.object.PlayState;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import com.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.cgessinger.creaturesandbeasts.init.CNBTags.Items.CINDERSHELL_FOOD;

public class CindershellEntity extends Animal implements GeoEntity, Bucketable, ContainerListener, Container, StackedContentsCompatible, MenuProvider, RecipeCraftingHolder {
    private static final EntityDataAccessor<Boolean> EATING = SynchedEntityData.defineId(CindershellEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(CindershellEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FURNACE = SynchedEntityData.defineId(CindershellEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> PLAYER = SynchedEntityData.defineId(CindershellEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);

    private static final Identifier HEALTH_REDUCTION_ID = Identifier.fromNamespaceAndPath(com.cgessinger.creaturesandbeasts.CreaturesAndBeasts.MOD_ID, "cindershell_health_reduction");
    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);
    protected CinderFurnaceContainer inventory;
    private Player playerInMenu;
    private int eatTimer;

    int cookingProgress;
    int cookingTotalTime;
    protected NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int index) {
            switch(index) {
                case 0:
                    return CindershellEntity.this.cookingProgress;
                case 1:
                    return CindershellEntity.this.cookingTotalTime;
                default:
                    return 0;
            }
        }

        public void set(int index, int value) {
            switch(index) {
                case 0:
                    CindershellEntity.this.cookingProgress = value;
                    break;
                case 1:
                    CindershellEntity.this.cookingTotalTime = value;
            }

        }

        public int getCount() {
            return 2;
        }
    };
    private final Object2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Object2IntOpenHashMap<>();

    public CindershellEntity(EntityType<CindershellEntity> type, Level worldIn) {
        super(type, worldIn);
        this.eatTimer = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.1D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 100D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(EATING, false);
        builder.define(FROM_BUCKET, false);
        builder.define(FURNACE, false);
        builder.define(PLAYER, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("FromBucket", this.fromBucket());
        output.putBoolean("HasFurnace", this.hasFurnace());
        if (this.hasFurnace()) {
            ValueOutput.ValueOutputList list = output.childrenList("Items");

            for(int i = 0; i < this.items.size(); i++) {
                ItemStack itemstack = this.items.get(i);
                if (!itemstack.isEmpty()) {
                    ValueOutput itemOutput = list.addChild();
                    itemOutput.putByte("Slot", (byte)i);
                    itemOutput.store("Item", ItemStack.OPTIONAL_CODEC, itemstack);
                }
            }

            this.entityData.get(PLAYER).ifPresent(player -> output.store("Player", UUIDUtil.CODEC, player.getUUID()));
            output.putInt("CookTime", this.cookingProgress);
            output.putInt("CookTimeTotal", this.cookingTotalTime);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        this.setFromBucket(input.getBooleanOr("FromBucket", false));
        UUID playerUUID = input.read("Player", UUIDUtil.CODEC).orElse(null);
        this.setFurnace(input.getBooleanOr("HasFurnace", false), playerUUID);
        if (this.hasFurnace()) {
            for(ValueInput itemInput : input.childrenListOrEmpty("Items")) {
                int j = itemInput.getByteOr("Slot", (byte)-1) & 255;
                if (j < this.items.size()) {
                    itemInput.read("Item", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> this.setItem(j, stack));
                }
            }

            this.cookingProgress = input.getIntOr("CookTime", 0);
            this.cookingTotalTime = input.getIntOr("CookTimeTotal", 0);
        }

        super.readAdditionalSaveData(input);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new CindershellFloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
        this.goalSelector.addGoal(2, new CindershellBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, Ingredient.of(this.registryAccess().lookupOrThrow(Registries.ITEM).getOrThrow(CINDERSHELL_FOOD)), false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.getEating()) {
            this.navigation.stop();
            this.eatTimer--;
        }

        if (this.eatTimer == 10) {
            this.setHolding(ItemStack.EMPTY);
        } else if (this.eatTimer == 0) {
            this.setEating(false);
        }
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 10.0F;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.hasFurnace() && this.random.nextDouble() <= 0.25) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX() + (this.random.nextDouble() * 0.5D - 0.25), this.getY() + 2.5 + (this.random.nextDouble() * 0.1D - 0.05), this.getZ() + (this.random.nextDouble() * 0.5D - 0.25), this.getDeltaMovement().x, 0, this.getDeltaMovement().z);
        }

        if (this.level() instanceof ServerLevel serverLevel && this.hasFurnace()) {
            if (!this.items.get(0).isEmpty()) {
                RecipeHolder<SmeltingRecipe> recipe = serverLevel.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(this.items.get(0)), serverLevel).orElse(null);

                if (this.canBurn(recipe, this.inventory.getItems(), 64)) {
                    if (this.random.nextDouble() < 0.1D) {
                        this.playSound(SoundEvents.FURNACE_FIRE_CRACKLE, 1.0F, 1.0F);
                    }
                    ++this.cookingProgress;
                    if (this.cookingProgress >= this.cookingTotalTime) {
                        this.cookingProgress = 0;
                        this.cookingTotalTime = getTotalCookTime(this.level(), RecipeType.SMELTING, this);
                        if (this.smelt(recipe, this.items, 64)) {
                            this.setRecipeUsed(recipe);
                        }
                    }
                } else {
                    this.cookingProgress = 0;
                }
            }
        }
    }

    @Override
    public boolean canUsePortal(boolean allowVehicles) {
        return !this.hasFurnace() && super.canUsePortal(allowVehicles);
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    public static boolean checkCindershellSpawnRules(EntityType<CindershellEntity> entity, LevelAccessor level, EntitySpawnReason mobSpawnType, BlockPos pos, RandomSource random) {
        return pos.getY() <= 50;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, EntitySpawnReason reason, SpawnGroupData spawnDataIn) {
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CINDERSHELL_FOOD);
    }

    public InteractionResult tryStartEat(Player player, ItemStack stack) {
        if (stack.is(CINDERSHELL_FOOD)) {
            int i = this.getAge();
            if (!this.level().isClientSide() && i == 0 && this.canFallInLove()) {
                this.usePlayerItem(player, player.getUsedItemHand(), stack);
                this.setEating(true);
                this.setInLove(player);
                this.playSound(CNBSoundEvents.CINDERSHELL_ADULT_EAT.get(), 1.2F, 1F);
                this.setHolding(stack);
                return InteractionResult.SUCCESS;
            }

            if (this.isBaby()) {
                this.playSound(CNBSoundEvents.CINDERSHELL_BABY_EAT.get(), 1.3F, 1F);
                this.usePlayerItem(player, player.getUsedItemHand(), stack);
                this.ageUp((int) (-i / 20F * 0.1F), true);
                return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }

            if (this.level().isClientSide()) {
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (item.is(Items.LAVA_BUCKET) && this.isAlive() && this.isBaby()) {
            this.playSound(this.getPickupSound(), 1.0F, 1.0F);
            ItemStack bucketItem = this.getBucketItemStack();
            this.saveToBucketTag(bucketItem);
            ItemStack bucketWithData = ItemUtils.createFilledResult(item, player, bucketItem, false);
            player.setItemInHand(hand, bucketWithData);
            Level level = this.level();

            if (!level.isClientSide()) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, bucketItem);
            }

            this.discard();
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        } else if (!this.isBaby() && !this.hasFurnace() && item.is(CNBItems.CINDERSHELL_FURNACE.get())) {
            this.setFurnace(true, player.getUUID());

            this.inventory = this.createMenu(this.getId(), player.getInventory(), player);

            if (!player.getAbilities().instabuild) {
                item.shrink(1);
            }

            this.playSound(SoundEvents.HORSE_SADDLE.value(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        } else if (this.isFood(item) && !this.getEating()) {
            return this.tryStartEat(player, item);
        } else if (this.hasFurnace() && player.isSecondaryUseActive()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                this.dropEquipment(serverLevel);
            }
            return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        } else if (this.hasFurnace()) {
            if (!this.level().isClientSide()) {
                player.openMenu(this);
            }

            return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        } else {
            return InteractionResult.PASS;
        }
    }

    public CinderFurnaceContainer createMenu(int id, Inventory playerInventory, Player player) {
        this.playerInMenu = player;
        return new CinderFurnaceContainer(id, playerInventory, this, this.dataAccess);
    }

    @Override
    protected void dropEquipment(ServerLevel level) {
        super.dropEquipment(level);
        if (this.hasFurnace()) {
            this.playSound(SoundEvents.HORSE_SADDLE.value(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.8F);

            if (!this.level().isClientSide()) {
                this.spawnAtLocation(level, CNBBlocks.CINDER_FURNACE.get());
                for (ItemStack stack : this.items) {
                    if (!stack.isEmpty()) {
                        this.spawnAtLocation(level, stack.copy());
                    }
                }
                if (this.inventory != null) {
                    ((CinderFurnaceContainer.CinderFurnaceResultSlot)this.inventory.getSlot(1)).checkTakeAchievements(this.inventory.getSlot(1).getItem());
                }
                this.clearContent();
            }

            this.setFurnace(false, null);
        }
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack itemstack = this.getItem(slot);
        boolean flag = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemstack);
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        if (slot == 0 && !flag) {
            this.dataAccess.set(1, getTotalCookTime(this.level(), RecipeType.SMELTING, this));
            this.dataAccess.set(0, 0);
            this.setChanged();
        }

    }

    @Override
    public void setChanged() {
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.items.size(); i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        if (this.inventory != null) {
            this.inventory.clearCraftingContent();
        }
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public static int getTotalCookTime(Level level, RecipeType<SmeltingRecipe> recipeType, CindershellEntity container) {
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, container.level().dimension().identifier());
        float cookTimeMultiplier = dimensionKey.equals(Level.NETHER) ? 1.0F : 1.667F;
        if (level instanceof ServerLevel serverLevel) {
            return (int) (serverLevel.recipeAccess().getRecipeFor(recipeType, new SingleRecipeInput(container.getItem(0)), serverLevel).map(holder -> holder.value().cookingTime()).orElse(200) * cookTimeMultiplier);
        }
        return (int) (200 * cookTimeMultiplier);
    }

    private boolean smelt(@Nullable RecipeHolder<? extends AbstractCookingRecipe> recipe, NonNullList<ItemStack> stack, int amount) {
        if (recipe != null && this.canBurn(recipe, stack, amount)) {
            ItemStack itemstack = stack.get(0);
            ItemStack itemstack1 = recipe.value().assemble(new SingleRecipeInput(itemstack));
            ItemStack itemstack2 = stack.get(1);
            if (itemstack2.isEmpty()) {
                stack.set(1, itemstack1.copy());
            } else if (itemstack2.is(itemstack1.getItem())) {
                itemstack2.grow(itemstack1.getCount());
            }

            itemstack.shrink(1);
            return true;
        } else {
            return false;
        }
    }

    private boolean canBurn(@Nullable RecipeHolder<? extends AbstractCookingRecipe> recipe, NonNullList<ItemStack> items, int maxStack) {
        if (!items.get(0).isEmpty() && recipe != null) {
            ItemStack itemstack = recipe.value().assemble(new SingleRecipeInput(items.get(0)));
            if (itemstack.isEmpty()) {
                return false;
            } else {
                ItemStack itemstack1 = items.get(1);
                if (itemstack1.isEmpty()) {
                    return true;
                } else if (!ItemStack.isSameItem(itemstack1, itemstack)) {
                    return false;
                } else if (itemstack1.getCount() + itemstack.getCount() <= maxStack && itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) {
                    return true;
                } else {
                    return itemstack1.getCount() + itemstack.getCount() <= itemstack.getMaxStackSize();
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
        if (recipe != null) {
            this.recipesUsed.addTo(recipe.id(), 1);
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
        List<RecipeHolder<?>> list = this.getRecipesToAwardAndPopExperience((ServerLevel) player.level(), player.position());
        player.awardRecipes(list);
        this.recipesUsed.clear();
    }

    public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel level, Vec3 vec3) {
        List<RecipeHolder<?>> list = Lists.newArrayList();

        for(Object2IntMap.Entry<ResourceKey<Recipe<?>>> entry : this.recipesUsed.object2IntEntrySet()) {
            level.recipeAccess().byKey(entry.getKey()).ifPresent((recipe) -> {
                list.add(recipe);
                if (recipe.value() instanceof AbstractCookingRecipe cookingRecipe) {
                    createExperience(level, vec3, entry.getIntValue(), cookingRecipe.experience());
                }
            });
        }

        return list;
    }

    private static void createExperience(ServerLevel level, Vec3 vec3, int value, float experience) {
        int i = Mth.floor((float)value * experience);
        float f = Mth.frac((float)value * experience);
        if (f != 0.0F && Math.random() < (double)f) {
            ++i;
        }

        ExperienceOrb.award(level, vec3, i);
    }

    @Override
    public void fillStackedContents(StackedItemContents stackedContents) {
        for(ItemStack itemstack : this.items) {
            stackedContents.accountStack(itemstack);
        }
    }

    @Override
    public boolean fromBucket() {
        return this.entityData.get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
        this.entityData.set(FROM_BUCKET, fromBucket);
    }

    @Override
    public void saveToBucketTag(ItemStack stack) {
        Bucketable.saveDefaultDataToBucketTag(this, stack);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, stack, tag -> tag.putInt("Age", this.getAge()));
    }

    @Override
    public void loadFromBucketTag(CompoundTag compound) {
        Bucketable.loadDefaultDataFromBucketTag(this, compound);

        if (compound.contains("Age")) {
            this.setAge(compound.getIntOr("Age", -24000));
        } else {
            this.setAge(-24000);
        }
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(CNBItems.CINDERSHELL_BUCKET.get());
    }

    @Override
    public SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_LAVA;
    }

    @Override
    public void slotChanged(AbstractContainerMenu menu, int slot, ItemStack stack) {

    }

    @Override
    public void dataChanged(AbstractContainerMenu menu, int dataSlotIndex, int value) {

    }

    @Override
    public void setAge(int age) {
        super.setAge(age);
        double MAX_HEALTH = this.getAttribute(Attributes.MAX_HEALTH).getValue();
        float babyHealth = 10.0F;
        if (isBaby() && MAX_HEALTH > babyHealth) {
            this.getAttribute(Attributes.MAX_HEALTH).addOrUpdateTransientModifier(new AttributeModifier(HEALTH_REDUCTION_ID, babyHealth - MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE));
            this.setHealth(babyHealth);
        }
    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        this.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_REDUCTION_ID);
        this.setHealth((float) this.getAttribute(Attributes.MAX_HEALTH).getValue());
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 23 && !this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.55F : 1.0F;
    }

    public ItemStack getHolding() {
        return this.getItemBySlot(EquipmentSlot.MAINHAND);
    }

    public void setHolding(ItemStack stack) {
        this.setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    public void setEating(boolean isEating) {
        this.eatTimer = isEating ? 40 : 0;
        this.entityData.set(EATING, isEating);
    }

    public boolean getEating() {
        return this.entityData.get(EATING);
    }

    public boolean hasFurnace() {
        return this.entityData.get(FURNACE);
    }

    public void setFurnace(boolean hasFurnace, @Nullable UUID playerUUID) {
        this.entityData.set(FURNACE, hasFurnace);
        if (playerUUID != null) {
            this.entityData.set(PLAYER, Optional.of(EntityReference.of(playerUUID)));
        } else {
            this.entityData.set(PLAYER, Optional.empty());
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return CNBEntityTypes.CINDERSHELL.get().create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean causeFallDamage(double distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return CNBSoundEvents.CINDERSHELL_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return CNBSoundEvents.CINDERSHELL_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return CNBSoundEvents.CINDERSHELL_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return super.getSoundVolume() * 2;
    }

    @Override
    public int getMaxHeadYRot() {
        return 50;
    }

    @Override
    public int getMaxHeadXRot() {
        return 25;
    }

    public static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("cindershell_idle");
    public static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("cindershell_walk");
    public static final RawAnimation BABY_WALK_ANIMATION = RawAnimation.begin().thenLoop("baby_cindershell_walk");
    public static final RawAnimation DEATH_ANIMATION = RawAnimation.begin().thenPlayAndHold("cindershell_death");
    public static final RawAnimation IDLE_EAT_ANIMATION = RawAnimation.begin().thenLoop("cindershell_idle_eat");
    public static final RawAnimation EAT_ANIMATION = RawAnimation.begin().thenLoop("cindershell_eat");

    private <E extends GeoAnimatable> PlayState animationPredicate(AnimationTest<E> event) {
        double xMovement = this.getX() - this.xo;
        double zMovement = this.getZ() - this.zo;

        if (xMovement * xMovement + zMovement * zMovement > 1.0E-6D) {
            event.controller().setAnimation(this.isBaby() ? BABY_WALK_ANIMATION : WALK_ANIMATION);
        } else if (this.getEating()) {
            event.controller().setAnimation(IDLE_EAT_ANIMATION);
        } else if (this.isDeadOrDying()) {
            event.controller().setAnimation(DEATH_ANIMATION);
        } else {
            event.controller().setAnimation(IDLE_ANIMATION);
        }
        return PlayState.CONTINUE;
    }

    private <E extends GeoAnimatable> PlayState eatAnimationPredicate(AnimationTest<E> event) {
        if (this.getEating()) {
            event.controller().setAnimation(EAT_ANIMATION);
            return PlayState.CONTINUE;
        }
        event.controller().reset();
        return PlayState.STOP;
    }

    private <E extends GeoAnimatable> void soundListener(KeyFrameEvent<E, SoundKeyframeData> event) {
        LocalPlayer player = Minecraft.getInstance().player;
        player.playSound(this.isBaby() ? CNBSoundEvents.CINDERSHELL_BABY_EAT.get() : CNBSoundEvents.CINDERSHELL_ADULT_EAT.get(), 0.4F, 1F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar animationData) {
        AnimationController<CindershellEntity> controller = new AnimationController<>("controller", 0, this::animationPredicate);
        AnimationController<CindershellEntity> eatController = new AnimationController<>("eatController", 0, this::eatAnimationPredicate);

        eatController.setSoundKeyframeHandler(this::soundListener);

        animationData.add(controller);
        animationData.add(eatController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean hitByPlayer) {
        super.dropCustomDeathLoot(level, damageSource, hitByPlayer);

        if (this.getRandom().nextInt(73) < 5) {
            this.spawnAtLocation(level, new ItemStack(CNBItems.CINDERSHELL_SHELL_SHARD.get(), this.getRandom().nextInt(3) + 1));
        }
    }

    static class CindershellFloatGoal extends FloatGoal {
        private final CindershellEntity cindershell;

        public CindershellFloatGoal(CindershellEntity cindershell) {
            super(cindershell);
            this.cindershell = cindershell;
        }

        @Override
        public boolean canUse() {
            return this.cindershell.isInLava();
        }
    }

    static class CindershellBreedGoal extends BreedGoal {

        public CindershellBreedGoal(Animal cindershell, double speedModifier) {
            super(cindershell, speedModifier);
        }

        @Override
        protected void breed() {
            int range = this.animal.getRandom().nextInt(4) + 3;
            for (int i = 0; i <= range; i++) {
                super.breed();
            }
        }
    }
}
