package com.holybuckets.terraincontrol.worldgen;

import com.holybuckets.terraincontrol.worldgen.carver.ModCarvers;
import com.holybuckets.terraincontrol.worldgen.carver.PitCarverConfiguration;
import com.holybuckets.terraincontrol.worldgen.carver.WeightedCaveConfig;
import com.mojang.serialization.Codec;
import java.nio.file.Path;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ForgeWorldgen {

    private static final DeferredRegister<WorldCarver<?>> CARVERS =
        DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, TerrainControlWorldgen.MOD_ID);
    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS =
        DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, TerrainControlWorldgen.MOD_ID);

    public static final RegistryObject<WorldCarver<WeightedCaveConfig>> WEIGHTED_CAVE =
        CARVERS.register(ModCarvers.WEIGHTED_CAVE_ID.getPath(), () -> ModCarvers.WEIGHTED_CAVE);
    public static final RegistryObject<WorldCarver<PitCarverConfiguration>> PIT =
        CARVERS.register(ModCarvers.PIT_ID.getPath(), () -> ModCarvers.PIT);
    public static final RegistryObject<Codec<AddCarversBiomeModifier>> ADD_CARVERS =
        BIOME_MODIFIERS.register("add_carvers", () -> AddCarversBiomeModifier.CODEC);

    private ForgeWorldgen() {
    }

    public static void register(IEventBus modBus) {
        CARVERS.register(modBus);
        BIOME_MODIFIERS.register(modBus);
        modBus.addListener(ForgeWorldgen::onAddPackFinders);
    }

    private static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        Path root = ModList.get().getModFileById(TerrainControlWorldgen.MOD_ID).getFile()
            .findResource(TerrainControlWorldgen.OVERRIDE_PACK_PATH);
        Pack.ResourcesSupplier resources = name -> new PathPackResources(name, root, true);
        Pack.Info info = Pack.readPackInfo(TerrainControlWorldgen.OVERRIDE_PACK_NAME, resources);
        if (info == null) {
            return;
        }
        Pack pack = Pack.create(
            "builtin/" + TerrainControlWorldgen.OVERRIDE_PACK_NAME,
            Component.literal("HBs Terrain Control Overrides"),
            true,
            resources,
            info,
            PackType.SERVER_DATA,
            Pack.Position.TOP,
            true,
            PackSource.BUILT_IN);
        event.addRepositorySource(consumer -> consumer.accept(pack));
    }
}
