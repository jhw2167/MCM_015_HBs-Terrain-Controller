package com.holybuckets.terraincontrol;

import com.holybuckets.terraincontrol.client.CommonClassClient;
import com.holybuckets.terraincontrol.worldgen.ForgeWorldgen;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod( Constants.MOD_ID)
public class TerrainControlMainForge {

    public TerrainControlMainForge() {
        super();
        ForgeWorldgen.register(FMLJavaModLoadingContext.get().getModEventBus());
        Balm.initialize(Constants.MOD_ID, CommonClass::init);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> BalmClient.initialize(Constants.MOD_ID, CommonClassClient::initClient));
    }


}
