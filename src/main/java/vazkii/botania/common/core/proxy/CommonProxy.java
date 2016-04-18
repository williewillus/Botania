/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 13, 2014, 7:45:37 PM (GMT)]
 */
package vazkii.botania.common.core.proxy;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Level;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.boss.IBotaniaBoss;
import vazkii.botania.api.lexicon.ITwoNamedPage;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.lexicon.LexiconPage;
import vazkii.botania.common.Botania;
import vazkii.botania.common.achievement.ModAchievements;
import vazkii.botania.common.block.ModBanners;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.ModMultiblocks;
import vazkii.botania.common.block.subtile.generating.SubTileNarslimmus;
import vazkii.botania.common.block.tile.TileLightRelay.EntityPlayerMover;
import vazkii.botania.common.block.tile.corporea.TileCorporeaIndex;
import vazkii.botania.common.brew.ModBrews;
import vazkii.botania.common.brew.ModPotions;
import vazkii.botania.common.core.command.CommandOpen;
import vazkii.botania.common.core.command.CommandShare;
import vazkii.botania.common.core.command.CommandSkyblockSpread;
import vazkii.botania.common.core.handler.BiomeDecorationHandler;
import vazkii.botania.common.core.handler.ChestGenHandler;
import vazkii.botania.common.core.handler.CommonTickHandler;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.handler.InternalMethodHandler;
import vazkii.botania.common.core.loot.LootHandler;
import vazkii.botania.common.core.handler.ManaNetworkHandler;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.core.handler.PixieHandler;
import vazkii.botania.common.core.handler.SheddingHandler;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.crafting.ModBrewRecipes;
import vazkii.botania.common.crafting.ModCraftingRecipes;
import vazkii.botania.common.crafting.ModElvenTradeRecipes;
import vazkii.botania.common.crafting.ModManaAlchemyRecipes;
import vazkii.botania.common.crafting.ModManaConjurationRecipes;
import vazkii.botania.common.crafting.ModManaInfusionRecipes;
import vazkii.botania.common.crafting.ModMigrationRecipes;
import vazkii.botania.common.crafting.ModPetalRecipes;
import vazkii.botania.common.crafting.ModPureDaisyRecipes;
import vazkii.botania.common.crafting.ModRuneRecipes;
import vazkii.botania.common.entity.EntityCorporeaSpark;
import vazkii.botania.common.entity.EntityDoppleganger;
import vazkii.botania.common.entity.EntityFlameRing;
import vazkii.botania.common.entity.EntityMagicLandmine;
import vazkii.botania.common.entity.EntityMagicMissile;
import vazkii.botania.common.entity.EntityManaBurst;
import vazkii.botania.common.entity.EntityPinkWither;
import vazkii.botania.common.entity.EntitySignalFlare;
import vazkii.botania.common.entity.EntitySpark;
import vazkii.botania.common.entity.ModEntities;
import vazkii.botania.common.integration.buildcraft.StatementAPIPlugin;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.common.network.GuiHandler;
import vazkii.botania.common.world.SkyblockWorldEvents;
import vazkii.botania.common.world.WorldTypeSkyblock;

//import vazkii.botania.common.block.ModBanners;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		BotaniaAPI.internalHandler = new InternalMethodHandler();

		ConfigHandler.loadConfig(event.getSuggestedConfigurationFile());

		ModSounds.init();
		ModBlocks.init();
		ModItems.init();
		ModEntities.init();
		ModPotions.init();
		ModBrews.init();

		ModAchievements.init();
		ModMultiblocks.init();
		ModBanners.init();

		ChestGenHandler.init();
		LootHandler.init();

		if(Botania.gardenOfGlassLoaded)
			new WorldTypeSkyblock();
	}

	public void init(FMLInitializationEvent event) {
		ModCraftingRecipes.init();
		ModMigrationRecipes.init();
		ModPetalRecipes.init();
		ModPureDaisyRecipes.init();
		ModRuneRecipes.init();
		ModManaAlchemyRecipes.init();
		ModManaConjurationRecipes.init();
		ModManaInfusionRecipes.init();
		ModElvenTradeRecipes.init();
		ModBrewRecipes.init();
		LexiconData.init();

		NetworkRegistry.INSTANCE.registerGuiHandler(Botania.instance, new GuiHandler());

		MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeDecorationHandler());
		MinecraftForge.EVENT_BUS.register(ManaNetworkHandler.instance);
		MinecraftForge.EVENT_BUS.register(new PixieHandler());
		MinecraftForge.EVENT_BUS.register(new SheddingHandler());
		MinecraftForge.EVENT_BUS.register(new SubTileNarslimmus.SpawnIntercepter());
		MinecraftForge.EVENT_BUS.register(TileCorporeaIndex.getInputHandler());

		if(Botania.gardenOfGlassLoaded)
			MinecraftForge.EVENT_BUS.register(new SkyblockWorldEvents());

		MinecraftForge.EVENT_BUS.register(new CommonTickHandler());

		FMLInterModComms.sendMessage("ProjectE", "interdictionblacklist", EntityManaBurst.class.getCanonicalName());

		if(Botania.bcTriggersLoaded)
			new StatementAPIPlugin();
	}

	public void postInit(FMLPostInitializationEvent event) {
		if(Botania.thaumcraftLoaded) {
			ModBrews.initTC();
			ModBrewRecipes.initTC();
		}

		ModBlocks.addDispenserBehaviours();
		ModBlocks.registerMultiparts();
		ConfigHandler.loadPostInit();
		LexiconData.postInit();

		int words = 0;
		for(LexiconEntry entry : BotaniaAPI.getAllEntries())
			for(LexiconPage page : entry.pages) {
				words += countWords(page.getUnlocalizedName());
				if(page instanceof ITwoNamedPage)
					words += countWords(((ITwoNamedPage) page).getSecondUnlocalizedName());
			}
		FMLLog.log(Level.INFO, "[Botania] The Lexica Botania has %d words.", words);

		registerDefaultEntityBlacklist();
	}

	private int countWords(String s) {
		String s1 = I18n.translateToLocal(s);
		return s1.split(" ").length;
	}

	private void registerDefaultEntityBlacklist() {
		// Vanilla
		BotaniaAPI.blacklistEntityFromGravityRod(EntityDragon.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityDragonPart.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityWither.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityItemFrame.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityEnderCrystal.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityPainting.class);

		// Botania
		BotaniaAPI.blacklistEntityFromGravityRod(EntityCorporeaSpark.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityDoppleganger.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityFlameRing.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityMagicLandmine.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityMagicMissile.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityManaBurst.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityPinkWither.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntitySignalFlare.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntitySpark.class);
		BotaniaAPI.blacklistEntityFromGravityRod(EntityPlayerMover.class);
	}

	// Overriding the internal method handler will break everything as it changes regularly.
	// So just don't be a moron and don't override it. Thanks.
	public void serverAboutToStart(FMLServerAboutToStartEvent event) {
		String clname = BotaniaAPI.internalHandler.getClass().getName();
		String expect = "vazkii.botania.common.core.handler.InternalMethodHandler";
		if(!clname.equals(expect)) {
			new IllegalAccessError("The Botania API internal method handler has been overriden. "
					+ "This will cause crashes and compatibility issues, and that's why it's marked as"
					+ " \"Do not Override\". Whoever had the brilliant idea of overriding it needs to go"
					+ " back to elementary school and learn to read. (Expected classname: " + expect + ", Actual classname: " + clname + ")").printStackTrace();
			FMLCommonHandler.instance().exitJava(1, true);
		}
	}

	public void serverStarting(FMLServerStartingEvent event) {
		// todo 1.8 - we don't have a file server yet. event.registerServerCommand(new CommandDownloadLatest());
		event.registerServerCommand(new CommandShare());
		event.registerServerCommand(new CommandOpen());
		if(Botania.gardenOfGlassLoaded)
			event.registerServerCommand(new CommandSkyblockSpread());
	}

	public void setEntryToOpen(LexiconEntry entry) {
		// NO-OP
	}

	public void setToTutorialIfFirstLaunch() {
		// NO-OP
	}

	public void setLexiconStack(ItemStack stack) {
		// NO-OP
	}

	public boolean isTheClientPlayer(EntityLivingBase entity) {
		return false;
	}

	public boolean isClientPlayerWearingMonocle() {
		return false;
	}

	public String getLastVersion() {
		return LibMisc.BUILD;
	}

	public void setExtraReach(EntityLivingBase entity, float reach) {
		if(entity instanceof EntityPlayerMP)
			((EntityPlayerMP) entity).interactionManager.setBlockReachDistance(Math.max(5, ((EntityPlayerMP) entity).interactionManager.getBlockReachDistance() + reach));
	}

	public boolean openWikiPage(World world, Block block, RayTraceResult pos) {
		return false;
	}

	public void playRecordClientSided(World world, BlockPos pos, ItemRecord record) {
		// NO-OP
	}

	public void setMultiblock(World world, int x, int y, int z, double radius, Block block) {
		// NO-OP
	}

	public void removeSextantMultiblock() {
		// NO-OP
	}

	public long getWorldElapsedTicks() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0].getTotalWorldTime();
	}

	public void setSparkleFXNoClip(boolean noclip) {
		// NO-OP
	}

	public void setSparkleFXCorrupt(boolean corrupt) {
		// NO-OP
	}

	public void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float size, int m) {
		sparkleFX(world, x, y, z, r, g, b, size, m, false);
	}

	public void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float size, int m, boolean fake) {
		// NO-OP
	}

	public void setWispFXDistanceLimit(boolean limit) {
		// NO-OP
	}

	public void setWispFXDepthTest(boolean depth) {
		// NO-OP
	}

	public void wispFX(World world, double x, double y, double z, float r, float g, float b, float size) {
		wispFX(world, x, y, z, r, g, b, size, 0F);
	}

	public void wispFX(World world, double x, double y, double z, float r, float g, float b, float size, float gravity) {
		wispFX(world, x, y, z, r, g, b, size, gravity, 1F);
	}

	public void wispFX(World world, double x, double y, double z, float r, float g, float b, float size, float gravity, float maxAgeMul) {
		wispFX(world, x, y, z, r, g, b, size, 0, -gravity, 0, maxAgeMul);
	}

	public void wispFX(World world, double x, double y, double z, float r, float g, float b, float size, float motionx, float motiony, float motionz) {
		wispFX(world, x, y, z, r, g, b, size, motionx, motiony, motionz, 1F);
	}

	public void wispFX(World world, double x, double y, double z, float r, float g, float b, float size, float motionx, float motiony, float motionz, float maxAgeMul) {
		// NO-OP
	}

	public void lightningFX(World world, Vector3 vectorStart, Vector3 vectorEnd, float ticksPerMeter, int colorOuter, int colorInner) {
		lightningFX(world, vectorStart, vectorEnd, ticksPerMeter, System.nanoTime(), colorOuter, colorInner);
	}

	public void lightningFX(World world, Vector3 vectorStart, Vector3 vectorEnd, float ticksPerMeter, long seed, int colorOuter, int colorInner) {
		// NO-OP
	}

	public void addBoss(IBotaniaBoss boss) {
		// NO-OP
	}

	public void removeBoss(IBotaniaBoss boss) {
		// NO-OP
	}

}
