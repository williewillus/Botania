/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [May 26, 2014, 4:05:50 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import baubles.api.BaubleType;
import baubles.common.lib.PlayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.api.sound.BotaniaSoundEvents;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.core.helper.ShaderHelper;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.Botania;
import vazkii.botania.common.achievement.ICraftAchievement;
import vazkii.botania.common.achievement.ModAchievements;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lib.LibItemNames;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class ItemFlightTiara extends ItemBauble implements IManaUsingItem, IBaubleRender, ICraftAchievement {

	private static ResourceLocation textureHud = new ResourceLocation(LibResources.GUI_HUD_ICONS);
	private static ResourceLocation textureHalo = new ResourceLocation(LibResources.MISC_HALO);

	private static final String TAG_FLYING = "flying";
	private static final String TAG_TIME_LEFT = "timeLeft";
	private static final String TAG_INFINITE_FLIGHT = "infiniteFlight";
	private static final String TAG_DASH_COOLDOWN = "dashCooldown";
	private static final String TAG_IS_SPRINTING = "isSprinting";

	public static final List<String> playersWithFlight = new ArrayList();
	private static final int COST = 35;
	private static final int COST_OVERKILL = COST * 3;
	private static final int MAX_FLY_TIME = 1200;

	private static final int SUBTYPES = 8;
	public static final int WING_TYPES = 9;

	public ItemFlightTiara() {
		super(LibItemNames.FLIGHT_TIARA);
		MinecraftForge.EVENT_BUS.register(this);
		setHasSubtypes(true);
	}

	@Override
	public BaubleType getBaubleType(ItemStack arg0) {
		return BaubleType.AMULET;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		for(int i = 0; i < SUBTYPES + 1; i++)
			list.add(new ItemStack(item, 1, i));
	}

	@Override
	public void addHiddenTooltip(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4) {
		super.addHiddenTooltip(par1ItemStack, par2EntityPlayer, par3List, par4);
		par3List.add(I18n.translateToLocal("botania.wings" + par1ItemStack.getItemDamage()));
	}

	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase player) {
		super.onEquipped(stack, player);
		if(stack.getItemDamage() != WING_TYPES && hash(stack.getDisplayName()).equals("16E1BDFD1D6AE1A954C9C5E1B2D9099780F3E1724541F1F2F77310B769CFFBAC")) {
			stack.setItemDamage(WING_TYPES);
			stack.getTagCompound().removeTag("display");
		}
	}

	String hash(String str) {
		if(str != null)
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				return new HexBinaryAdapter().marshal(md.digest(salt(str).getBytes()));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		return "";
	}

	// Might as well be called sugar given it's not secure at all :D
	String salt(String str) {
		str = str += "wowsuchsaltmuchsecurityverywow";
		SecureRandom rand = new SecureRandom(str.getBytes());
		int l = str.length();
		int steps = rand.nextInt(l);
		char[] chrs = str.toCharArray();
		for(int i = 0; i < steps; i++) {
			int indA = rand.nextInt(l);
			int indB;
			do {
				indB = rand.nextInt(l);
			} while(indB == indA);
			char c = (char) (chrs[indA] ^ chrs[indB]);
			chrs[indA] = c;
		}

		return String.copyValueOf(chrs);
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		super.onWornTick(stack, player);

		if(player instanceof EntityPlayer) {
			EntityPlayer p = (EntityPlayer) player;
			boolean flying = p.capabilities.isFlying;

			boolean wasSprting = ItemNBTHelper.getBoolean(stack, TAG_IS_SPRINTING, false);
			boolean isSprinting = p.isSprinting();
			if(isSprinting != wasSprting)
				ItemNBTHelper.setBoolean(stack, TAG_IS_SPRINTING, isSprinting);

			int time = ItemNBTHelper.getInt(stack, TAG_TIME_LEFT, MAX_FLY_TIME);
			int newTime = time;
			Vector3 look = new Vector3(p.getLookVec());
			look.y = 0;
			look.normalize();

			if(flying) {
				if(time > 0 && !ItemNBTHelper.getBoolean(stack, TAG_INFINITE_FLIGHT, false))
					newTime--;
				final int maxCd = 80;
				int cooldown = ItemNBTHelper.getInt(stack, TAG_DASH_COOLDOWN, 0);
				if(!wasSprting && isSprinting && cooldown == 0) {
					p.motionX += look.x;
					p.motionZ += look.z;
					p.worldObj.playSound(null, p.posX, p.posY, p.posZ, BotaniaSoundEvents.dash, SoundCategory.PLAYERS, 1F, 1F);
					ItemNBTHelper.setInt(stack, TAG_DASH_COOLDOWN, maxCd);
				} else if(cooldown > 0) {
					if(maxCd - cooldown < 2)
						player.moveFlying(0F, 1F, 5F);
					else if(maxCd - cooldown < 10)
						player.setSprinting(false);
					ItemNBTHelper.setInt(stack, TAG_DASH_COOLDOWN, cooldown - 2);
					if(player instanceof EntityPlayerMP)
						BotaniaAPI.internalHandler.sendBaubleUpdatePacket((EntityPlayerMP) player, 0);
				}
			} else if(!flying) {
				boolean doGlide = player.isSneaking() && !player.onGround && player.fallDistance >= 2F;
				if(time < MAX_FLY_TIME && player.ticksExisted % (doGlide ? 6 : 2) == 0)
					newTime++;

				if(doGlide) {
					player.motionY = Math.max(-0.15F, player.motionY);
					float mul = 0.6F;
					player.motionX = look.x * mul;
					player.motionZ = look.z * mul;
					player.fallDistance = 2F;
				}
			}

			ItemNBTHelper.setBoolean(stack, TAG_FLYING, flying);
			if(newTime != time)
				ItemNBTHelper.setInt(stack, TAG_TIME_LEFT, newTime);
		}
	}

	@SubscribeEvent
	public void updatePlayerFlyStatus(LivingUpdateEvent event) {
		if(event.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			ItemStack tiara = PlayerHandler.getPlayerBaubles(player).getStackInSlot(0);
			int left = ItemNBTHelper.getInt(tiara, TAG_TIME_LEFT, MAX_FLY_TIME);

			if(playersWithFlight.contains(playerStr(player))) {
				if(shouldPlayerHaveFlight(player)) {
					player.capabilities.allowFlying = true;
					if(player.capabilities.isFlying) {
						if(!player.worldObj.isRemote)
							ManaItemHandler.requestManaExact(tiara, player, getCost(tiara, left), true);
						else if(Math.abs(player.motionX) > 0.1 || Math.abs(player.motionZ) > 0.1) {
							double x = event.getEntityLiving().posX - 0.5;
							double y = event.getEntityLiving().posY - 0.5;
							double z = event.getEntityLiving().posZ - 0.5;

							player.getGameProfile().getName();
							float r = 1F;
							float g = 1F;
							float b = 1F;

							switch(tiara.getItemDamage()) {
							case 2 : {
								r = 0.1F;
								g = 0.1F;
								b = 0.1F;
								break;
							}
							case 3 : {
								r = 0F;
								g = 0.6F;
								break;
							}
							case 4 : {
								g = 0.3F;
								b = 0.3F;
								break;
							}
							case 5 : {
								r = 0.6F;
								g = 0F;
								b = 0.6F;
								break;
							}
							case 6 : {
								r = 0.4F;
								g = 0F;
								b = 0F;
								break;
							}
							case 7 : {
								r = 0.2F;
								g = 0.6F;
								b = 0.2F;
								break;
							}
							case 8 : {
								r = 0.85F;
								g = 0.85F;
								b = 0F;
								break;
							}
							case 9 : {
								r = 0F;
								b = 0F;
								break;
							}
							}

							for(int i = 0; i < 2; i++)
								Botania.proxy.sparkleFX(event.getEntityLiving().worldObj, x + Math.random() * event.getEntityLiving().width, y + Math.random() * 0.4, z + Math.random() * event.getEntityLiving().width, r, g, b, 2F * (float) Math.random(), 20);
						}
					}
				} else {
					if(!player.capabilities.isCreativeMode) {
						player.capabilities.allowFlying = false;
						player.capabilities.isFlying = false;
						player.capabilities.disableDamage = false;
					}
					playersWithFlight.remove(playerStr(player));
				}
			} else if(shouldPlayerHaveFlight(player)) {
				playersWithFlight.add(playerStr(player));
				player.capabilities.allowFlying = true;
			}
		}
	}

	@SubscribeEvent
	public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		String username = event.player.getGameProfile().getName();
		playersWithFlight.remove(username + ":false");
		playersWithFlight.remove(username + ":true");
	}

	public static String playerStr(EntityPlayer player) {
		return player.getGameProfile().getName() + ":" + player.worldObj.isRemote;
	}

	private boolean shouldPlayerHaveFlight(EntityPlayer player) {
		ItemStack armor = PlayerHandler.getPlayerBaubles(player).getStackInSlot(0);
		if(armor != null && armor.getItem() == this) {
			int left = ItemNBTHelper.getInt(armor, TAG_TIME_LEFT, MAX_FLY_TIME);
			boolean flying = ItemNBTHelper.getBoolean(armor, TAG_FLYING, false);
			return (left > (flying ? 0 : MAX_FLY_TIME / 10) || player.inventory.hasItemStack(new ItemStack(ModItems.flugelEye))) && ManaItemHandler.requestManaExact(armor, player, getCost(armor, left), false);
		}

		return false;
	}

	public int getCost(ItemStack stack, int timeLeft) {
		return timeLeft <= 0 ? COST_OVERKILL : COST;
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, RenderType type, float partialTicks) {
		int meta = stack.getItemDamage();
		if(type == RenderType.BODY) {
			if(meta > 0 && meta <= MiscellaneousIcons.INSTANCE.tiaraWingIcons.length) {
				TextureAtlasSprite icon = MiscellaneousIcons.INSTANCE.tiaraWingIcons[meta - 1];
				Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

				boolean flying = player.capabilities.isFlying;

				float rz = 120F;
				float rx = 20F + (float) ((Math.sin((double) (player.ticksExisted + partialTicks) * (flying ? 0.4F : 0.2F)) + 0.5F) * (flying ? 30F : 5F));
				float ry = 0F;
				float h = 0.2F;
				float i = 0.15F;
				float s = 1F;

				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlStateManager.color(1F, 1F, 1F, 1F);

				int light = 15728880;
				int lightmapX = light % 65536;
				int lightmapY = light / 65536;
				switch(meta) {
				case 1 : { // Jibril
					h = 0.4F;
					break;
				}
				case 2 : { // Sephiroth
					s = 1.3F;
					break;
				}
				case 3 : { // Cirno
					h = -0.1F;
					rz = 0F;
					rx = 0F;
					i = 0.3F;
					break;
				}
				case 4 : { // Phoenix
					rz = 180F;
					h = 0.5F;
					rx = 20F;
					ry = -(float) ((Math.sin((double) (player.ticksExisted + partialTicks) * (flying ? 0.4F : 0.2F)) + 0.6F) * (flying ? 30F : 5F));
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapX, lightmapY);
					break;
				}
				case 5 : { // Kuroyukihime
					h = 0.8F;
					rz = 180F;
					ry = -rx;
					rx = 0F;
					s = 2F;
					break;
				}
				case 6 : { // Random Devil
					rz = 150F;
					break;
				}
				case 7 : { // Lyfa
					h = -0.1F;
					rz = 0F;
					ry = -rx;
					rx = 0F;
					GlStateManager.color(1F, 1F, 1F, 0.5F + (float) Math.cos((double) (player.ticksExisted + partialTicks) * 0.3F) * 0.2F);
					break;
				}
				case 8 : { // Mega Ultra Chicken
					h = 0.1F;
					break;
				}
				case 9 : { // The One
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapX, lightmapY);
					rz = 180F;
					rx = 0F;
					s = 1.5F;
					h = 1.2F;
					GlStateManager.color(1F, 1F, 1F, 0.5F + (flying ? (float) Math.cos((double) (player.ticksExisted + partialTicks) * 0.3F) * 0.25F + 0.25F : 0F));
				}
				}

				float f = icon.getMinU();
				float f1 = icon.getMaxU();
				float f2 = icon.getMinV();
				float f3 = icon.getMaxV();
				float sr = 1F / s;

				Helper.rotateIfSneaking(player);

				GlStateManager.translate(0F, h, i);

				GlStateManager.rotate(rz, 0F, 0F, 1F);
				GlStateManager.rotate(rx, 1F, 0F, 0F);
				GlStateManager.rotate(ry, 0F, 1F, 0F);
				GlStateManager.scale(s, s, s);
				IconHelper.renderIconIn3D(Tessellator.getInstance(), f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 1F / 32F);
				GlStateManager.scale(sr, sr, sr);
				GlStateManager.rotate(-ry, 0F, 1F, 0F);
				GlStateManager.rotate(-rx, 1F, 0F, 0F);
				GlStateManager.rotate(-rz, 0F, 0F, 1F);

				if(meta != 2) { // Sephiroth
					GlStateManager.scale(-1F, 1F, 1F);
					GlStateManager.rotate(rz, 0F, 0F, 1F);
					GlStateManager.rotate(rx, 1F, 0F, 0F);
					GlStateManager.rotate(ry, 0F, 1F, 0F);
					GlStateManager.scale(s, s, s);
					IconHelper.renderIconIn3D(Tessellator.getInstance(), f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 1F / 32F);
					GlStateManager.scale(sr, sr, sr);
					GlStateManager.rotate(-ry, 1F, 0F, 0F);
					GlStateManager.rotate(-rx, 1F, 0F, 0F);
					GlStateManager.rotate(-rz, 0F, 0F, 1F);
				}

				GlStateManager.color(1F, 1F, 1F);
				GlStateManager.popMatrix();
			}
		} else if(meta == 1) // Jibril's Halo
			renderHalo(player, partialTicks);
	}

	@SideOnly(Side.CLIENT)
	public static void renderHalo(EntityPlayer player, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.color(1F, 1F, 1F, 1F);

		Minecraft.getMinecraft().renderEngine.bindTexture(textureHalo);

		if(player != null)
			Helper.translateToHeadLevel(player);
		GlStateManager.translate(0, 1.5F, 0);
		GlStateManager.rotate(30, 1, 0, -1);
		GlStateManager.translate(-0.1F, -0.5F, -0.1F);
		if(player != null)
			GlStateManager.rotate(player.ticksExisted + partialTicks, 0, 1, 0);
		else GlStateManager.rotate(Botania.proxy.getWorldElapsedTicks(), 0, 1, 0);

		Tessellator tes = Tessellator.getInstance();
		ShaderHelper.useShader(ShaderHelper.halo);
		tes.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		tes.getBuffer().pos(-0.75, 0, -0.75).tex(0, 0).endVertex();
		tes.getBuffer().pos(-0.75, 0, 0.75).tex(0, 1).endVertex();
		tes.getBuffer().pos(0.75, 0, 0.75).tex(1, 1).endVertex();
		tes.getBuffer().pos(0.75, 0, -0.75).tex(1, 0).endVertex();
		tes.draw();
		ShaderHelper.releaseShader();

		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableCull();
	}

	@SideOnly(Side.CLIENT)
	public static void renderHUD(ScaledResolution resolution, EntityPlayer player, ItemStack stack) {
		int u = Math.max(1, stack.getItemDamage()) * 9 - 9;
		int v = 0;

		Minecraft mc = Minecraft.getMinecraft();
		mc.renderEngine.bindTexture(textureHud);
		int xo = resolution.getScaledWidth() / 2 + 10;
		int x = xo;
		int y = resolution.getScaledHeight() - ConfigHandler.flightBarHeight;
		if(player.getAir() < 300)
			y = resolution.getScaledHeight() - ConfigHandler.flightBarBreathHeight;

		int left = ItemNBTHelper.getInt(stack, TAG_TIME_LEFT, MAX_FLY_TIME);

		int segTime = MAX_FLY_TIME / 10;
		int segs = left / segTime + 1;
		int last = left % segTime;

		for(int i = 0; i < segs; i++) {
			float trans = 1F;
			if(i == segs - 1) {
				trans = (float) last / (float) segTime;
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlStateManager.disableAlpha();
			}

			GlStateManager.color(1F, 1F, 1F, trans);
			RenderHelper.drawTexturedModalRect(x, y, 0, u, v, 9, 9);
			x += 8;
		}

		if(player.capabilities.isFlying) {
			int width = ItemNBTHelper.getInt(stack, TAG_DASH_COOLDOWN, 0);
			GlStateManager.color(1F, 1F, 1F, 1F);
			if(width > 0)
				Gui.drawRect(xo, y - 2, xo + 80, y - 1, 0x88000000);
			Gui.drawRect(xo, y - 2, xo + width, y - 1, 0xFFFFFFFF);
		}

		GlStateManager.enableAlpha();
		GlStateManager.color(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(Gui.icons);
	}

	@Override
	public Achievement getAchievementOnCraft(ItemStack stack, EntityPlayer player, IInventory matrix) {
		return stack.getItemDamage() == 1 ? ModAchievements.tiaraWings : null;
	}

}
