package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

@Environment(EnvType.CLIENT)
public class CactemSpearModel extends Model<Object> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(CreaturesAndBeasts.id("thrown_cactem_spear"), "main");
    private final ModelPart spear;

	public CactemSpearModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.spear = root.getChild("spear");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("spear", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(-0.5F, -2.0F, -6.0F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -0.5F, -6.0F, 3.0F, 0.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 4).addBox(-1.5F, -1.5F, 0.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

		return LayerDefinition.create(meshdefinition, 64, 64);
	}
}
