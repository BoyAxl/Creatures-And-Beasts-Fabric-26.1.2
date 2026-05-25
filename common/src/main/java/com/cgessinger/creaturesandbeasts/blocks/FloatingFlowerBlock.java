package com.cgessinger.creaturesandbeasts.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class FloatingFlowerBlock extends FlowerBlock {
    public FloatingFlowerBlock(Holder<MobEffect> effect, float seconds, BlockBehaviour.Properties properties) {
        super(effect, seconds, properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        if (super.mayPlaceOn(state, level, pos)) {
            return true;
        }

        FluidState fluidState = level.getFluidState(pos);
        FluidState aboveFluidState = level.getFluidState(pos.above());
        return (fluidState.is(FluidTags.SUPPORTS_LILY_PAD) || state.is(BlockTags.SUPPORTS_LILY_PAD)) && aboveFluidState.is(Fluids.EMPTY);
    }
}
