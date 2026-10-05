package com.holybuckets.terraincontrol.worldgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;

public class PitCarverConfiguration extends CarverConfiguration {

    public static final Codec<PitCarverConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CarverConfiguration.CODEC.forGetter(config -> config),
        FloatProvider.CODEC.fieldOf("radius").forGetter(config -> config.radius),
        IntProvider.CODEC.fieldOf("depth").forGetter(config -> config.depth),
        FloatProvider.CODEC.fieldOf("taper").forGetter(config -> config.taper)
    ).apply(instance, PitCarverConfiguration::new));

    public final FloatProvider radius;
    public final IntProvider depth;
    public final FloatProvider taper;

    public PitCarverConfiguration(CarverConfiguration base, FloatProvider radius, IntProvider depth, FloatProvider taper) {
        super(base.probability, base.y, base.yScale, base.lavaLevel, base.debugSettings, base.replaceable);
        this.radius = radius;
        this.depth = depth;
        this.taper = taper;
    }
}
