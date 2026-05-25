package com.cgessinger.creaturesandbeasts.items;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class FloatingFlowerBlockItem extends BlockItem {
    public FloatingFlowerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        if (context.getLevel().getFluidState(placeContext.getClickedPos()).is(FluidTags.SUPPORTS_LILY_PAD)) {
            return InteractionResult.PASS;
        }

        InteractionResult result = super.useOn(context);
        return result == InteractionResult.FAIL ? InteractionResult.PASS : result;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionResult placeOnWaterResult = placeOnWater(level, player, hand);
        return placeOnWaterResult.consumesAction() ? placeOnWaterResult : super.use(level, player, hand);
    }

    private InteractionResult placeOnWater(Level level, Player player, InteractionHand hand) {
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }

        BlockPos placePos = hitResult.getBlockPos().above();
        BlockHitResult placeHit = hitResult.withPosition(placePos);
        return super.useOn(new UseOnContext(player, hand, placeHit));
    }
}
