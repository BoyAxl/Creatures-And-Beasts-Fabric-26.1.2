package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.CactemModel;
import com.cgessinger.creaturesandbeasts.entities.CactemEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CactemRenderer extends GeoEntityRenderer<CactemEntity, LivingEntityRenderState> {
    private static final DataTicket<Boolean> SPEAR_SHOWN = DataTickets.create("cnb_cactem_spear_shown", Boolean.class);

    public CactemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CactemModel());
        this.withRenderLayer(new CactemHeldItemLayer(renderManager, this));
        this.shadowRadius = 0.4F;
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }

    @Override
    public void extractRenderState(CactemEntity animatable, LivingEntityRenderState renderState, float partialTick) {
        super.extractRenderState(animatable, renderState, partialTick);

        if (animatable.isHealFacingLocked()) {
            renderState.bodyRot = animatable.getHealFacingYRot();
            renderState.yRot = 0.0F;
            renderState.xRot = animatable.getHealFacingXRot();
        }
    }

    @Override
    public void addRenderData(CactemEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(SPEAR_SHOWN, animatable.isSpearShown());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderInfo, BoneSnapshots boneSnapshots) {
        super.adjustModelBonesForRender(renderInfo, boneSnapshots);
        if (!renderInfo.getOrDefaultGeckolibData(SPEAR_SHOWN, true)) {
            boneSnapshots.ifPresent("spear", bone -> bone.skipRender(true));
        }
    }

    private static class CactemHeldItemLayer extends BlockAndItemGeoLayer<CactemEntity, Void, LivingEntityRenderState> {
        public CactemHeldItemLayer(EntityRendererProvider.Context context, CactemRenderer renderer) {
            super(context, renderer);
        }

        @Override
        protected List<RenderData> getRelevantBones(CactemEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            if (!animatable.isTrading()) {
                return List.of();
            }

            ItemDisplayContext displayContext = ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            ItemStackRenderState itemRenderState = RenderUtil.createRenderStateForItem(new ItemStack(Items.TOTEM_OF_UNDYING), this.itemModelResolver, displayContext, animatable);

            return List.of(RenderData.item("ItemHolder", displayContext, itemRenderState));
        }

        @Override
        public void addRenderData(CactemEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            List<RenderData> relevantBones = getRelevantBones(animatable, relatedObject, renderState, partialTick);
            if (!relevantBones.isEmpty()) {
                renderState.addGeckolibData(CONTENTS, relevantBones);
            }
        }

        @Override
        protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState itemRenderState, ItemDisplayContext displayContext, LivingEntityRenderState renderState, SubmitNodeCollector submitNodeCollector, int packedLight) {
            poseStack.pushPose();
            bone.translateAwayFromPivotPoint(poseStack);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(-0.4F, 0.3F, -0.1F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            super.submitItemStackRender(poseStack, bone, itemRenderState, displayContext, renderState, submitNodeCollector, packedLight);
            poseStack.popPose();
        }
    }
}
