package com.holybuckets.terraincontrol.worldgen.carver;

import com.holybuckets.terraincontrol.worldgen.TerrainControlWorldgen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public final class ModCarvers {

    public static final ResourceLocation WEIGHTED_CAVE_ID = TerrainControlWorldgen.id("weighted_cave");
    public static final ResourceLocation PIT_ID = TerrainControlWorldgen.id("pit");

    public static final WorldCarver<WeightedCaveConfig> WEIGHTED_CAVE = new WeightedCaveCarver(WeightedCaveConfig.CODEC);
    public static final WorldCarver<PitCarverConfiguration> PIT = new PitCarver(PitCarverConfiguration.CODEC);

    public static final ResourceKey<ConfiguredWorldCarver<?>> CAVE_FLAVORS =
        ResourceKey.create(Registries.CONFIGURED_CARVER, TerrainControlWorldgen.id("cave_flavors"));

    private ModCarvers() {
    }
}
