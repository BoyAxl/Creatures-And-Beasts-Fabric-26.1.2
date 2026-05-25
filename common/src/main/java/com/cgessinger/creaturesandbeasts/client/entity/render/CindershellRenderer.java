package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.CindershellModel;
import com.cgessinger.creaturesandbeasts.entities.CindershellEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CindershellRenderer extends GeoEntityRenderer<CindershellEntity, LivingEntityRenderState> {
    public CindershellRenderer(EntityRendererProvider.Context context) {
        super(context, new CindershellModel());
        this.withRenderLayer(new CindershellGlowLayer(this));
        this.withRenderLayer(new CindershellHeldItemLayer(context, this));
        this.shadowRadius = 0.4F;
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0;
    }

    private static class CindershellHeldItemLayer extends BlockAndItemGeoLayer<CindershellEntity, Void, LivingEntityRenderState> {
        private static final double HELD_ITEM_Y_OFFSET = 0.62D;
        private static final double HELD_ITEM_Z_OFFSET = -1.52D;
        private static final float HELD_ITEM_X_ROT = 90.0F;

        public CindershellHeldItemLayer(EntityRendererProvider.Context context, CindershellRenderer renderer) {
            super(context, renderer);
        }

        @Override
        protected List<RenderData> getRelevantBones(CindershellEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            ItemStack heldStack = animatable.getItemBySlot(EquipmentSlot.MAINHAND);
            if (heldStack.isEmpty()) {
                return List.of();
            }

            ItemDisplayContext displayContext = ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            ItemStackRenderState itemRenderState = RenderUtil.createRenderStateForItem(heldStack, this.itemModelResolver, displayContext, animatable);

            return List.of(RenderData.item("itemHolder", displayContext, itemRenderState));
        }

        @Override
        public void addRenderData(CindershellEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            List<RenderData> relevantBones = getRelevantBones(animatable, relatedObject, renderState, partialTick);
            if (!relevantBones.isEmpty()) {
                renderState.addGeckolibData(CONTENTS, relevantBones);
            }
        }

        @Override
        protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState itemRenderState, ItemDisplayContext displayContext, LivingEntityRenderState renderState, SubmitNodeCollector submitNodeCollector, int packedLight) {
            poseStack.pushPose();
            bone.translateAwayFromPivotPoint(poseStack);
            poseStack.translate(0, HELD_ITEM_Y_OFFSET, HELD_ITEM_Z_OFFSET);
            poseStack.mulPose(Axis.XP.rotationDegrees(HELD_ITEM_X_ROT));
            super.submitItemStackRender(poseStack, bone, itemRenderState, displayContext, renderState, submitNodeCollector, packedLight);
            poseStack.popPose();
        }
    }
}
