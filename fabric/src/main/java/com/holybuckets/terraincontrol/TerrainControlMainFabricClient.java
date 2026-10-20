package com.holybuckets.terraincontrol;

import com.holybuckets.terraincontrol.client.CommonClassClient;
import net.blay09.mods.balm.api.client.BalmClient;
import net.fabricmc.api.ClientModInitializer;


public class TerrainControlMainFabricClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        BalmClient.initialize(Constants.MOD_ID, CommonClassClient::initClient);
    }

}
