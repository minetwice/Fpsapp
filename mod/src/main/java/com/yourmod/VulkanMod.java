package com.yourvulkanmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VulkanMod implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "vulkanmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("VulkanMod Loaded!");
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("VulkanMod Client Initialize...");
        // We'll add renderer initialization here later
    }
}
