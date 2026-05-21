package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.CindershellEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class CindershellModel extends GeoModel<CindershellEntity> {
    private static final Identifier CINDERSHELL_MODEL = CreaturesAndBeasts.id("geo/entity/cindershell/cindershell");
    private static final Identifier BABY_CINDERSHELL_MODEL = CreaturesAndBeasts.id("geo/entity/cindershell/baby_cindershell");
    private static final Identifier CINDERSHELL_FURNACE_MODEL = CreaturesAndBeasts.id("geo/entity/cindershell/cindershell_furnace");
    private static final Identifier CINDERSHELL_TEXTURE = CreaturesAndBeasts.id("textures/entity/cindershell/cindershell.png");
    private static final Identifier BABY_CINDERSHELL_TEXTURE = CreaturesAndBeasts.id("textures/entity/cindershell/baby_cindershell.png");
    private static final Identifier CINDERSHELL_ANIMATIONS = CreaturesAndBeasts.id("cindershell");
    private static final DataTicket<Identifier> MODEL = DataTickets.create("cindershell_model", Identifier.class);
    private static final DataTicket<Identifier> TEXTURE = DataTickets.create("cindershell_texture", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(MODEL, CINDERSHELL_MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, CINDERSHELL_TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(CindershellEntity entity) {
        return CINDERSHELL_ANIMATIONS;
    }

    @Override
    public void addAdditionalStateData(CindershellEntity entity, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(MODEL, modelFor(entity));
        renderState.addGeckolibData(TEXTURE, entity.isBaby() ? BABY_CINDERSHELL_TEXTURE : CINDERSHELL_TEXTURE);
    }

    private static Identifier modelFor(CindershellEntity entity) {
        if (entity.isBaby()) {
            return BABY_CINDERSHELL_MODEL;
        }
        return entity.hasFurnace() ? CINDERSHELL_FURNACE_MODEL : CINDERSHELL_MODEL;
    }
}
