package com.cgessinger.creaturesandbeasts.util;

import net.minecraft.util.Mth;

public final class MinipadGlow {
    private static final long DAY_TIME = 24000L;
    private static final long START_TIME = 13000L;
    private static final long END_TIME = 23000L;
    private static final int TRANSITION_TICKS = 200;

    private MinipadGlow() {
    }

    public static boolean isNightGlowTime(long defaultClockTime) {
        long time = normalizeTime(defaultClockTime);
        return time >= START_TIME && time <= END_TIME;
    }

    public static boolean dropsGlowingFlowerAt(long defaultClockTime) {
        return normalizeTime(defaultClockTime) > START_TIME;
    }

    public static int ticksUntilNextGlowChange(long defaultClockTime) {
        long time = normalizeTime(defaultClockTime);
        if (time < START_TIME) {
            return (int) (START_TIME - time);
        }

        if (time <= END_TIME) {
            return (int) (END_TIME - time + 1);
        }

        return (int) (DAY_TIME - time + START_TIME);
    }

    public static float getAlpha(long defaultClockTime, float partialTick) {
        float time = normalizeTime(defaultClockTime) + partialTick;
        if (time >= DAY_TIME) {
            time -= DAY_TIME;
        }

        if (time < START_TIME) {
            return 0.0F;
        }

        if (time < START_TIME + TRANSITION_TICKS) {
            return Mth.clamp((time - START_TIME) / TRANSITION_TICKS, 0.0F, 1.0F);
        }

        if (time <= END_TIME) {
            return 1.0F;
        }

        if (time < END_TIME + TRANSITION_TICKS) {
            return 1.0F - Mth.clamp((time - END_TIME) / TRANSITION_TICKS, 0.0F, 1.0F);
        }

        return 0.0F;
    }

    private static long normalizeTime(long defaultClockTime) {
        return Math.floorMod(defaultClockTime, DAY_TIME);
    }
}
