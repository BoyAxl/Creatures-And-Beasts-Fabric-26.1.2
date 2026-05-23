package com.cgessinger.creaturesandbeasts.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class FindWaterOneDeepGoal extends Goal {
    private static final int LAND_SEARCH_DELAY = 200;
    private static final int SEARCH_RETRY_INTERVAL = 3600;
    private static final int DEEP_WATER_FALLBACK_DURATION = 6000;
    private static final int SEARCH_RANGE = 20;
    private static final int PATH_RECALC_INTERVAL = 20;
    private static final int MAX_RETURN_TICKS = 200;

    private final PathfinderMob creature;
    private final double speedModifier;
    private int landStartTick = -1;
    private int nextSearchTick;
    private int timeToRecalcPath;
    private int returnTicks;
    private boolean waitingForDeepWaterFallback;
    private BlockPos targetPos;
    private Path targetPath;

    public FindWaterOneDeepGoal(PathfinderMob creature) {
        this(creature, 1.0D);
    }

    public FindWaterOneDeepGoal(PathfinderMob creature, double speedModifier) {
        this.creature = creature;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
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
    public boolean canContinueToUse() {
        return this.targetPos != null && !this.isStandingInValidWater() && this.returnTicks < MAX_RETURN_TICKS;
    }

    @Override
    public void start() {
        this.returnTicks = 0;
        this.timeToRecalcPath = 0;
        this.moveToTarget();
    }

    @Override
    public void tick() {
        ++this.returnTicks;
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(PATH_RECALC_INTERVAL);
            this.moveToTarget();
        }
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

        this.targetPos = null;
        this.targetPath = null;
        this.returnTicks = 0;
        this.timeToRecalcPath = 0;
        this.creature.getNavigation().stop();
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
        for (BlockPos candidate : this.collectOneDeepWaterCandidates()) {
            Path path = this.createReachablePath(candidate);
            if (path != null) {
                this.waitingForDeepWaterFallback = false;
                this.targetPos = candidate;
                this.targetPath = path;
                return true;
            }
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
        List<BlockPos> candidates = new ArrayList<>();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int y = -SEARCH_RANGE; y <= SEARCH_RANGE; ++y) {
            for (int x = -SEARCH_RANGE; x <= SEARCH_RANGE; ++x) {
                for (int z = -SEARCH_RANGE; z <= SEARCH_RANGE; ++z) {
                    mutablePos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (this.isOneDeepWaterTarget(mutablePos)) {
                        candidates.add(mutablePos.immutable());
                    }
                }
            }
        }

        candidates.sort(Comparator.comparingDouble(origin::distSqr));
        return candidates;
    }

    private boolean isOneDeepWaterTarget(BlockPos pos) {
        return this.creature.level().getFluidState(pos).is(FluidTags.WATER)
                && this.creature.level().getBlockState(pos.below()).isSolid()
                && this.creature.level().getBlockState(pos.above()).isAir();
    }

    @Nullable
    private Path createReachablePath(BlockPos pos) {
        Path path = this.creature.getNavigation().createPath(pos, 0);
        if (path != null && path.getNodeCount() > 0 && path.canReach()) {
            return path;
        }

        return null;
    }

    private void moveToTarget() {
        if (this.targetPos == null) {
            return;
        }

        Path path = this.targetPath != null ? this.targetPath : this.createReachablePath(this.targetPos);
        if (path == null) {
            this.targetPath = null;
            this.targetPos = null;
            this.creature.getNavigation().stop();
            return;
        }

        this.targetPath = null;
        double speed = this.creature.isInWater() ? 1.5D : this.speedModifier;
        this.creature.getNavigation().moveTo(path, speed);
    }

    public interface DeepWaterFallback {
        void startDeepWaterFallback(int durationTicks);

        void stopDeepWaterFallback();
    }
}
