package com.cgessinger.creaturesandbeasts.mixin;

import com.cgessinger.creaturesandbeasts.entities.SporelingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At("HEAD"), cancellable = true)
    private void CNB_positionSporelingRider(Entity entity, Entity.MoveFunction moveFunction, CallbackInfo ci) {
        if (entity instanceof SporelingEntity sporelingEntity && ((Entity) (Object)this) instanceof Player player) {
            if (player.hasPassenger(sporelingEntity)) {
                SporelingEntity.positionBackpackPassenger(player, sporelingEntity, moveFunction);
                ci.cancel();
            }
        }
    }

    @Inject(method = "turn", at = @At("RETURN"))
    private void CNB_rotateSporelingRider(double xRot, double yRot, CallbackInfo ci) {
        if (((Entity) (Object)this) instanceof Player player) {
            SporelingEntity sporelingEntity = SporelingEntity.getBackpackPassenger(player);
            if (sporelingEntity != null) {
                SporelingEntity.clampBackpackRotation(player, sporelingEntity);
            }
        }
    }
}
