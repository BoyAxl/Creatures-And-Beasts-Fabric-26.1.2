package com.cgessinger.creaturesandbeasts.fabric.client;

import com.cgessinger.creaturesandbeasts.client.CNBClient;
import com.cgessinger.creaturesandbeasts.events.ClientEvents;
import com.cgessinger.creaturesandbeasts.init.CNBParticleTypes;
import net.fabricmc.api.ClientModInitializer;

public class CreaturesAndBeastsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CNBClient.init();
        CNBClient.initClientDeferred();
        CNBParticleTypes.registerParticleFactories();
        ClientEvents.registerRenderers();
        ClientEvents.registerLayerDefinitions();
    }
}
