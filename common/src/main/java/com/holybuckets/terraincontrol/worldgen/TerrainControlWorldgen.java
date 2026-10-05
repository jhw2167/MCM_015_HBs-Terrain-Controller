package com.holybuckets.terraincontrol.worldgen;

import net.minecraft.resources.ResourceLocation;

public final class TerrainControlWorldgen {

    public static final String MOD_ID = "hbs_terrain_control";
    public static final String OVERRIDE_PACK_NAME = "hbs_terrain_control_overrides";
    public static final String OVERRIDE_PACK_PATH = "resourcepacks/" + OVERRIDE_PACK_NAME;

    private TerrainControlWorldgen() {
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
