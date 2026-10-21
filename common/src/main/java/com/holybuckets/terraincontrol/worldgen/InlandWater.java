package com.holybuckets.terraincontrol.worldgen;

import com.holybuckets.terraincontrol.mixin.AquiferFluidStatusAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class InlandWater {

    public static final boolean ENABLED = true;
    public static final boolean KEEP_RIVERS = true;
    public static final double COAST_CONTINENTALNESS = -0.11;
    public static final double RIVER_WEIRDNESS = 0.07;
    public static final Aquifer.FluidStatus DRY = new Aquifer.FluidStatus(DimensionType.WAY_BELOW_MIN_Y, Blocks.AIR.defaultBlockState());

    private static final int[][] RIVER_SAMPLE_OFFSETS = {{0, 0}, {16, 0}, {-16, 0}, {0, 16}, {0, -16}};

    private InlandWater() {
    }

    public static boolean shouldDry(Aquifer.FluidStatus status, DensityFunction continents, DensityFunction ridges, int x, int y, int z) {
        if (!ENABLED || status == null || continents == null || ridges == null) {
            return false;
        }
        AquiferFluidStatusAccessor fluid = (AquiferFluidStatusAccessor) (Object) status;
        if (!fluid.hbs_terrain_control$getFluidType().is(Blocks.WATER) || fluid.hbs_terrain_control$getFluidLevel() <= DimensionType.MIN_Y) {
            return false;
        }
        if (sample(continents, x, y, z) <= COAST_CONTINENTALNESS) {
            return false;
        }
        return !(KEEP_RIVERS && isNearRiver(ridges, x, y, z));
    }

    private static boolean isNearRiver(DensityFunction ridges, int x, int y, int z) {
        for (int[] offset : RIVER_SAMPLE_OFFSETS) {
            if (Math.abs(sample(ridges, x + offset[0], y, z + offset[1])) < RIVER_WEIRDNESS) {
                return true;
            }
        }
        return false;
    }

    private static double sample(DensityFunction function, int x, int y, int z) {
        return function.compute(new DensityFunction.SinglePointContext(x, y, z));
    }
}
