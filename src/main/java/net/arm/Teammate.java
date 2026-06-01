package net.arm;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Teammate implements ModInitializer {
	public static final String MOD_ID = "kotateam";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		LOGGER.info("KotaTeam mod initialized. Developed by Akad4mka.");
	}
}