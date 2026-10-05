package com.holybuckets.terraincontrol.worldgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;

public class WeightedCaveConfig extends CarverConfiguration {

    public static final Codec<WeightedCaveConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(config -> config.probability),
        ExtraCodecs.POSITIVE_INT.optionalFieldOf("region_size_chunks", 16).forGetter(WeightedCaveConfig::regionSizeChunks),
        SimpleWeightedRandomList.wrappedCodec(ConfiguredWorldCarver.CODEC).fieldOf("flavors").forGetter(WeightedCaveConfig::flavors)
    ).apply(instance, WeightedCaveConfig::new));

    private final int regionSizeChunks;
    private final SimpleWeightedRandomList<Holder<ConfiguredWorldCarver<?>>> flavors;

    public WeightedCaveConfig(float probability, int regionSizeChunks, SimpleWeightedRandomList<Holder<ConfiguredWorldCarver<?>>> flavors) {
        super(probability,
            ConstantHeight.of(VerticalAnchor.absolute(0)),
            ConstantFloat.of(1.0F),
            VerticalAnchor.bottom(),
            CarverDebugSettings.DEFAULT,
            HolderSet.<Block>direct(List.of()));
        this.regionSizeChunks = regionSizeChunks;
        this.flavors = flavors;
    }

    public int regionSizeChunks() {
        return regionSizeChunks;
    }

    public SimpleWeightedRandomList<Holder<ConfiguredWorldCarver<?>>> flavors() {
        return flavors;
    }
}
