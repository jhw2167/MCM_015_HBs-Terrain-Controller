package com.holybuckets.terraincontrol.worldgen;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.terraincontrol.LoggerProject;
import com.holybuckets.terraincontrol.core.CustomCaveFeatureManager;
import com.holybuckets.terraincontrol.mixin.SinglePoolElementAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;

public final class CaveStructureGuard {

    public static final String ENTRANCE_TAG = "/entrance";
    public static final String INTERIOR_TAG = "/interior";

    private CaveStructureGuard() {}

    public static boolean isIncompleteCave(Structure structure, StructureStart start)
    {
        if (start == null || !start.isValid()) return false;
        ResourceLocation id = HBUtil.LevelUtil.toStructureResourceLocation(structure);
        if (id == null || !CustomCaveFeatureManager.CAVE_NAMESPACE.equals(id.getNamespace())) return false;

        boolean entrance = false;
        boolean interior = false;
        for (StructurePiece piece : start.getPieces()) {
            String path = templatePath(piece);
            if (path == null) continue;
            if (path.contains(ENTRANCE_TAG)) entrance = true;
            else if (path.contains(INTERIOR_TAG)) interior = true;
        }
        if (entrance && interior) return false;

        LoggerProject.logInfo("015020", "Rejected incomplete cave at " + entrance);
        return true;
    }

    private static String templatePath(StructurePiece piece) {
        if (!(piece instanceof PoolElementStructurePiece poolPiece)) return null;
        if (!(poolPiece.getElement() instanceof SinglePoolElement single)) return null;
        return ((SinglePoolElementAccessor) single).hbs_terrain_control$getTemplate().left()
            .map(ResourceLocation::getPath).orElse(null);
    }
}
