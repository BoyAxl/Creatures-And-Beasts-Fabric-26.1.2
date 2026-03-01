package com.cgessinger.creaturesandbeasts.mixin;

import com.cgessinger.creaturesandbeasts.entities.SporelingEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mixin(GeoEntityRenderer.class)
public class MixinGeoEntityRenderer<T extends LivingEntity & GeoAnimatable> {

    @ModifyVariable(method = "actuallyRender(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/Entity;Lsoftware/bernie/geckolib/cache/object/BakedGeoModel;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIIFFFF)V", at = @At(value = "STORE"), name = "shouldSit")
    private boolean CNB_stopSporelingRotatingOnPlayer(boolean value, @Local(name = "livingEntity") T entity) {
        if (entity instanceof SporelingEntity && entity.getVehicle() instanceof Player) {
            return false;
        } else {
            return value;
        }
    }
}
