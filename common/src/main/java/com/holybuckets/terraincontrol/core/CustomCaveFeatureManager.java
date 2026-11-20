package com.holybuckets.terraincontrol.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.holybuckets.foundation.GeneralConfig;
import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.block.ModBlocks;
import com.holybuckets.foundation.datastore.DataStore;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.foundation.event.custom.DatastoreSaveEvent;
import com.holybuckets.foundation.event.custom.ServerTickEvent;
import com.holybuckets.foundation.event.custom.StructureLoadedEvent;
import com.holybuckets.foundation.event.custom.TickType;
import com.holybuckets.foundation.model.ManagedChunkUtility;
import com.holybuckets.foundation.structure.StructureAPI;
import com.holybuckets.foundation.structure.StructureInfo;
import com.holybuckets.foundation.structure.StructureManager;
import com.holybuckets.orecluster.core.OreClusterApi;
import com.holybuckets.terraincontrol.Constants;
import com.holybuckets.terraincontrol.LoggerProject;
import net.blay09.mods.balm.api.event.LevelLoadingEvent;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.blay09.mods.balm.api.event.server.ServerStoppedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.core.Vec3i;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomCaveFeatureManager {

    public enum CaveFeatureType { ANCIENT, ORE, RUBBLE, MONSTER_ROOM }

    public static final String CAVE_NAMESPACE = "hbs_caves";

    public static final float ANY_FEATURE_CHANCE = 0.33f;
    public static final float SURFACE_FEATURE_CHANCE = 1;// 0.33f;
    public static final float DEEP_FEATURE_CHANCE = 1; //0.33f;
    public static final float ANCIENT_FEATURE_CHANCE = 1; //0.25f;
    public static final float ORE_FEATURE_CHANCE = 1; //0.5f;
    public static final float RUBBLE_FEATURE_CHANCE = 1; //0.6f;
    public static final float MONSTER_ROOM_FEATURE_CHANCE = 1; //0.2f;

    public static final Map<CaveFeatureType, Float> FEATURE_WEIGHTS = Map.of(
        CaveFeatureType.ANCIENT, ANCIENT_FEATURE_CHANCE,
        CaveFeatureType.ORE, ORE_FEATURE_CHANCE,
        CaveFeatureType.RUBBLE, RUBBLE_FEATURE_CHANCE,
        CaveFeatureType.MONSTER_ROOM, MONSTER_ROOM_FEATURE_CHANCE
    );

    private static final List<CaveFeatureType> ALL_TYPES = List.of(CaveFeatureType.values());
    private static final List<CaveFeatureType> ALL_SURFACE = List.of(CaveFeatureType.ANCIENT, CaveFeatureType.RUBBLE, CaveFeatureType.MONSTER_ROOM);
    private static final Map<String, List<CaveFeatureType>> MARKER_FEATURE_NAMES = Map.of(
        "white", ALL_TYPES,
        "brown", ALL_SURFACE,
        "pink", ALL_SURFACE,
        "black", List.of(CaveFeatureType.ANCIENT, CaveFeatureType.ORE, CaveFeatureType.MONSTER_ROOM),
        "yellow", List.of(CaveFeatureType.ANCIENT),
        "orange", List.of(CaveFeatureType.RUBBLE),
        "red", List.of(CaveFeatureType.MONSTER_ROOM),
        "lime", List.of(CaveFeatureType.ORE)
    );
    private static final Map<String, Float> MARKER_CHANCE_NAMES = Map.of(
        "white", ANY_FEATURE_CHANCE,
        "brown", SURFACE_FEATURE_CHANCE,
        "pink", SURFACE_FEATURE_CHANCE,
        "black", DEEP_FEATURE_CHANCE,
        "yellow", ANCIENT_FEATURE_CHANCE,
        "orange", RUBBLE_FEATURE_CHANCE,
        "red", MONSTER_ROOM_FEATURE_CHANCE,
        "lime", ORE_FEATURE_CHANCE
    );

    public static final Map<CaveFeatureType, List<ResourceLocation>> FEATURE_TEMPLATES = Map.of(
        CaveFeatureType.ANCIENT, ids("features/ancient/", "ancient_altar", "ancient_camp", "ancient_cave_drawings",
            "ancient_den_1", "ancient_den_2", "ancient_scrap", "ancient_witch_camp"),
        CaveFeatureType.RUBBLE, ids("features/cave_rubble/", "badlands_rubble_1", "badlands_rubble_2", "burried_rubble_1",
            "burried_rubble_2", "cave_rubble_1", "cave_rubble_2", "cobweb_rubble_1", "cobweb_rubble_2", "desert_rubble_1",
            "desert_rubble_2", "hard_rubble_1", "hard_rubble_2", "jungle_rubble_1", "jungle_rubble_2", "spring_rubble_1",
            "spring_rubble_2", "wooden_rubble_1", "wooden_rubble_2"),
        CaveFeatureType.MONSTER_ROOM, ids("features/monster_room/", "monster_room_1", "monster_room_2", "monster_room_3",
            "monster_room_4"),
        CaveFeatureType.ORE, List.of()
    );

    public static final Map<CaveFeatureType, Integer> FEATURE_SINK = new HashMap<>();
    static {
        FEATURE_SINK.put(CaveFeatureType.ANCIENT, 1);
        FEATURE_SINK.put(CaveFeatureType.RUBBLE, 1);
        FEATURE_SINK.put(CaveFeatureType.MONSTER_ROOM, 0);
        FEATURE_SINK.put(CaveFeatureType.ORE, 1);
    }

    public static final Map<ResourceLocation, Rotation> FEATURE_FRONT_ROTATION = new HashMap<>();

    public static final String ORE_FEATURE_SHAPE = "MOUND";
    public static final HBUtil.TripleInt ORE_FEATURE_VOLUME = new HBUtil.TripleInt(5, 4, 5);
    public static final float ORE_FEATURE_DENSITY = 0.7f;
    public static final List<Block> ORE_FEATURE_BLOCKS = List.of(Blocks.COPPER_ORE, Blocks.IRON_ORE, Blocks.COAL_ORE);
    public static final Map<Block, Block> DEEPSLATE_ORES = Map.of(
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE
    );

    public static final Map<String, List<String>> FEATURE_BIOME_WHITELIST_NAMES = new HashMap<>();
    public static final Map<String, List<String>> FEATURE_BIOME_BLACKLIST_NAMES = new HashMap<>();
    static {
        List<String> desertAndBadlands = List.of("#hbs_terrain_control:has_structure/rubble_badlands", "#hbs_terrain_control:has_structure/rubble_desert");
        FEATURE_BIOME_WHITELIST_NAMES.put(CAVE_NAMESPACE + ":features/cave_rubble/badlands_rubble_1", desertAndBadlands);
        FEATURE_BIOME_WHITELIST_NAMES.put(CAVE_NAMESPACE + ":features/cave_rubble/badlands_rubble_2", desertAndBadlands);
    }

    private static final Map<String, Set<Biome>> featureBiomeWhitelist = new HashMap<>();
    private static final Map<String, Set<Biome>> featureBiomeBlacklist = new HashMap<>();

    private static final String PROCESSED_SAVE_KEY = "processedCaves";

    private static final Set<ChunkPos> PROCESSED = ConcurrentHashMap.newKeySet();

    private static final Map<Block, List<CaveFeatureType>> markers = new HashMap<>();
    private static final Map<Block, Float> markerChances = new HashMap<>();
    private static final Map<Block, Set<Block>> exclusiveMarkers = new HashMap<>();
    private static final Map<CaveFeatureType, List<StructureTemplate>> loadedTemplates = new EnumMap<>(CaveFeatureType.class);
    private static final Map<StructureTemplate, ResourceLocation> templateIds = new IdentityHashMap<>();

    private static GeneralConfig GENERAL_CONFIG;

    private CustomCaveFeatureManager() {}

    public static void init(EventRegistrar reg) {
        reg.registerOnServerTick(TickType.ON_120_TICKS, CustomCaveFeatureManager::onServerTick);
        reg.registerOnBeforeServerStarted(CustomCaveFeatureManager::onServerStarted);
        reg.registerOnStructureLoaded(CustomCaveFeatureManager::onStructureLoad);
        reg.registerOnDataSave(CustomCaveFeatureManager::onDataSave);
        reg.registerOnLevelLoad(CustomCaveFeatureManager::loadProcessed);
        reg.registerOnServerStopped(CustomCaveFeatureManager::onServerStopped);
    }

    private static void onServerStarted(ServerStartingEvent event)
    {

        GENERAL_CONFIG = GeneralConfig.getInstance();

        markers.clear();
        markerChances.clear();
        exclusiveMarkers.clear();


        for(Map.Entry<String, List<CaveFeatureType>> e : MARKER_FEATURE_NAMES.entrySet())
        {
            String color = e.getKey();
            List<CaveFeatureType> types = e.getValue();
            markers.put(marker(color), types);
            markerChances.put(marker(color), MARKER_CHANCE_NAMES.get(color));
        }

        Set<Block> E1 = Set.of(marker("brown"), marker("pink"));
        exclusiveMarkers.put(marker("brown"), E1);
        exclusiveMarkers.put(marker("pink"), E1);

        loadedTemplates.clear();
        templateIds.clear();
        StructureTemplateManager manager = event.getServer().getStructureManager();
        for (Map.Entry<CaveFeatureType, List<ResourceLocation>> e : FEATURE_TEMPLATES.entrySet())
        {
            List<StructureTemplate> list = new ArrayList<>();
            for (ResourceLocation id : e.getValue()) {
                Optional<StructureTemplate> t = manager.get(id);
                if (t.isPresent()) {
                    list.add(t.get());
                    templateIds.put(t.get(), id);
                } else {
                    LoggerProject.logWarning("015001", "Missing cave feature template " + id);
                }
            }
            loadedTemplates.put(e.getKey(), list);
        }
        featureBiomeWhitelist.clear();
        featureBiomeBlacklist.clear();
        resolveBiomeLists(FEATURE_BIOME_WHITELIST_NAMES, featureBiomeWhitelist);
        resolveBiomeLists(FEATURE_BIOME_BLACKLIST_NAMES, featureBiomeBlacklist);
        LoggerProject.logInfo("015000", "Cave features ready: markers=" + markers.size()
            + " whitelists=" + featureBiomeWhitelist.size() + " blacklists=" + featureBiomeBlacklist.size());
    }

    private static void resolveBiomeLists(Map<String, List<String>> names, Map<String, Set<Biome>> resolved)
    {
        for (Map.Entry<String, List<String>> e : names.entrySet()) {
            if (e.getValue() == null) continue;
            Set<Biome> biomes = new HashSet<>(HBUtil.LevelUtil.getBiomesFromNamesOrTag(e.getValue()));
            if (biomes.isEmpty()) LoggerProject.logWarning("015019", "No biomes resolved for cave feature " + e.getKey());
            resolved.put(e.getKey(), biomes);
        }
    }

    private static void loadProcessed(LevelLoadingEvent.Load event)
    {
        if(!HBUtil.LevelUtil.testLevel((Level) event.getLevel(), GeneralConfig.OVERWORLD_LOC)) return;
        PROCESSED.clear();
        DataStore ds = GeneralConfig.getInstance().getDataStore();
        if (ds == null) return;
        JsonElement saved = ds.getOrCreateLevelSaveData(Constants.MOD_ID, (Level) event.getLevel()).get(PROCESSED_SAVE_KEY);
        if (saved == null || !saved.isJsonArray()) return;
        for (JsonElement e : saved.getAsJsonArray()) PROCESSED.add(new ChunkPos(e.getAsLong()));
        LoggerProject.logInfo("015004", "Restored " + PROCESSED.size() + " processed caves");
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        PROCESSED.clear();
    }

    private static void onDataSave(DatastoreSaveEvent event)
    {
        DataStore ds = event.getDataStore();
        if (ds == null || GeneralConfig.OVERWORLD == null) return;
        JsonArray processed = new JsonArray();
        for (ChunkPos cp : PROCESSED) processed.add(cp.toLong());
        ds.getOrCreateLevelSaveData(Constants.MOD_ID, GeneralConfig.OVERWORLD).addProperty(PROCESSED_SAVE_KEY, processed);
    }

    public static void onStructureLoad(StructureLoadedEvent event) {
        StructureInfo info = event.getStructureInfo();
        if(!isCaveStructure(info)) return;
        ChunkPos chunkPos = new ChunkPos(info.getOrigin());
        if(PROCESSED.contains(chunkPos)) return;
        LoggerProject.logInfo("015006", "Cave " + info.getStructureLocation() + " at " + info.getOrigin()
            + " loaded unprocessed, firstLoad=" + event.isFirstTimeLoaded());
    }

    private static boolean isCaveStructure(StructureInfo info)
    {
        if(info == null || info.isPseudo() || info.getOrigin() == null) return false;
        ResourceLocation loc = info.getStructureLocation();
        return CAVE_NAMESPACE.equals(loc.getNamespace());
    }


    private static void onServerTick(ServerTickEvent event)
    {
        ServerLevel level = GeneralConfig.OVERWORLD;
        if (level == null) return;
        StructureAPI api = StructureAPI.get(level);
        if (api == null) return;

        for (BlockPos pos : StructureManager.get(level).getCurrentlyLoadedStructurePositions())
        {
            StructureInfo info = api.structureAt(pos);
            if (!isCaveStructure(info)) continue;
            ChunkPos chunkPos = new ChunkPos(info.getOrigin());
            if (PROCESSED.contains(chunkPos)) continue;
            StructureStart start = api.loadedStructureStart(info);
            if (!isReadyToProcess(level, chunkPos, start)) continue;

            LoggerProject.logInfo("015011", "Processing cave " + info.getStructureLocation() + " at " + info.getOrigin() + " box=" + start.getBoundingBox());
            addCustomCaveFeatures(level, chunkPos, start);
            PROCESSED.add(chunkPos);
            LoggerProject.logInfo("015012", "Finished cave at " + info.getOrigin() + ", processed=" + PROCESSED.size());
            return;
        }
    }

    private static boolean isReadyToProcess(ServerLevel level, ChunkPos chunkPos, StructureStart start) {
        if (start == null || !start.isValid()) return false;
        if (!ManagedChunkUtility.isChunkFullyLoaded(level, HBUtil.ChunkUtil.getId(chunkPos))) return false;
        return allChunksLoaded(level, start.getBoundingBox());
    }

    private static boolean allChunksLoaded(ServerLevel level, BoundingBox box) {
        ManagedChunkUtility chunkUtil = ManagedChunkUtility.getInstance(level);
        BlockPos center = box.getCenter();
        for( ChunkPos cp : HBUtil.ChunkUtil.getLocalChunkPos(new ChunkPos(center), 3) ) {
            if (!chunkUtil.isLoaded(cp)) return false;
        }
        return true;
    }

    public static void addCustomCaveFeatures(ServerLevel level, ChunkPos chunkPos, StructureStart start)
    {

        Map<BlockPos, BlockState> markers = findMarkers(level, start.getBoundingBox());
        LoggerProject.logInfo("015013", "Cave at " + chunkPos + ": found " + markers.size() + " markers in " + start.getBoundingBox());
        Random random = ManagedChunkUtility.getChunkRandom(chunkPos);
        if (markers.isEmpty()) return;

        var removalSet = resolveExclusiveMarkers(markers, random);
        for (BlockPos pos : markers.keySet()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE );
        removalSet.forEach(markers::remove);
        LoggerProject.logInfo("015014", "Cave at " + chunkPos + ": cleared markers, " + removalSet.size() + " removed as exclusive, " + markers.size() + " remain");

        int placed = 0;
        for (Map.Entry<BlockPos, BlockState> e : markers.entrySet()) {
            BlockPos pos = e.getKey();
            BlockState state = e.getValue();

            String markerName = HBUtil.BlockUtil.blockToString(state.getBlock());
            Float chance = markerChances.get(state.getBlock());
            if (chance == null || random.nextFloat() >= chance) {
                LoggerProject.logInfo("015015", "Marker " + markerName + " at " + pos + " skipped, chance=" + chance);
                continue;
            }

            Biome biome = HBUtil.LevelUtil.getBiomeAtSurface(level, pos).value();
            CaveFeatureType type = selectType(CustomCaveFeatureManager.markers.get(state.getBlock()), biome, random);
            if (type == null) {
                LoggerProject.logInfo("015016", "Marker " + markerName + " at " + pos + " has no feature type valid in "
                    + HBUtil.LevelUtil.toBiomeResourceLocation(biome));
                continue;
            }

            if (placeFeature(level, type, pos, facingOf(state), biome, random)) placed++;
        }
        LoggerProject.logInfo("015003", "Cave at " + chunkPos + ": markers=" + markers.size() + " placed=" + placed);
    }

    private static Map<BlockPos, BlockState> findMarkers(ServerLevel level, BoundingBox box)
    {
        int halfXSpan = box.getXSpan()/2 + 1;
        int halfZSpan = box.getZSpan()/2 + 1;
        BlockPos center = box.getCenter();
        Map<BlockPos, BlockState> found = new LinkedHashMap<>();
        for (int cx = center.getX()-halfXSpan; cx <= center.getX()+halfXSpan; cx++ ) {
            for (int cz = center.getZ()-halfZSpan; cz <= center.getZ()+halfZSpan; cz++ ) {
                for (int y = box.minY(); y <= box.maxY(); y++) {
                    BlockPos pos = new BlockPos(cx, y, cz);
                    BlockState state = level.getBlockState(pos);
                    if(markers.containsKey(state.getBlock())) found.put(pos, state);
                }
            }
        }
        return found;
    }

    private static Set<BlockPos> resolveExclusiveMarkers(Map<BlockPos, BlockState> discovered, Random rand)
    {
        Set<BlockPos> removals = new HashSet<>();
        Set<Block> present = new HashSet<>();
        for(BlockState bs : discovered.values()) present.add(bs.getBlock());

        //once per cave, keep one block of each exclusive group and remove every marker of the others
        for( Set<Block> group : new HashSet<>(exclusiveMarkers.values()) )
        {
            List<Block> candidates = new ArrayList<>(group);
            candidates.retainAll(present);
            if(candidates.size() < 2) continue;
            candidates.sort(Comparator.comparing(HBUtil.BlockUtil::blockToString));
            Block keep = candidates.get(rand.nextInt(candidates.size()));
            for(var e : discovered.entrySet()) {
                Block b = e.getValue().getBlock();
                if(group.contains(b) && b != keep) removals.add(e.getKey());
            }
        }

        return removals;
    }

    private static CaveFeatureType selectType(List<CaveFeatureType> allowed, Biome biome, Random random)
    {
        if (allowed == null) return null;
        float total = 0;
        for (CaveFeatureType t : allowed)
            if (hasFeatures(t, biome)) total += FEATURE_WEIGHTS.get(t);
        if (total <= 0) return null;

        float roll = random.nextFloat() * total;
        CaveFeatureType last = null;
        for (CaveFeatureType t : allowed) {
            if (!hasFeatures(t, biome)) continue;
            last = t;
            roll -= FEATURE_WEIGHTS.get(t);
            if (roll < 0) return t;
        }
        return last;
    }

    private static boolean hasFeatures(CaveFeatureType type, Biome biome) {
        return type == CaveFeatureType.ORE || !validTemplates(type, biome).isEmpty();
    }

    private static List<StructureTemplate> validTemplates(CaveFeatureType type, Biome biome) {
        List<StructureTemplate> valid = new ArrayList<>();
        for (StructureTemplate t : loadedTemplates.getOrDefault(type, List.of())) {
            if (isValidInBiome(templateIds.get(t), biome)) valid.add(t);
        }
        return valid;
    }

    private static boolean isValidInBiome(ResourceLocation featureId, Biome biome) {
        if (featureId == null) return true;
        String id = featureId.toString();
        Set<Biome> whitelist = featureBiomeWhitelist.get(id);
        if (whitelist != null && !whitelist.contains(biome)) return false;
        Set<Biome> blacklist = featureBiomeBlacklist.get(id);
        return blacklist == null || !blacklist.contains(biome);
    }

    private static boolean placeFeature(ServerLevel level, CaveFeatureType type, BlockPos markerPos, Direction facing, Biome biome, Random random)
    {
        if (type == CaveFeatureType.ORE) return placeOreFeature(level, markerPos, random);
        List<StructureTemplate> candidates = validTemplates(type, biome);
        if (candidates.isEmpty()) return false;
        StructureTemplate template = candidates.get(random.nextInt(candidates.size()));
        ResourceLocation id = templateIds.get(template);

        Rotation rotation = rotationFor(facing).getRotated(FEATURE_FRONT_ROTATION.getOrDefault(id, Rotation.NONE));
        Vec3i size = template.getSize();
        BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
        int sink = FEATURE_SINK.getOrDefault(type, 0);
        BlockPos origin = markerPos.offset(-pivot.getX(), -sink, -pivot.getZ());

        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setRotationPivot(pivot)
            .setIgnoreEntities(false)
            .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);

        boolean result = template.placeInWorld(level, origin, origin, settings, level.getRandom(), Block.UPDATE_CLIENTS);
        LoggerProject.logInfo("015017", "Placed " + type + " " + id + " at " + origin + " rotation=" + rotation + " success=" + result);
        return result;
    }

    private static boolean placeOreFeature(ServerLevel level, BlockPos markerPos, Random random)
    {
        BlockState filler = level.getBlockState(markerPos.below());
        if (filler.isAir() || !filler.getFluidState().isEmpty() || filler.hasBlockEntity()) filler = Blocks.STONE.defaultBlockState();

        Block ore = ORE_FEATURE_BLOCKS.get(random.nextInt(ORE_FEATURE_BLOCKS.size()));
        if (filler.is(Blocks.DEEPSLATE) || filler.is(Blocks.TUFF)) ore = DEEPSLATE_ORES.get(ore);

        BlockPos source = markerPos.below(FEATURE_SINK.getOrDefault(CaveFeatureType.ORE, 0));
        List<Pair<BlockState, BlockPos>> cluster = OreClusterApi.generateStandaloneCluster(source, ORE_FEATURE_SHAPE,
            ORE_FEATURE_VOLUME, ore.defaultBlockState(), filler, ORE_FEATURE_DENSITY, random);

        int placed = 0;
        for (Pair<BlockState, BlockPos> p : cluster) {
            BlockState current = level.getBlockState(p.getRight());
            if (current.hasBlockEntity() || current.getDestroySpeed(level, p.getRight()) < 0) continue;
            level.setBlock(p.getRight(), p.getLeft(), Block.UPDATE_CLIENTS);
            placed++;
        }
        LoggerProject.logInfo("015018", "Placed ORE " + ore + " at " + source + ", blocks=" + placed + "/" + cluster.size());
        return placed > 0;
    }

    private static Rotation rotationFor(Direction facing) {
        return switch (facing) {
            case WEST -> Rotation.CLOCKWISE_90;
            case NORTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private static Direction facingOf(BlockState state) {
        return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    private static Block marker(String color) {
        switch (color) {
            case "white": return ModBlocks.getMarker("white");
            case "brown": return ModBlocks.getMarker("brown");
            case "pink": return ModBlocks.getMarker("pink");
            case "black": return ModBlocks.getMarker("black");
            case "yellow": return ModBlocks.getMarker("yellow");
            case "orange": return ModBlocks.getMarker("orange");
            case "red": return ModBlocks.getMarker("red");
            case "lime": return ModBlocks.getMarker("lime");
            default: return null;
        }
    }

    private static int chebyshev(BlockPos a, BlockPos b) {
        return Math.max(Math.abs(a.getX() - b.getX()), Math.max(Math.abs(a.getY() - b.getY()), Math.abs(a.getZ() - b.getZ())));
    }

    private static BoundingBox union(BoundingBox a, BoundingBox b) {
        return new BoundingBox(Math.min(a.minX(), b.minX()), Math.min(a.minY(), b.minY()), Math.min(a.minZ(), b.minZ()),
            Math.max(a.maxX(), b.maxX()), Math.max(a.maxY(), b.maxY()), Math.max(a.maxZ(), b.maxZ()));
    }

    private static List<ResourceLocation> ids(String prefix, String... names) {
        List<ResourceLocation> list = new ArrayList<>();
        for (String n : names) list.add(new ResourceLocation(CAVE_NAMESPACE, prefix + n));
        return List.copyOf(list);
    }


    public static GeneralConfig getGeneralConfig() {
        return GENERAL_CONFIG;
    }
}
