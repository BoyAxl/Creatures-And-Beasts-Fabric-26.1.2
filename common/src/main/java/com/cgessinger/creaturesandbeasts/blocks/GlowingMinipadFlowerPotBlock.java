package com.cgessinger.creaturesandbeasts.blocks;

import com.cgessinger.creaturesandbeasts.util.CNBRegistrySupplier;
import com.cgessinger.creaturesandbeasts.util.MinipadGlow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class GlowingMinipadFlowerPotBlock extends FlowerPotBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final double PARTICLE_CHANCE = 0.1D;
    private final CNBRegistrySupplier<SimpleParticleType> particle;

    public GlowingMinipadFlowerPotBlock(Block content, BlockBehaviour.Properties properties, CNBRegistrySupplier<SimpleParticleType> particle) {
        super(content, properties);
        this.particle = particle;
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
    }

    public static int getLightLevel(BlockState state) {
        return state.getValue(LIT) ? 14 : 0;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            updateGlowState(state, level, pos);
            level.scheduleTick(pos, this, ticksUntilNextGlowChange(level));
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        updateGlowState(state, level, pos);
        level.scheduleTick(pos, this, ticksUntilNextGlowChange(level));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (!state.getValue(LIT) || random.nextDouble() >= PARTICLE_CHANCE) {
            return;
        }

        double x = pos.getX() + 0.5D + (random.nextDouble() * 0.5D - 0.25D);
        double y = pos.getY() + 0.55D + (random.nextDouble() * 0.08D - 0.04D);
        double z = pos.getZ() + 0.5D + (random.nextDouble() * 0.5D - 0.25D);
        level.addParticle(this.particle.get(), x, y, z, 0D, 0D, 0D);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    private static void updateGlowState(BlockState state, Level level, BlockPos pos) {
        boolean lit = MinipadGlow.isNightGlowTime(level.getDefaultClockTime());
        if (state.getValue(LIT) != lit) {
            level.setBlock(pos, state.setValue(LIT, lit), Block.UPDATE_ALL);
        }
    }

    private static int ticksUntilNextGlowChange(Level level) {
        return MinipadGlow.ticksUntilNextGlowChange(level.getDefaultClockTime());
    }
}
