package com.cgessinger.creaturesandbeasts.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public abstract class ReachableBlockTargetGoal extends Goal {
    protected final PathfinderMob mob;

    private final double speedModifier;
    private final int pathRecalcInterval;
    private final int maxMoveTicks;
    private int timeToRecalcPath;
    private int moveTicks;
    private BlockPos targetPos;
    private Path targetPath;

    protected ReachableBlockTargetGoal(PathfinderMob mob, double speedModifier, int pathRecalcInterval, int maxMoveTicks) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.pathRecalcInterval = pathRecalcInterval;
        this.maxMoveTicks = maxMoveTicks;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canContinueToUse() {
        return this.hasTarget() && this.canContinueMoving() && this.moveTicks < this.maxMoveTicks;
    }

    @Override
    public void start() {
        this.moveTicks = 0;
        this.timeToRecalcPath = 0;
        this.moveToTarget();
    }

    @Override
    public void tick() {
        ++this.moveTicks;
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(this.pathRecalcInterval);
            this.moveToTarget();
        }
    }

    @Override
    public void stop() {
        this.clearTarget();
        this.mob.getNavigation().stop();
    }

    protected boolean canContinueMoving() {
        return true;
    }

    protected double getMoveSpeed() {
        return this.speedModifier;
    }

    protected boolean hasTarget() {
        return this.targetPos != null;
    }

    protected boolean setReachableTarget(Iterable<BlockPos> candidates) {
        return this.setReachableTarget(candidates, 0);
    }

    protected boolean setReachableTarget(Iterable<BlockPos> candidates, int maxPathCandidates) {
        int checkedCandidates = 0;
        for (BlockPos candidate : candidates) {
            Path path = this.createReachablePath(candidate);
            if (path != null) {
                this.targetPos = candidate;
                this.targetPath = path;
                return true;
            }

            if (maxPathCandidates > 0 && ++checkedCandidates >= maxPathCandidates) {
                break;
            }
        }

        return false;
    }

    protected List<BlockPos> collectCandidates(int horizontalRange, int verticalRange, Predicate<BlockPos> targetPredicate, Comparator<BlockPos> comparator) {
        BlockPos origin = this.mob.blockPosition();
        List<BlockPos> candidates = new ArrayList<>();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int y = -verticalRange; y <= verticalRange; ++y) {
            for (int x = -horizontalRange; x <= horizontalRange; ++x) {
                for (int z = -horizontalRange; z <= horizontalRange; ++z) {
                    mutablePos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (targetPredicate.test(mutablePos)) {
                        candidates.add(mutablePos.immutable());
                    }
                }
            }
        }

        candidates.sort(comparator);
        return candidates;
    }

    @Nullable
    protected Path createReachablePath(BlockPos pos) {
        Path path = this.mob.getNavigation().createPath(pos, 0);
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
            this.clearTarget();
            this.mob.getNavigation().stop();
            return;
        }

        this.targetPath = null;
        this.mob.getNavigation().moveTo(path, this.getMoveSpeed());
    }

    private void clearTarget() {
        this.targetPos = null;
        this.targetPath = null;
        this.moveTicks = 0;
        this.timeToRecalcPath = 0;
    }
}
