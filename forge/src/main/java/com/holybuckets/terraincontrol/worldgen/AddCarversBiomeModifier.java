package com.holybuckets.terraincontrol.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public record AddCarversBiomeModifier(HolderSet<Biome> biomes,
                                      HolderSet<ConfiguredWorldCarver<?>> carvers,
                                      GenerationStep.Carving step) implements BiomeModifier {

    public static final Codec<AddCarversBiomeModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddCarversBiomeModifier::biomes),
        ConfiguredWorldCarver.LIST_CODEC.fieldOf("carvers").forGetter(AddCarversBiomeModifier::carvers),
        GenerationStep.Carving.CODEC.optionalFieldOf("step", GenerationStep.Carving.AIR).forGetter(AddCarversBiomeModifier::step)
    ).apply(instance, AddCarversBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !biomes.contains(biome)) {
            return;
        }
        List<Holder<ConfiguredWorldCarver<?>>> target = builder.getGenerationSettings().getCarvers(step);
        carvers.forEach(carver -> {
            if (!target.contains(carver)) {
                target.add(carver);
            }
        });
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return ForgeWorldgen.ADD_CARVERS.get();
    }
}
