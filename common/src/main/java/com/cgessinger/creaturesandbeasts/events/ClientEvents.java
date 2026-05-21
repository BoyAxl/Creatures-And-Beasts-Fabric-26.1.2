package com.cgessinger.creaturesandbeasts.events;

import com.cgessinger.creaturesandbeasts.client.entity.model.CactemSpearModel;
import com.cgessinger.creaturesandbeasts.client.entity.render.*;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class ClientEvents {

    public static void registerRenderers() {
        EntityRendererRegistry.register(CNBEntityTypes.LITTLE_GREBE.get(), LittleGrebeRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.LIZARD.get(), LizardRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.CINDERSHELL.get(), CindershellRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.LILYTAD.get(), LilytadRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.SPORELING.get(), SporelingRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.YETI.get(), YetiRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.MINIPAD.get(), MinipadRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.END_WHALE.get(), EndWhaleRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.CACTEM.get(), CactemRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.LIZARD_EGG.get(), manager -> new ThrownItemRenderer<>(manager, 1.0F, true));
        EntityRendererRegistry.register(CNBEntityTypes.THROWN_CACTEM_SPEAR.get(), ThrownCactemSpearRenderer::new);
    }

    public static void registerLayerDefinitions() {
        ModelLayerRegistry.registerModelLayer(CactemSpearModel.LAYER_LOCATION, CactemSpearModel::createLayer);
    }
}
