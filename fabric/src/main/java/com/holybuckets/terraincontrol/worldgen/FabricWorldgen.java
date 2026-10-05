package com.holybuckets.terraincontrol.worldgen;

import com.holybuckets.terraincontrol.worldgen.carver.ModCarvers;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.GenerationStep;

public final class FabricWorldgen {

    private FabricWorldgen() {
    }

    public static void init() {
        Registry.register(BuiltInRegistries.CARVER, ModCarvers.WEIGHTED_CAVE_ID, ModCarvers.WEIGHTED_CAVE);
        Registry.register(BuiltInRegistries.CARVER, ModCarvers.PIT_ID, ModCarvers.PIT);

        BiomeModifications.create(TerrainControlWorldgen.id("cave_flavors"))
            .add(ModificationPhase.ADDITIONS, BiomeSelectors.foundInOverworld(),
                context -> context.getGenerationSettings().addCarver(GenerationStep.Carving.AIR, ModCarvers.CAVE_FLAVORS));

        FabricLoader.getInstance().getModContainer(TerrainControlWorldgen.MOD_ID).ifPresent(container ->
            ResourceManagerHelper.registerBuiltinResourcePack(
                TerrainControlWorldgen.id(TerrainControlWorldgen.OVERRIDE_PACK_NAME),
                container,
                Component.literal("HBs Terrain Control Overrides"),
                ResourcePackActivationType.ALWAYS_ENABLED));
    }
}
