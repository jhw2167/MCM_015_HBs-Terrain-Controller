package com.holybuckets.terraincontrol.worldgen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public class PitCarver extends WorldCarver<PitCarverConfiguration> {

    public PitCarver(Codec<PitCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(PitCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= config.probability;
    }

    @Override
    public boolean carve(CarvingContext context, PitCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeAccessor, RandomSource random, Aquifer aquifer,
                         ChunkPos sourceChunk, CarvingMask carvingMask) {
        return false;
    }

    protected boolean carveShaft(CarvingContext context, PitCarverConfiguration config, ChunkAccess chunk,
                                 Function<BlockPos, Holder<Biome>> biomeAccessor, Aquifer aquifer, CarvingMask carvingMask,
                                 double centerX, double topY, double centerZ, int depth, float radius, float taper) {
        return false;
    }

    protected double radiusAt(float radius, float taper, double progress) {
        return radius;
    }
}
