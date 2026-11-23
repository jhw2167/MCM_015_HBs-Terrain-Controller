package com.holybuckets.terraincontrol.worldgen;

import net.blay09.mods.balm.api.world.BalmWorldGen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;

public final class TerrainControlWorldgen {

    public static final String MOD_ID = "hbs_terrain_control";
    public static final String OVERRIDE_PACK_NAME = "hbs_terrain_control_overrides";
    public static final String OVERRIDE_PACK_PATH = "resourcepacks/" + OVERRIDE_PACK_NAME;

    public static final TagKey<Biome> HAS_SUPER_SUNJUMP_FLOWER =
        TagKey.create(Registries.BIOME, id("has_super_sunjump_flower"));

    private TerrainControlWorldgen() {
    }

    public static void initialize(BalmWorldGen worldGen) {
        worldGen.addFeatureToBiomes(
            (name, biome) -> biome.is(HAS_SUPER_SUNJUMP_FLOWER),
            GenerationStep.Decoration.VEGETAL_DECORATION,
            id("super_sunjump_flower"));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
