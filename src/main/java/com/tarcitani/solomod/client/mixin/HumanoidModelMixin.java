package com.tarcitani.solomod.client.mixin;

import com.tarcitani.solomod.client.PlayerAnimations;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Aplica poses de habilidade após a pose vanilla; PlayerModel copia os braços para as mangas em seguida. */
@Mixin(value = HumanoidModel.class, remap = false)
public abstract class HumanoidModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"), remap = false)
    private void solomod$abilityPose(LivingEntity entity, float swing, float amount, float age, float yaw, float pitch, CallbackInfo ci) {
        PlayerAnimations.poseModel((HumanoidModel<?>) (Object) this, entity, age);
    }
}
