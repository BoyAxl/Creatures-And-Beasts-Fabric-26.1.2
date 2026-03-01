package com.cgessinger.creaturesandbeasts.items;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import dev.architectury.registry.fuel.FuelRegistry;
import net.minecraft.world.item.Item;

public class CNBFuelItem extends Item {

    public CNBFuelItem(int burnTime) {
        super(new Item.Properties().arch$tab(CreaturesAndBeasts.TAB));
        FuelRegistry.register(burnTime, this);
    }
}
