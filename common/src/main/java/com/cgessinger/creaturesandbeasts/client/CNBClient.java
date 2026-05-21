package com.cgessinger.creaturesandbeasts.client;

import com.cgessinger.creaturesandbeasts.client.gui.screens.inventory.CinderFurnaceScreen;
import com.cgessinger.creaturesandbeasts.init.CNBContainerTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;

@Environment(EnvType.CLIENT)
public class CNBClient {
    public static void init() {
        MenuScreens.register(CNBContainerTypes.CINDER_FURNACE_CONTAINER.get(), CinderFurnaceScreen::new);
    }

    public static void initClientDeferred() {
    }
}
