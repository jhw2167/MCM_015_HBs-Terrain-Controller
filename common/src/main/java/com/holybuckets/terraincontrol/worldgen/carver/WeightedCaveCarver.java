package com.holybuckets.terraincontrol.worldgen.carver;

import com.holybuckets.terraincontrol.worldgen.TerrainControlWorldgen;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public class WeightedCaveCarver extends WorldCarver<WeightedCaveConfig> {

    private static final ResourceLocation FLAVOR_RANDOM_ID = TerrainControlWorldgen.id("cave_flavor_region");

    public WeightedCaveCarver(Codec<WeightedCaveConfig> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(WeightedCaveConfig config, RandomSource random) {
        return random.nextFloat() <= config.probability;
    }

    @Override
    public boolean carve(CarvingContext context, WeightedCaveConfig config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeAccessor, RandomSource random, Aquifer aquifer,
                         ChunkPos sourceChunk, CarvingMask carvingMask) {
        return selectFlavor(context, config, sourceChunk)
            .map(flavor -> flavor.value().carve(context, chunk, biomeAccessor, random, aquifer, sourceChunk, carvingMask))
            .orElse(false);
    }

    public Optional<Holder<ConfiguredWorldCarver<?>>> selectFlavor(CarvingContext context, WeightedCaveConfig config, ChunkPos sourceChunk) {
        int regionX = Math.floorDiv(sourceChunk.x, config.regionSizeChunks());
        int regionZ = Math.floorDiv(sourceChunk.z, config.regionSizeChunks());
        RandomSource regionRandom = context.randomState().getOrCreateRandomFactory(FLAVOR_RANDOM_ID).at(regionX, 0, regionZ);
        return config.flavors().getRandomValue(regionRandom);
    }
}
