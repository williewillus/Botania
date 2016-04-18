/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 29, 2015, 7:54:40 PM (GMT)]
 */
package vazkii.botania.common.item.relic;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.UsernameCache;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.item.ItemMod;
import vazkii.botania.common.item.ModItems;

import java.util.List;
import java.util.UUID;

public class ItemRelic extends ItemMod implements IRelic {

	private static final String TAG_SOULBIND_NAME = "soulbind";
	private static final String TAG_SOULBIND_UUID = "soulbindUUID";

	private Achievement achievement;

	public ItemRelic(String name) {
		super(name);
		setMaxStackSize(1);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if(!world.isRemote && entity instanceof EntityPlayer)
			updateRelic(stack, (EntityPlayer) entity);
	}

	@Override
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List<String> p_77624_3_, boolean p_77624_4_) {
		addBindInfo(p_77624_3_, p_77624_1_, p_77624_2_);
	}

	public void addBindInfo(List<String> list, ItemStack stack, EntityPlayer player) {
		if(GuiScreen.isShiftKeyDown()) {
			if(!hasUUID(stack)) {
				addStringToTooltip(I18n.translateToLocal("botaniamisc.relicUnbound"), list);
			} else {
				addStringToTooltip(String.format(I18n.translateToLocal("botaniamisc.relicSoulbound"), getSoulbindUsername(stack)), list);
				if(!getSoulbindUUID(stack).equals(player.getUniqueID()))
					addStringToTooltip(String.format(I18n.translateToLocal("botaniamisc.notYourSagittarius"), getSoulbindUsername(stack)), list);
			}

			if(stack.getItem() == ModItems.aesirRing)
				addStringToTooltip(I18n.translateToLocal("botaniamisc.dropIkea"), list);

			if(stack.getItem() == ModItems.dice) {
				addStringToTooltip("", list);
				String name = stack.getUnlocalizedName() + ".poem";
				for(int i = 0; i < 4; i++)
					addStringToTooltip(TextFormatting.ITALIC + I18n.translateToLocal(name + i), list);
			}
		} else addStringToTooltip(I18n.translateToLocal("botaniamisc.shiftinfo"), list);
	}

	public boolean shouldDamageWrongPlayer() {
		return true;
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		return Integer.MAX_VALUE;
	}

	static void addStringToTooltip(String s, List<String> tooltip) {
		tooltip.add(s.replaceAll("&", "\u00a7"));
	}

	public void updateRelic(ItemStack stack, EntityPlayer player) {
		if(stack == null || !(stack.getItem() instanceof IRelic))
			return;

		boolean rightPlayer = true;
		if(hasUUID(stack)) {
			// Sync to username todo is this worth 'optimizing'?
			if (UsernameCache.containsUUID(getSoulbindUUID(stack))) {
				bindToUsername(UsernameCache.getLastKnownUsername(getSoulbindUUID(stack)), stack);
			} else {
				bindToUsername("", stack);
			}

			// UUID trumps username
			rightPlayer = getSoulbindUUID(stack).equals(player.getUniqueID());
		} else {
			if ("".equals(getSoulbindUsername(stack))) {
				// New user
				bindToUUID(player.getUniqueID(), stack);
				player.addStat(((IRelic) stack.getItem()).getBindAchievement(), 1);
			} else {
				if (player.getName().equals(getSoulbindUsername(stack))) {
					// Old relic, correct owner, convert to UUID
					bindToUUID(player.getUniqueID(), stack);
				} else {
					// Old relic, wrong owner, damage
					rightPlayer = false;
				}
			}
		}

		if(!rightPlayer && player.ticksExisted % 10 == 0 && (!(stack.getItem() instanceof ItemRelic) || ((ItemRelic) stack.getItem()).shouldDamageWrongPlayer()))
			player.attackEntityFrom(damageSource(), 2);
	}

	public boolean isRightPlayer(EntityPlayer player, ItemStack stack) {
		if (hasUUID(stack)) {
			return getSoulbindUUID(stack).equals(player.getUniqueID());
		} else {
			return getSoulbindUsername(stack).equals(player.getName());
		}
	}

	public static DamageSource damageSource() {
		return new DamageSource("botania-relic");
	}

	@Override
	public void bindToUsername(String playerName, ItemStack stack) {
		ItemNBTHelper.setString(stack, TAG_SOULBIND_NAME, playerName);
	}

	@Override
	public String getSoulbindUsername(ItemStack stack) {
		return ItemNBTHelper.getString(stack, TAG_SOULBIND_NAME, "");
	}

	@Override
	public void bindToUUID(UUID uuid, ItemStack stack) {
		ItemNBTHelper.setString(stack, TAG_SOULBIND_UUID, uuid.toString());
	}

	@Override
	public UUID getSoulbindUUID(ItemStack stack) {
		if(ItemNBTHelper.verifyExistance(stack, TAG_SOULBIND_UUID)) {
			try {
				return UUID.fromString(ItemNBTHelper.getString(stack, TAG_SOULBIND_UUID, ""));
			} catch (IllegalArgumentException ex) { // Bad UUID in tag
				ItemNBTHelper.removeEntry(stack, TAG_SOULBIND_UUID);
			}
		}

		return null;
	}

	@Override
	public boolean hasUUID(ItemStack stack) {
		return getSoulbindUUID(stack) != null;
	}

	@Override
	public Achievement getBindAchievement() {
		return achievement;
	}

	@Override
	public void setBindAchievement(Achievement achievement) {
		this.achievement = achievement;
	}

	@Override
	public EnumRarity getRarity(ItemStack p_77613_1_) {
		return BotaniaAPI.rarityRelic;
	}

}
