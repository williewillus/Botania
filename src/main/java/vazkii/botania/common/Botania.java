/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 13, 2014, 6:32:39 PM (GMT)]
 */
package vazkii.botania.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import vazkii.botania.common.core.handler.IMCHandler;
import vazkii.botania.common.core.handler.ManaNetworkHandler;
import vazkii.botania.common.core.proxy.CommonProxy;
import vazkii.botania.common.integration.coloredlights.ILightHelper;
import vazkii.botania.common.integration.coloredlights.LightHelperColored;
import vazkii.botania.common.integration.coloredlights.LightHelperVanilla;
import vazkii.botania.common.lib.LibMisc;

@Mod(modid = LibMisc.MOD_ID, name = LibMisc.MOD_NAME, version = LibMisc.VERSION, dependencies = LibMisc.DEPENDENCIES, guiFactory = LibMisc.GUI_FACTORY, acceptedMinecraftVersions = LibMisc.MC_VERSIONS)
public class Botania {

	public static boolean gardenOfGlassLoaded = false;

	public static boolean thaumcraftLoaded = false;
	public static boolean bcTriggersLoaded = false;
	public static boolean bloodMagicLoaded = false;
	public static boolean coloredLightsLoaded = false;
	public static boolean etFuturumLoaded = false;
	public static boolean rfApiLoaded = false;
	public static boolean storageDrawersLoaded = false;
	public static boolean quarkLoaded = false;

	public static ILightHelper lightHelper;

	@Instance(LibMisc.MOD_ID)
	public static Botania instance;

	@SidedProxy(serverSide = LibMisc.PROXY_COMMON, clientSide = LibMisc.PROXY_CLIENT)
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		gardenOfGlassLoaded = Loader.isModLoaded("GardenOfGlass");

		thaumcraftLoaded = Loader.isModLoaded("Thaumcraft");
		bcTriggersLoaded = ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|statements");
		bloodMagicLoaded = Loader.isModLoaded("AWWayofTime"); // Psh, noob
		coloredLightsLoaded = Loader.isModLoaded("easycoloredlights");
		etFuturumLoaded = Loader.isModLoaded("etfuturum");
		rfApiLoaded = ModAPIManager.INSTANCE.hasAPI("CoFHAPI|energy");
		quarkLoaded = Loader.isModLoaded("Quark");

		storageDrawersLoaded = Loader.isModLoaded("StorageDrawers");
		
		lightHelper = coloredLightsLoaded ? new LightHelperColored() : new LightHelperVanilla();

		proxy.preInit(event);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void nagUnofficial(PlayerEvent.PlayerLoggedInEvent evt) {
		EntityPlayer player = evt.player;

		ITextComponent message = new TextComponentTranslation("botaniamisc.unofficial.warnRemove");
		message.setStyle(new Style().setColor(TextFormatting.RED));

		ITextComponent message2 = new TextComponentTranslation("botaniamisc.unofficial.report");
		message2.setStyle(new Style().setColor(TextFormatting.YELLOW));

		ITextComponent url = new TextComponentString(" https://github.com/williewillus/Botania/issues");
		url.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/williewillus/Botania/issues"));
		url.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("CLICK")));

		player.addChatComponentMessage(message);

		player.addChatComponentMessage(message2.appendSibling(url));
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	@EventHandler
	public void serverStarting(FMLServerAboutToStartEvent event) {
		proxy.serverAboutToStart(event);
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		proxy.serverStarting(event);
	}

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
		ManaNetworkHandler.instance.clear();
	}

	@EventHandler
	public void handleIMC(FMLInterModComms.IMCEvent event) {
		IMCHandler.processMessages(event.getMessages());
	}

}
