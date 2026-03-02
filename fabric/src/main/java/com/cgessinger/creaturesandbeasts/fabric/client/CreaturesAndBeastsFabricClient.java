package com.cgessinger.creaturesandbeasts.fabric.client;

import com.cgessinger.creaturesandbeasts.client.CNBClient;
import com.cgessinger.creaturesandbeasts.events.ClientEvents;
import net.fabricmc.api.ClientModInitializer;

public class CreaturesAndBeastsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CNBClient.init();
        CNBClient.initClientDeferred();
        ClientEvents.registerRenderers();
        ClientEvents.registerLayerDefinitions();
    }
}
