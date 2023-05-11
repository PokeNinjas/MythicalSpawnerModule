package com.mythicalnetwork.mythicalspawner.mixin;

import com.mythicalnetwork.mythicalspawner.events.EntityEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PersistentEntitySectionManager.class)
public class PersistentEntitySectionManagerMixin<T extends EntityAccess> {
    @Inject(method = "addEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/SectionPos;asLong(Lnet/minecraft/core/BlockPos;)J", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void mythicalspawner$fireEntityLoadEvent(T entity, boolean existing, CallbackInfoReturnable<Boolean> cir){
        if(entity instanceof Entity centity){
            if(!EntityEvents.LOADED_FROM_DISK.invoker().onLoadedFromDisk(centity, centity.level, existing)){
                cir.setReturnValue(false);
            }
        }
    }
}
