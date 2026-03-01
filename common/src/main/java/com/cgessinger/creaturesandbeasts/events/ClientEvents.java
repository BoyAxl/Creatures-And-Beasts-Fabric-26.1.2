package com.cgessinger.creaturesandbeasts.events;

import com.cgessinger.creaturesandbeasts.client.entity.model.CactemSpearModel;
import com.cgessinger.creaturesandbeasts.client.entity.render.*;
import com.cgessinger.creaturesandbeasts.init.CNBEntityTypes;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class ClientEvents {

    public static void registerRenderers() {
        EntityRendererRegistry.register(CNBEntityTypes.LITTLE_GREBE, LittleGrebeRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.LIZARD, LizardRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.CINDERSHELL, CindershellRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.LILYTAD, LilytadRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.SPORELING, SporelingRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.YETI, YetiRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.MINIPAD, MinipadRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.END_WHALE, EndWhaleRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.CACTEM, CactemRenderer::new);
        EntityRendererRegistry.register(CNBEntityTypes.LIZARD_EGG, manager -> new ThrownItemRenderer<>(manager, 1.0F, true));
        EntityRendererRegistry.register(CNBEntityTypes.THROWN_CACTEM_SPEAR, ThrownCactemSpearRenderer::new);
    }

    public static void registerLayerDefinitions() {
        EntityModelLayerRegistry.register(CactemSpearModel.LAYER_LOCATION, CactemSpearModel::createLayer);
    }
}
