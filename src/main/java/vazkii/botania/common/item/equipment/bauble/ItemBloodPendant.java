/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Nov 6, 2014, 5:11:23 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import baubles.api.BaubleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.brew.IBrewContainer;
import vazkii.botania.api.brew.IBrewItem;
import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.item.IColorable;
import vazkii.botania.common.lib.LibItemNames;

import java.awt.*;
import java.util.List;

public class ItemBloodPendant extends ItemBauble implements IBrewContainer, IBrewItem, IManaUsingItem, IBaubleRender, IColorable {

	private static final String TAG_BREW_KEY = "brewKey";

	public ItemBloodPendant() {
		super(LibItemNames.BLOOD_PENDANT);
		setMaxStackSize(1);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		super.getSubItems(item, tab, list);
		for(String s : BotaniaAPI.brewMap.keySet()) {
			ItemStack brewStack = getItemForBrew(BotaniaAPI.brewMap.get(s), new ItemStack(this));
			if(brewStack != null)
				list.add(brewStack);
		}
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if(pass == 0)
			return 0xFFFFFF;

		Brew brew = getBrew(stack);
		if(brew == BotaniaAPI.fallbackBrew)
			return 0xC6000E;

		Color color = new Color(brew.getColor(stack));
		int add = (int) (Math.sin(ClientTickHandler.ticksInGame * 0.2) * 24);

		int r = Math.max(0, Math.min(255, color.getRed() + add));
		int g = Math.max(0, Math.min(255, color.getGreen() + add));
		int b = Math.max(0, Math.min(255, color.getBlue() + add));

		return r << 16 | g << 8 | b;
	}

	@Override
	public void addHiddenTooltip(ItemStack stack, EntityPlayer player, List<String> list, boolean adv) {
		super.addHiddenTooltip(stack, player, list, adv);

		Brew brew = getBrew(stack);
		if(brew == BotaniaAPI.fallbackBrew) {
			addStringToTooltip(TextFormatting.LIGHT_PURPLE + I18n.translateToLocal("botaniamisc.notInfused"), list);
			return;
		}

		addStringToTooltip(TextFormatting.LIGHT_PURPLE + String.format(I18n.translateToLocal("botaniamisc.brewOf"), I18n.translateToLocal(brew.getUnlocalizedName(stack))), list);
		for(PotionEffect effect : brew.getPotionEffects(stack)) {
			TextFormatting format = effect.getPotion().isBadEffect() ? TextFormatting.RED : TextFormatting.GRAY;
			addStringToTooltip(" " + format + I18n.translateToLocal(effect.getEffectName()) + (effect.getAmplifier() == 0 ? "" : " " + I18n.translateToLocal("botania.roman" + (effect.getAmplifier() + 1))), list);
		}
	}

	@Override
	public BaubleType getBaubleType(ItemStack arg0) {
		return BaubleType.AMULET;
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		Brew brew = getBrew(stack);
		if(brew != BotaniaAPI.fallbackBrew && player instanceof EntityPlayer && !player.worldObj.isRemote) {
			EntityPlayer eplayer = (EntityPlayer) player;
			PotionEffect effect = brew.getPotionEffects(stack).get(0);
			float cost = (float) brew.getManaCost(stack) / effect.getDuration() / (1 + effect.getAmplifier()) * 2.5F;
			boolean doRand = cost < 1;
			if(ManaItemHandler.requestManaExact(stack, eplayer, (int) Math.ceil(cost), false)) {
				PotionEffect currentEffect = player.getActivePotionEffect(effect.getPotion());
				boolean nightVision = effect.getPotion() == MobEffects.nightVision;
				if(currentEffect == null || currentEffect.getDuration() < (nightVision ? 205 : 3)) {
					PotionEffect applyEffect = new PotionEffect(effect.getPotion(), nightVision ? 285 : 80, effect.getAmplifier(), true, true);
					player.addPotionEffect(applyEffect);
				}

				if(!doRand || Math.random() < cost)
					ManaItemHandler.requestManaExact(stack, eplayer, (int) Math.ceil(cost), true);
			}
		}
	}

	@Override
	public Brew getBrew(ItemStack stack) {
		String key = ItemNBTHelper.getString(stack, TAG_BREW_KEY, "");
		return BotaniaAPI.getBrewFromKey(key);
	}

	public static void setBrew(ItemStack stack, Brew brew) {
		setBrew(stack, brew.getKey());
	}

	public static void setBrew(ItemStack stack, String brew) {
		ItemNBTHelper.setString(stack, TAG_BREW_KEY, brew);
	}

	@Override
	public ItemStack getItemForBrew(Brew brew, ItemStack stack) {
		if(!brew.canInfuseBloodPendant() || brew.getPotionEffects(stack).size() != 1 || brew.getPotionEffects(stack).get(0).getPotion().isInstant())
			return null;

		ItemStack brewStack = new ItemStack(this);
		setBrew(brewStack, brew);
		return brewStack;
	}

	@Override
	public int getManaCost(Brew brew, ItemStack stack) {
		return brew.getManaCost() * 10;
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return getBrew(stack) != BotaniaAPI.fallbackBrew;
	}

	@Override
	public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, RenderType type, float partialTicks) {
		if(type == RenderType.BODY) {
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			Helper.rotateIfSneaking(player);
			boolean armor = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST) != null;
			GlStateManager.rotate(180F, 1F, 0F, 0F);
			GlStateManager.translate(-0.26F, -0.4F, armor ? 0.2F : 0.15F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);

			for(TextureAtlasSprite icon : new TextureAtlasSprite[] { MiscellaneousIcons.INSTANCE.bloodPendantChain, MiscellaneousIcons.INSTANCE.bloodPendantGem }) {
				float f = icon.getMinU();
				float f1 = icon.getMaxU();
				float f2 = icon.getMinV();
				float f3 = icon.getMaxV();
				IconHelper.renderIconIn3D(Tessellator.getInstance(), f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 1F / 32F);

				Color color = new Color(getColorFromItemStack(stack, 1));
				GL11.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
				int light = 15728880;
				int lightmapX = light % 65536;
				int lightmapY = light / 65536;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapX, lightmapY);
			}
			GL11.glColor3ub(((byte) 255), ((byte) 255), ((byte) 255));
		}
	}

}
