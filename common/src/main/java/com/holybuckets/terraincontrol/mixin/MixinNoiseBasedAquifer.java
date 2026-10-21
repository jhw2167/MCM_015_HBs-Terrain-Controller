package com.holybuckets.terraincontrol.mixin;

import com.holybuckets.terraincontrol.worldgen.InlandWater;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.level.levelgen.Aquifer$NoiseBasedAquifer")
public abstract class MixinNoiseBasedAquifer {

    @Unique
    private DensityFunction hbs_terrain_control$continents;

    @Unique
    private DensityFunction hbs_terrain_control$ridges;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hbs_terrain_control$captureRouter(NoiseChunk noiseChunk, ChunkPos chunkPos, NoiseRouter router,
                                                   PositionalRandomFactory randomFactory, int minY, int height,
                                                   Aquifer.FluidPicker globalFluidPicker, CallbackInfo ci) {
        this.hbs_terrain_control$continents = router.continents();
        this.hbs_terrain_control$ridges = router.ridges();
    }

    @Inject(method = "computeFluid", at = @At("RETURN"), cancellable = true)
    private void hbs_terrain_control$blockInlandSeaWater(int x, int y, int z, CallbackInfoReturnable<Aquifer.FluidStatus> cir) {
        if (InlandWater.shouldDry(cir.getReturnValue(), this.hbs_terrain_control$continents, this.hbs_terrain_control$ridges, x, y, z)) {
            cir.setReturnValue(InlandWater.DRY);
        }
    }
}
