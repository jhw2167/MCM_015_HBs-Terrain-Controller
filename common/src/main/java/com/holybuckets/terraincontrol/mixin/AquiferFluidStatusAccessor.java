package com.holybuckets.terraincontrol.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Aquifer.FluidStatus.class)
public interface AquiferFluidStatusAccessor {

    @Accessor("fluidLevel")
    int hbs_terrain_control$getFluidLevel();

    @Accessor("fluidType")
    BlockState hbs_terrain_control$getFluidType();
}
