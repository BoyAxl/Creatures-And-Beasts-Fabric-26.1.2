package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.YetiModel;
import com.cgessinger.creaturesandbeasts.entities.YetiEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
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

import java.util.List;

@Environment(EnvType.CLIENT)
public class YetiRenderer extends GeoEntityRenderer<YetiEntity, LivingEntityRenderState> {
    public YetiRenderer(EntityRendererProvider.Context context) {
        super(context, new YetiModel());
        this.withRenderLayer(new YetiHeldItemLayer(context, this));
        this.shadowRadius = 0.7F;
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    private static class YetiHeldItemLayer extends BlockAndItemGeoLayer<YetiEntity, Void, LivingEntityRenderState> {
        private static final DataTicket<Boolean> BABY = DataTickets.create("cnb_yeti_baby", Boolean.class);

        public YetiHeldItemLayer(EntityRendererProvider.Context context, YetiRenderer renderer) {
            super(context, renderer);
        }

        @Override
        protected List<RenderData> getRelevantBones(YetiEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            ItemStack heldStack = animatable.getHolding();
            if (heldStack.isEmpty()) {
                return List.of();
            }

            ItemDisplayContext displayContext = ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            ItemStackRenderState itemRenderState = RenderUtil.createRenderStateForItem(heldStack, this.itemModelResolver, displayContext, animatable);

            return List.of(RenderData.item("ItemHolder", displayContext, itemRenderState));
        }

        @Override
        public void addRenderData(YetiEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            List<RenderData> relevantBones = getRelevantBones(animatable, relatedObject, renderState, partialTick);
            if (!relevantBones.isEmpty()) {
                renderState.addGeckolibData(CONTENTS, relevantBones);
                renderState.addGeckolibData(BABY, animatable.isBaby());
            }
        }

        @Override
        protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState itemRenderState, ItemDisplayContext displayContext, LivingEntityRenderState renderState, SubmitNodeCollector submitNodeCollector, int packedLight) {
            poseStack.pushPose();

            if (renderState.getOrDefaultGeckolibData(BABY, false)) {
                poseStack.translate(0.05D, 0.3D, 0.15D);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-43.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
            } else {
                poseStack.translate(0.3D, 1.0D, 0);
            }

            super.submitItemStackRender(poseStack, bone, itemRenderState, displayContext, renderState, submitNodeCollector, packedLight);
            poseStack.popPose();
        }
    }
}
