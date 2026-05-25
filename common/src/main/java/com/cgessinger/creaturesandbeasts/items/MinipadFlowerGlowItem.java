package com.cgessinger.creaturesandbeasts.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class MinipadFlowerGlowItem extends BlockItem {
    public MinipadFlowerGlowItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
