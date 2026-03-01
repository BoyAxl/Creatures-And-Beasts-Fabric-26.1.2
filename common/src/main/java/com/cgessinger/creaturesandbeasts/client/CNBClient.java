package com.cgessinger.creaturesandbeasts.client;

import com.cgessinger.creaturesandbeasts.client.gui.screens.inventory.CinderFurnaceScreen;
import com.cgessinger.creaturesandbeasts.init.CNBContainerTypes;
import com.cgessinger.creaturesandbeasts.init.CNBItems;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class CNBClient {
    public static void init() {
        MenuScreens.register(CNBContainerTypes.CINDER_FURNACE_CONTAINER.get(), CinderFurnaceScreen::new);
    }

    public static void initClientDeferred() {
        ItemPropertiesRegistry.register(CNBItems.CACTEM_SPEAR.get(), new ResourceLocation("throwing"), (item, resourceLocation, entity, itemPropertyFunction) -> entity != null && entity.isUsingItem() && entity.getUseItem() == item ? 1.0F : 0.0F);
    }
}
