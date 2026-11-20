package com.holybuckets.terraincontrol.mixin;

import com.holybuckets.terraincontrol.worldgen.CaveStructureGuard;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Structure.class)
public abstract class MixinStructure {

    @Inject(method = "generate", at = @At("RETURN"), cancellable = true)
    private void rejectIncompleteCaves(CallbackInfoReturnable<StructureStart> cir) {
        if (CaveStructureGuard.isIncompleteCave((Structure) (Object) this, cir.getReturnValue()))
            cir.setReturnValue(StructureStart.INVALID_START);
    }
}
