package com.cgessinger.creaturesandbeasts.items;

import com.cgessinger.creaturesandbeasts.entities.LizardEntity;
import com.cgessinger.creaturesandbeasts.util.LizardType;
import com.cgessinger.creaturesandbeasts.util.CNBRegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Objects;

public class LizardItem extends SpawnEggItem {
    private final LizardType type;

    public LizardItem(CNBRegistrySupplier<? extends EntityType<? extends Mob>> entityTypeSupplier, Properties properties, LizardType type) {
        super(properties.spawnEgg(entityTypeSupplier.get()));
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            ItemStack itemstack = context.getItemInHand();
            BlockPos blockpos = context.getClickedPos();
            Direction direction = context.getClickedFace();
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(Blocks.SPAWNER)) {
                BlockEntity blockentity = level.getBlockEntity(blockpos);
                if (blockentity instanceof SpawnerBlockEntity spawner) {
                    EntityType<?> entitytype1 = SpawnEggItem.getType(itemstack);
                    spawner.setEntityId(entitytype1, level.getRandom());
                    blockentity.setChanged();
                    level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
                    itemstack.shrink(1);
                    return InteractionResult.CONSUME;
                }
            }

            BlockPos blockpos1;
            if (blockstate.getCollisionShape(level, blockpos).isEmpty()) {
                blockpos1 = blockpos;
            } else {
                blockpos1 = blockpos.relative(direction);
            }

            EntityType<?> entitytype = SpawnEggItem.getType(itemstack);

            CompoundTag itemTag = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

            itemTag.putString("LizardType", type.getId().toString());
            itemstack.set(DataComponents.CUSTOM_DATA, CustomData.of(itemTag));

            Entity entity = entitytype.spawn((ServerLevel)level, itemstack, context.getPlayer(), blockpos1, EntitySpawnReason.SPAWN_ITEM_USE, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP);

            if (entity != null) {
                if (entity instanceof LizardEntity lizardEntity) {
                    lizardEntity.loadFromNetTag(itemTag);
                }
                itemstack.shrink(1);
                level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
            }

            return InteractionResult.CONSUME;
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        HitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (hitresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        } else if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            BlockHitResult blockhitresult = (BlockHitResult)hitresult;
            BlockPos blockpos = blockhitresult.getBlockPos();
            if (!(level.getBlockState(blockpos).getBlock() instanceof LiquidBlock)) {
                return InteractionResult.PASS;
            } else if (level.mayInteract(player, blockpos) && player.mayUseItemAt(blockpos, blockhitresult.getDirection(), itemstack)) {
                EntityType<?> entitytype = SpawnEggItem.getType(itemstack);

                CompoundTag itemTag = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

                itemTag.putString("LizardType", type.getId().toString());
                itemstack.set(DataComponents.CUSTOM_DATA, CustomData.of(itemTag));

                if (entitytype.spawn((ServerLevel)level, itemstack, player, blockpos, EntitySpawnReason.SPAWN_ITEM_USE, false, false) == null) {
                    return InteractionResult.PASS;
                } else {
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, player.position());
                    return InteractionResult.CONSUME;
                }
            } else {
                return InteractionResult.FAIL;
            }
        }
    }
}
