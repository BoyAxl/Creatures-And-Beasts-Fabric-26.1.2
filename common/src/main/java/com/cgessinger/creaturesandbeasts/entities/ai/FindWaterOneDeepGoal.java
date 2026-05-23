package com.cgessinger.creaturesandbeasts.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;

import java.util.Comparator;
import java.util.List;

public class FindWaterOneDeepGoal extends ReachableBlockTargetGoal {
    private static final int LAND_SEARCH_DELAY = 200;
    private static final int SEARCH_RETRY_INTERVAL = 3600;
    private static final int DEEP_WATER_FALLBACK_DURATION = 6000;
    private static final int SEARCH_RANGE = 20;
    private static final int PATH_RECALC_INTERVAL = 20;
    private static final int MAX_RETURN_TICKS = 200;

    private final PathfinderMob creature;
    private int landStartTick = -1;
    private int nextSearchTick;
    private boolean waitingForDeepWaterFallback;

    public FindWaterOneDeepGoal(PathfinderMob creature) {
        this(creature, 1.0D);
    }

    public FindWaterOneDeepGoal(PathfinderMob creature, double speedModifier) {
        super(creature, speedModifier, PATH_RECALC_INTERVAL, MAX_RETURN_TICKS);
        this.creature = creature;
    }

    @Override
    public boolean canUse() {
        if (this.isStandingInValidWater()) {
            this.landStartTick = -1;
            this.nextSearchTick = 0;
            this.waitingForDeepWaterFallback = false;
            this.stopDeepWaterFallback();
            return false;
        }

        boolean onLand = this.isOnLand();
        boolean inDeepWater = this.isInWaterTooDeep();
        if (!onLand && !inDeepWater) {
            this.landStartTick = -1;
            return false;
        }

        if (onLand && this.waitingForDeepWaterFallback) {
            this.nextSearchTick = 0;
            this.waitingForDeepWaterFallback = false;
        }

        if (onLand && !this.hasWaitedOnLand()) {
            return false;
        }

        if (this.creature.tickCount < this.nextSearchTick) {
            return false;
        }

        return this.searchReachableWater();
    }

    @Override
    protected boolean canContinueMoving() {
        return !this.isStandingInValidWater();
    }

    @Override
    public void stop() {
        if (this.isStandingInValidWater()) {
            this.landStartTick = -1;
            this.nextSearchTick = 0;
            this.waitingForDeepWaterFallback = false;
            this.stopDeepWaterFallback();
        } else if (this.isInWaterTooDeep()) {
            this.startDeepWaterFallback();
        } else {
            this.delayNextSearch(SEARCH_RETRY_INTERVAL);
        }

        super.stop();
    }

    private boolean isOnLand() {
        return this.creature.onGround() && !this.creature.isInWater();
    }

    private boolean isStandingInValidWater() {
        return this.creature.onGround() && this.isOneDeepWaterTarget(this.creature.blockPosition());
    }

    private boolean isInWaterTooDeep() {
        if (!this.creature.isInWater()) {
            return false;
        }

        BlockPos pos = this.creature.blockPosition();
        return this.creature.level().getFluidState(pos).is(FluidTags.WATER)
                && (this.creature.level().getFluidState(pos.above()).is(FluidTags.WATER)
                || this.creature.level().getFluidState(pos.below()).is(FluidTags.WATER));
    }

    private boolean hasWaitedOnLand() {
        if (this.landStartTick < 0) {
            this.landStartTick = this.creature.tickCount;
        }

        return this.creature.tickCount - this.landStartTick >= LAND_SEARCH_DELAY;
    }

    private boolean searchReachableWater() {
        boolean inDeepWater = this.isInWaterTooDeep();
        if (this.setReachableTarget(this.collectOneDeepWaterCandidates())) {
            this.waitingForDeepWaterFallback = false;
            return true;
        }

        if (inDeepWater) {
            this.startDeepWaterFallback();
        } else {
            this.delayNextSearch(SEARCH_RETRY_INTERVAL);
        }
        return false;
    }

    private void startDeepWaterFallback() {
        this.waitingForDeepWaterFallback = true;
        this.delayNextSearch(DEEP_WATER_FALLBACK_DURATION);
        if (this.creature instanceof DeepWaterFallback fallback) {
            fallback.startDeepWaterFallback(this.adjustedTickDelay(DEEP_WATER_FALLBACK_DURATION));
        }
    }

    private void stopDeepWaterFallback() {
        if (this.creature instanceof DeepWaterFallback fallback) {
            fallback.stopDeepWaterFallback();
        }
    }

    private void delayNextSearch(int ticks) {
        this.nextSearchTick = this.creature.tickCount + this.adjustedTickDelay(ticks);
    }

    private List<BlockPos> collectOneDeepWaterCandidates() {
        BlockPos origin = this.creature.blockPosition();
        return this.collectCandidates(SEARCH_RANGE, SEARCH_RANGE, this::isOneDeepWaterTarget, Comparator.comparingDouble(origin::distSqr));
    }

    private boolean isOneDeepWaterTarget(BlockPos pos) {
        return this.creature.level().getFluidState(pos).is(FluidTags.WATER)
                && this.creature.level().getBlockState(pos.below()).isSolid()
                && this.creature.level().getBlockState(pos.above()).isAir();
    }

    @Override
    protected double getMoveSpeed() {
        return this.creature.isInWater() ? 1.5D : super.getMoveSpeed();
    }

    public interface DeepWaterFallback {
        void startDeepWaterFallback(int durationTicks);

        void stopDeepWaterFallback();
    }
}
