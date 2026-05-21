package com.cgessinger.creaturesandbeasts.mixin;

import com.cgessinger.creaturesandbeasts.entities.EndWhaleEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer<T extends LivingEntity> {
    private static final DataTicket<Float> END_WHALE_RIDING_Z_ROT = DataTickets.create("cnb_end_whale_passenger_z_rot", Float.class);
    private static final DataTicket<Float> END_WHALE_RIDING_X_ROT = DataTickets.create("cnb_end_whale_passenger_x_rot", Float.class);

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("RETURN"))
    private void CNB_extractEndWhaleRidingRotations(T entity, LivingEntityRenderState renderState, float partialTick, CallbackInfo ci) {
        if (entity.getVehicle() instanceof EndWhaleEntity endWhale) {
            float headRot = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
            float bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);

            renderState.bodyRot = bodyRot;
            renderState.yRot = Mth.wrapDegrees(headRot - bodyRot);
            if (renderState.isUpsideDown) {
                renderState.yRot *= -1.0F;
            }

            renderState.addGeckolibData(END_WHALE_RIDING_Z_ROT, Mth.wrapDegrees(endWhale.getViewYRot(partialTick) - entity.getViewYRot(partialTick)) / 2.0F);
            renderState.addGeckolibData(END_WHALE_RIDING_X_ROT, Mth.wrapDegrees(endWhale.getViewXRot(partialTick) - entity.getViewXRot(partialTick)));
        }
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("RETURN"))
    private void CNB_setupWhaleRidingRotations(LivingEntityRenderState renderState, PoseStack stack, float rotationYaw, float scale, CallbackInfo ci) {
        stack.mulPose(Axis.ZP.rotationDegrees(renderState.getOrDefaultGeckolibData(END_WHALE_RIDING_Z_ROT, 0.0F)));
        stack.mulPose(Axis.XP.rotationDegrees(renderState.getOrDefaultGeckolibData(END_WHALE_RIDING_X_ROT, 0.0F)));
    }
}
