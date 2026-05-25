package com.cgessinger.creaturesandbeasts.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public class WaterlilyBlockItem extends BlockItem {
    public WaterlilyBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = super.useOn(context);
        return result == InteractionResult.FAIL ? InteractionResult.PASS : result;
    }
}
