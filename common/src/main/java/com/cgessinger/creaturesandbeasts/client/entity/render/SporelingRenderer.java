package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.SporelingModel;
import com.cgessinger.creaturesandbeasts.entities.SporelingEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class SporelingRenderer extends GeoEntityRenderer<SporelingEntity, LivingEntityRenderState> {
    private static final DataTicket<Float> RIDING_CROUCH_X_ROT = DataTickets.create("cnb_sporeling_riding_crouch_x_rot", Float.class);
    private static final DataTicket<Float> RIDING_CROUCH_Z_ROT = DataTickets.create("cnb_sporeling_riding_crouch_z_rot", Float.class);
    private static final DataTicket<Float> RIDING_ATTACK_Y_ROT = DataTickets.create("cnb_sporeling_riding_attack_y_rot", Float.class);

    public SporelingRenderer(EntityRendererProvider.Context context) {
        super(context, new SporelingModel());
        this.withRenderLayer(new SporelingHeldItemLayer(context, this));
        this.shadowRadius = 0.4F;
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }

    @Override
    public void extractRenderState(SporelingEntity animatable, LivingEntityRenderState renderState, float partialTick) {
        super.extractRenderState(animatable, renderState, partialTick);

        if (animatable.getVehicle() instanceof Player) {
            renderState.bodyRot = Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot);
            renderState.yRot = 0.0F;
            renderState.xRot = 0.0F;
        }
    }

    @Override
    public void addRenderData(SporelingEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);

        float crouchXRot = 0F;
        float crouchZRot = 0F;
        float attackYRot = 0F;

        if (animatable.getVehicle() instanceof Player player && player.isCrouching()) {
            float yRotLerped = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
            crouchXRot = -25.0F * Mth.cos(yRotLerped * Mth.DEG_TO_RAD);
            crouchZRot = -25.0F * Mth.sin(yRotLerped * Mth.DEG_TO_RAD);
        }

        if (animatable.getVehicle() instanceof Player player && player.getAttackAnim(partialTick) > 0) {
            float rotation = player.getAttackAnim(partialTick);
            attackYRot = -Mth.sin(Mth.sqrt(rotation) * ((float)Math.PI * 2F)) * 0.2F * Mth.RAD_TO_DEG;
        }

        renderState.addGeckolibData(RIDING_CROUCH_X_ROT, crouchXRot);
        renderState.addGeckolibData(RIDING_CROUCH_Z_ROT, crouchZRot);
        renderState.addGeckolibData(RIDING_ATTACK_Y_ROT, attackYRot);
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderInfo, SubmitNodeCollector submitNodeCollector) {
        PoseStack poseStack = renderInfo.poseStack();
        poseStack.mulPose(Axis.XP.rotationDegrees(renderInfo.getOrDefaultGeckolibData(RIDING_CROUCH_X_ROT, 0F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(renderInfo.getOrDefaultGeckolibData(RIDING_CROUCH_Z_ROT, 0F)));
        poseStack.mulPose(Axis.YP.rotationDegrees(renderInfo.getOrDefaultGeckolibData(RIDING_ATTACK_Y_ROT, 0F)));
    }

    private static class SporelingHeldItemLayer extends BlockAndItemGeoLayer<SporelingEntity, Void, LivingEntityRenderState> {
        public SporelingHeldItemLayer(EntityRendererProvider.Context context, SporelingRenderer renderer) {
            super(context, renderer);
        }

        @Override
        protected List<RenderData> getRelevantBones(SporelingEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            ItemStack heldStack = animatable.getItemBySlot(EquipmentSlot.MAINHAND);
            if (heldStack.isEmpty()) {
                return List.of();
            }

            ItemDisplayContext displayContext = ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            ItemStackRenderState itemRenderState = RenderUtil.createRenderStateForItem(heldStack, this.itemModelResolver, displayContext, animatable);

            return List.of(RenderData.item("itemHolder", displayContext, itemRenderState));
        }

        @Override
        public void addRenderData(SporelingEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            List<RenderData> relevantBones = getRelevantBones(animatable, relatedObject, renderState, partialTick);
            if (!relevantBones.isEmpty()) {
                renderState.addGeckolibData(CONTENTS, relevantBones);
            }
        }

        @Override
        protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState itemRenderState, ItemDisplayContext displayContext, LivingEntityRenderState renderState, SubmitNodeCollector submitNodeCollector, int packedLight) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(0.6F, 0.1F, -0.1F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            super.submitItemStackRender(poseStack, bone, itemRenderState, displayContext, renderState, submitNodeCollector, packedLight);
            poseStack.popPose();
        }
    }
}
