/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 13, 2014, 7:46:05 PM (GMT)]
 */
package vazkii.botania.client.core.proxy;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.common.MinecraftForge;
import vazkii.botania.api.item.IExtendedPlayerController;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.lexicon.multiblock.Multiblock;
import vazkii.botania.api.lexicon.multiblock.MultiblockSet;
import vazkii.botania.api.lexicon.multiblock.component.AnyComponent;
import vazkii.botania.api.wiki.IWikiProvider;
import vazkii.botania.api.wiki.WikiHooks;
import vazkii.botania.client.challenge.ModChallenges;
import vazkii.botania.client.core.handler.BaubleRenderHandler;
import vazkii.botania.client.core.handler.BotaniaPlayerController;
import vazkii.botania.client.core.handler.BoundTileRenderer;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.ContributorFancinessHandler;
import vazkii.botania.client.core.handler.CorporeaAutoCompleteHandler;
import vazkii.botania.client.core.handler.DebugHandler;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.client.core.handler.LightningHandler;
import vazkii.botania.client.core.handler.ModelHandler;
import vazkii.botania.client.core.handler.MultiblockRenderHandler;
import vazkii.botania.client.core.handler.PersistentVariableHelper;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.handler.SubTileRadiusRenderHandler;
import vazkii.botania.client.core.handler.TooltipHandler;
import vazkii.botania.client.core.helper.ShaderHelper;
import vazkii.botania.client.fx.FXSparkle;
import vazkii.botania.client.fx.FXWisp;
import vazkii.botania.client.gui.lexicon.GuiLexicon;
import vazkii.botania.client.gui.lexicon.GuiLexiconEntry;
import vazkii.botania.client.gui.lexicon.GuiLexiconIndex;
// todo 1.8.8 restore when nei updates import vazkii.botania.client.integration.nei.NEIGuiHooks;
import vazkii.botania.client.render.entity.RenderBabylonWeapon;
import vazkii.botania.client.render.entity.RenderCorporeaSpark;
import vazkii.botania.client.render.entity.RenderDoppleganger;
import vazkii.botania.client.render.entity.RenderManaStorm;
import vazkii.botania.client.render.entity.RenderPinkWither;
import vazkii.botania.client.render.entity.RenderPixie;
import vazkii.botania.client.render.entity.RenderPoolMinecart;
import vazkii.botania.client.render.entity.RenderSpark;
import vazkii.botania.client.render.entity.RenderThornChakram;
import vazkii.botania.client.render.tile.RenderTileAlfPortal;
import vazkii.botania.client.render.tile.RenderTileAltar;
import vazkii.botania.client.render.tile.RenderTileAvatar;
import vazkii.botania.client.render.tile.RenderTileBellows;
import vazkii.botania.client.render.tile.RenderTileBrewery;
import vazkii.botania.client.render.tile.RenderTileCocoon;
import vazkii.botania.client.render.tile.RenderTileCorporeaCrystalCube;
import vazkii.botania.client.render.tile.RenderTileCorporeaIndex;
import vazkii.botania.client.render.tile.RenderTileEnchanter;
import vazkii.botania.client.render.tile.RenderTileFloatingFlower;
import vazkii.botania.client.render.tile.RenderTileHourglass;
import vazkii.botania.client.render.tile.RenderTileIncensePlate;
import vazkii.botania.client.render.tile.RenderTileLightRelay;
import vazkii.botania.client.render.tile.RenderTilePool;
import vazkii.botania.client.render.tile.RenderTilePrism;
import vazkii.botania.client.render.tile.RenderTilePump;
import vazkii.botania.client.render.tile.RenderTilePylon;
import vazkii.botania.client.render.tile.RenderTileRedString;
import vazkii.botania.client.render.tile.RenderTileRuneAltar;
import vazkii.botania.client.render.tile.RenderTileSkullOverride;
import vazkii.botania.client.render.tile.RenderTileSparkChanger;
import vazkii.botania.client.render.tile.RenderTileSpreader;
import vazkii.botania.client.render.tile.RenderTileStarfield;
import vazkii.botania.client.render.tile.RenderTileTerraPlate;
import vazkii.botania.client.render.tile.RenderTileTeruTeruBozu;
import vazkii.botania.client.render.tile.RenderTileTinyPotato;
import vazkii.botania.client.render.world.SkyblockRenderEvents;
import vazkii.botania.common.block.tile.TileAlfPortal;
import vazkii.botania.common.block.tile.TileAltar;
import vazkii.botania.common.block.tile.TileAvatar;
import vazkii.botania.common.block.tile.TileBrewery;
import vazkii.botania.common.block.tile.TileCocoon;
import vazkii.botania.common.block.tile.TileEnchanter;
import vazkii.botania.common.block.tile.TileFloatingFlower;
import vazkii.botania.common.block.tile.TileFloatingSpecialFlower;
import vazkii.botania.common.block.tile.TileGaiaHead;
import vazkii.botania.common.block.tile.TileHourglass;
import vazkii.botania.common.block.tile.TileIncensePlate;
import vazkii.botania.common.block.tile.TileLightRelay;
import vazkii.botania.common.block.tile.TilePylon;
import vazkii.botania.common.block.tile.TileRuneAltar;
import vazkii.botania.common.block.tile.TileSparkChanger;
import vazkii.botania.common.block.tile.TileStarfield;
import vazkii.botania.common.block.tile.TileTerraPlate;
import vazkii.botania.common.block.tile.TileTeruTeruBozu;
import vazkii.botania.common.block.tile.TileTinyPotato;
import vazkii.botania.common.block.tile.corporea.TileCorporeaCrystalCube;
import vazkii.botania.common.block.tile.corporea.TileCorporeaIndex;
import vazkii.botania.common.block.tile.mana.TileBellows;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.block.tile.mana.TilePrism;
import vazkii.botania.common.block.tile.mana.TilePump;
import vazkii.botania.common.block.tile.mana.TileSpreader;
import vazkii.botania.common.block.tile.string.TileRedString;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.helper.MathHelper;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.core.proxy.CommonProxy;
import vazkii.botania.common.core.version.AdaptorNotifier;
import vazkii.botania.common.core.version.VersionChecker;
import vazkii.botania.common.entity.EntityBabylonWeapon;
import vazkii.botania.common.entity.EntityCorporeaSpark;
import vazkii.botania.common.entity.EntityDoppleganger;
import vazkii.botania.common.entity.EntityEnderAirBottle;
import vazkii.botania.common.entity.EntityManaStorm;
import vazkii.botania.common.entity.EntityPinkWither;
import vazkii.botania.common.entity.EntityPixie;
import vazkii.botania.common.entity.EntityPoolMinecart;
import vazkii.botania.common.entity.EntitySpark;
import vazkii.botania.common.entity.EntityThornChakram;
import vazkii.botania.common.entity.EntityVineBall;
import vazkii.botania.common.item.ItemSextant.MultiblockSextant;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.bauble.ItemMonocle;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibObfuscation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ClientProxy extends CommonProxy {

	public static boolean jingleTheBells = false;
	public static boolean dootDoot = false;

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		PersistentVariableHelper.setCacheFile(new File(Minecraft.getMinecraft().mcDataDir, "BotaniaVars.dat"));
		try {
			PersistentVariableHelper.load();
			PersistentVariableHelper.save();
		} catch (IOException e) {
			FMLLog.severe("Botania's persistent Variables couldn't load!");
			e.printStackTrace();
		}

		super.preInit(event);

		MinecraftForge.EVENT_BUS.register(MiscellaneousIcons.INSTANCE);
		ModelHandler.registerModels();
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);

		ModChallenges.init();

		MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
		MinecraftForge.EVENT_BUS.register(new HUDHandler());
		MinecraftForge.EVENT_BUS.register(new LightningHandler());
		if(ConfigHandler.boundBlockWireframe)
			MinecraftForge.EVENT_BUS.register(new BoundTileRenderer());
		MinecraftForge.EVENT_BUS.register(new TooltipHandler());
		MinecraftForge.EVENT_BUS.register(new BaubleRenderHandler());
		MinecraftForge.EVENT_BUS.register(new DebugHandler());
		MinecraftForge.EVENT_BUS.register(new SubTileRadiusRenderHandler());
		MinecraftForge.EVENT_BUS.register(new MultiblockRenderHandler());
		MinecraftForge.EVENT_BUS.register(new SkyblockRenderEvents());
		MinecraftForge.EVENT_BUS.register(new CorporeaAutoCompleteHandler());
		
		if(ConfigHandler.useAdaptativeConfig)
			MinecraftForge.EVENT_BUS.register(new AdaptorNotifier());
		if(ConfigHandler.versionCheckEnabled)
			new VersionChecker().init();

		if(ConfigHandler.enableSeasonalFeatures) {
			Calendar calendar = Calendar.getInstance();
			if((calendar.get(2) == 11 && calendar.get(5) >= 16) || (calendar.get(2) == 0 && calendar.get(5) <= 2))
				jingleTheBells = true;
			if(calendar.get(2) == 9)
				dootDoot = true;
		}

		initRenderers();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		CorporeaAutoCompleteHandler.updateItemList();
	}

	private void initRenderers() {
		RenderTileFloatingFlower renderTileFloatingFlower = new RenderTileFloatingFlower();
		ClientRegistry.bindTileEntitySpecialRenderer(TileAltar.class, new RenderTileAltar());
		ClientRegistry.bindTileEntitySpecialRenderer(TileSpreader.class, new RenderTileSpreader());
		ClientRegistry.bindTileEntitySpecialRenderer(TilePool.class, new RenderTilePool());
		ClientRegistry.bindTileEntitySpecialRenderer(TileRuneAltar.class, new RenderTileRuneAltar());
		ClientRegistry.bindTileEntitySpecialRenderer(TilePylon.class, new RenderTilePylon());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEnchanter.class, new RenderTileEnchanter());
		ClientRegistry.bindTileEntitySpecialRenderer(TileAlfPortal.class, new RenderTileAlfPortal());
		ClientRegistry.bindTileEntitySpecialRenderer(TileFloatingFlower.class, renderTileFloatingFlower);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFloatingSpecialFlower.class, renderTileFloatingFlower);
		ClientRegistry.bindTileEntitySpecialRenderer(TileTinyPotato.class, new RenderTileTinyPotato());
		// ClientRegistry.bindTileEntitySpecialRenderer(TileSpawnerClaw.class, new RenderTileSpawnerClaw());
		ClientRegistry.bindTileEntitySpecialRenderer(TileStarfield.class, new RenderTileStarfield());
		ClientRegistry.bindTileEntitySpecialRenderer(TileBrewery.class, new RenderTileBrewery());
		ClientRegistry.bindTileEntitySpecialRenderer(TileTerraPlate.class, new RenderTileTerraPlate());
		ClientRegistry.bindTileEntitySpecialRenderer(TileRedString.class, new RenderTileRedString());
		ClientRegistry.bindTileEntitySpecialRenderer(TilePrism.class, new RenderTilePrism());
		ClientRegistry.bindTileEntitySpecialRenderer(TileCorporeaIndex.class, new RenderTileCorporeaIndex());
		ClientRegistry.bindTileEntitySpecialRenderer(TilePump.class, new RenderTilePump());
		ClientRegistry.bindTileEntitySpecialRenderer(TileCorporeaCrystalCube.class, new RenderTileCorporeaCrystalCube());
		ClientRegistry.bindTileEntitySpecialRenderer(TileIncensePlate.class, new RenderTileIncensePlate());
		ClientRegistry.bindTileEntitySpecialRenderer(TileHourglass.class, new RenderTileHourglass());
		ClientRegistry.bindTileEntitySpecialRenderer(TileSparkChanger.class, new RenderTileSparkChanger());
		ClientRegistry.bindTileEntitySpecialRenderer(TileCocoon.class, new RenderTileCocoon());
		ClientRegistry.bindTileEntitySpecialRenderer(TileLightRelay.class, new RenderTileLightRelay());
		ClientRegistry.bindTileEntitySpecialRenderer(TileBellows.class, new RenderTileBellows());
		ClientRegistry.bindTileEntitySpecialRenderer(TileGaiaHead.class, new RenderTileSkullOverride());
		ClientRegistry.bindTileEntitySpecialRenderer(TileTeruTeruBozu.class, new RenderTileTeruTeruBozu());
		ClientRegistry.bindTileEntitySpecialRenderer(TileAvatar.class, new RenderTileAvatar());

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySkull.class, new RenderTileSkullOverride());

		RenderingRegistry.registerEntityRenderingHandler(EntityPixie.class, new RenderPixie());
		RenderingRegistry.registerEntityRenderingHandler(EntityVineBall.class, new RenderSnowball<EntityVineBall>(Minecraft.getMinecraft().getRenderManager(), ModItems.vineBall, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDoppleganger.class, new RenderDoppleganger(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpark.class, new RenderSpark(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityThornChakram.class, new RenderThornChakram(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityCorporeaSpark.class, new RenderCorporeaSpark(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityEnderAirBottle.class, new RenderSnowball<EntityEnderAirBottle>(Minecraft.getMinecraft().getRenderManager(), ModItems.manaResource, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityPoolMinecart.class, new RenderPoolMinecart(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityPinkWither.class, new RenderPinkWither(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityManaStorm.class, new RenderManaStorm(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityBabylonWeapon.class, new RenderBabylonWeapon(Minecraft.getMinecraft().getRenderManager()));

		ShaderHelper.initShaders();

		Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
		RenderPlayer render = skinMap.get("default");
		render.addLayer(new ContributorFancinessHandler());
		render = skinMap.get("slim");
		render.addLayer(new ContributorFancinessHandler());
	}

	@Override
	@Optional.Method(modid = "NotEnoughItems")
	public void registerNEIStuff() {
		// todo 1.8.8 restore when nei updates  NEIGuiHooks.init();
	}

	@Override
	public void setEntryToOpen(LexiconEntry entry) {
		GuiLexicon.currentOpenLexicon = new GuiLexiconEntry(entry, new GuiLexiconIndex(entry.category));
	}

	@Override
	public void setToTutorialIfFirstLaunch() {
		if(PersistentVariableHelper.firstLoad)
			GuiLexicon.currentOpenLexicon = new GuiLexiconEntry(LexiconData.welcome, new GuiLexiconEntry(LexiconData.tutorial, new GuiLexicon())).setFirstEntry();
	}

	@Override
	public void setLexiconStack(ItemStack stack) {
		GuiLexicon.stackUsed = stack;
	}

	@Override
	public boolean isTheClientPlayer(EntityLivingBase entity) {
		return entity == Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public boolean isClientPlayerWearingMonocle() {
		return ItemMonocle.hasMonocle(Minecraft.getMinecraft().thePlayer);
	}

	@Override
	public void setExtraReach(EntityLivingBase entity, float reach) {
		super.setExtraReach(entity, reach);
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		if(entity == player) {
			if(!(mc.playerController instanceof IExtendedPlayerController)) {
				GameType type = ReflectionHelper.getPrivateValue(PlayerControllerMP.class, mc.playerController, LibObfuscation.CURRENT_GAME_TYPE);
				NetHandlerPlayClient net = ReflectionHelper.getPrivateValue(PlayerControllerMP.class, mc.playerController, LibObfuscation.NET_CLIENT_HANDLER);
				BotaniaPlayerController controller = new BotaniaPlayerController(mc, net);
				boolean isFlying = player.capabilities.isFlying;
				boolean allowFlying = player.capabilities.allowFlying;
				controller.setGameType(type);
				player.capabilities.isFlying = isFlying;
				player.capabilities.allowFlying = allowFlying;
				mc.playerController = controller;
			}

			((IExtendedPlayerController) mc.playerController).setReachDistanceExtension(Math.max(0, ((IExtendedPlayerController) mc.playerController).getReachDistanceExtension() + reach));
		}
	}

	@Override
	public boolean openWikiPage(World world, Block block, MovingObjectPosition pos) {
		IWikiProvider wiki = WikiHooks.getWikiFor(block);
		String url = wiki.getWikiURL(world, pos);
		if(url != null && !url.isEmpty()) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public String getLastVersion() {
		String s = PersistentVariableHelper.lastBotaniaVersion;

		if(s == null)
			return "N/A";

		if(s.indexOf("-") > 0)
			return s.split("-")[1];

		return s;
	}

	@Override
	public void playRecordClientSided(World world, BlockPos pos, ItemRecord record) {
		Minecraft mc = Minecraft.getMinecraft();
		if(record == null)
			world.playAuxSFXAtEntity(null, 1005, pos, 0);
		else {
			world.playAuxSFXAtEntity(null, 1005, pos, Item.getIdFromItem(record));
			mc.ingameGUI.setRecordPlayingMessage(record.getRecordNameLocal());
		}
	}

	@Override
	public long getWorldElapsedTicks() {
		return ClientTickHandler.ticksInGame;
	}

	@Override
	public void setMultiblock(World world, int x, int y, int z, double radius, Block block) {
		MultiblockSextant mb = new MultiblockSextant();

		int iradius = (int) radius + 1;
		for(int i = 0; i < iradius * 2 + 1; i++)
			for(int j = 0; j < iradius * 2 + 1; j++) {
				int xp = x + i - iradius;
				int zp = z + j - iradius;

				if((int) Math.floor(MathHelper.pointDistancePlane(xp, zp, x, z)) == (iradius - 1))
					mb.addComponent(new AnyComponent(new BlockPos(xp - x, 1, zp - z), block.getDefaultState()));

				if((int) Math.floor(MathHelper.pointDistancePlane(xp, zp, x, z)) == iradius - 1)
					mb.addComponent(new AnyComponent(new BlockPos(xp - x, 1, zp - z), block.getDefaultState()));
			}

		MultiblockRenderHandler.setMultiblock(mb.makeSet());
		MultiblockRenderHandler.anchor = new BlockPos(x, y, z);
	}

	@Override
	public void removeSextantMultiblock() {
		MultiblockSet set = MultiblockRenderHandler.currentMultiblock;
		if(set != null) {
			Multiblock mb = set.getForFacing(EnumFacing.SOUTH);
			if(mb instanceof MultiblockSextant)
				MultiblockRenderHandler.setMultiblock(null);
		}
	}

	private static boolean noclipEnabled = false;
	private static boolean corruptSparkle = false;

	@Override
	public void setSparkleFXNoClip(boolean noclip) {
		noclipEnabled = noclip;
	}

	@Override
	public void setSparkleFXCorrupt(boolean corrupt) {
		corruptSparkle = corrupt;
	}

	@Override
	public void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float size, int m, boolean fake) {
		if(!doParticle(world) && !fake)
			return;

		FXSparkle sparkle = new FXSparkle(world, x, y, z, size, r, g, b, m);
		sparkle.fake = sparkle.noClip = fake;
		if(noclipEnabled)
			sparkle.noClip = true;
		if(corruptSparkle)
			sparkle.corrupt = true;
		Minecraft.getMinecraft().effectRenderer.addEffect(sparkle);
	}

	private static boolean distanceLimit = true;
	private static boolean depthTest = true;

	@Override
	public void setWispFXDistanceLimit(boolean limit) {
		distanceLimit = limit;
	}

	@Override
	public void setWispFXDepthTest(boolean test) {
		depthTest = test;
	}

	@Override
	public void wispFX(World world, double x, double y, double z, float r, float g, float b, float size, float motionx, float motiony, float motionz, float maxAgeMul) {
		if(!doParticle(world))
			return;

		FXWisp wisp = new FXWisp(world, x, y, z, size, r, g, b, distanceLimit, depthTest, maxAgeMul);
		wisp.motionX = motionx;
		wisp.motionY = motiony;
		wisp.motionZ = motionz;

		Minecraft.getMinecraft().effectRenderer.addEffect(wisp);
	}

	private boolean doParticle(World world) {
		if(!world.isRemote)
			return false;

		if(!ConfigHandler.useVanillaParticleLimiter)
			return true;

		float chance = 1F;
		if(Minecraft.getMinecraft().gameSettings.particleSetting == 1)
			chance = 0.6F;
		else if(Minecraft.getMinecraft().gameSettings.particleSetting == 2)
			chance = 0.2F;

		return chance == 1F || Math.random() < chance;
	}

	@Override
	public void lightningFX(World world, Vector3 vectorStart, Vector3 vectorEnd, float ticksPerMeter, long seed, int colorOuter, int colorInner) {
		LightningHandler.spawnLightningBolt(world, vectorStart, vectorEnd, ticksPerMeter, seed, colorOuter, colorInner);
	}
}

