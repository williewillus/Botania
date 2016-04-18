/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 29, 2015, 7:56:27 PM (GMT)]
 */
package vazkii.botania.common.item.relic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.world.World;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.equipment.bauble.ItemBauble;

import java.util.List;
import java.util.UUID;

public abstract class ItemRelicBauble extends ItemBauble implements IRelic {

	Achievement achievement;
	private ItemRelic dummy = new ItemRelic("dummy") { // Delegate for relic stuff
		@Override
		protected boolean shouldRegister() {
			return false;
		}
	};

	public ItemRelicBauble(String name) {
		super(name);
	}

	@Override
	public void onUpdate(ItemStack p_77663_1_, World p_77663_2_, Entity p_77663_3_, int p_77663_4_, boolean p_77663_5_) {
		if(p_77663_3_ instanceof EntityPlayer)
			dummy.updateRelic(p_77663_1_, (EntityPlayer) p_77663_3_);
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		super.onWornTick(stack, player);
		if(player instanceof EntityPlayer) {
			EntityPlayer ePlayer = (EntityPlayer) player;
			dummy.updateRelic(stack, ePlayer);
			if(dummy.isRightPlayer(ePlayer, stack))
				onValidPlayerWornTick(stack, ePlayer);
		}
	}

	@Override
	public void addHiddenTooltip(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4) {
		super.addHiddenTooltip(par1ItemStack, par2EntityPlayer, par3List, par4);
		dummy.addBindInfo(par3List, par1ItemStack, par2EntityPlayer);
	}

	public void onValidPlayerWornTick(ItemStack stack, EntityPlayer player) {
		// NO-OP
	}

	@Override
	public boolean canEquip(ItemStack stack, EntityLivingBase player) {
		return player instanceof EntityPlayer && dummy.isRightPlayer(((EntityPlayer) player), stack);
	}

	@Override
	public void bindToUsername(String playerName, ItemStack stack) {
		dummy.bindToUsername(playerName, stack);
	}

	@Override
	public String getSoulbindUsername(ItemStack stack) {
		return dummy.getSoulbindUsername(stack);
	}

	@Override
	public void bindToUUID(UUID uuid, ItemStack stack) {
		dummy.bindToUUID(uuid, stack);
	}

	@Override
	public UUID getSoulbindUUID(ItemStack stack) {
		return dummy.getSoulbindUUID(stack);
	}

	@Override
	public boolean hasUUID(ItemStack stack) {
		return dummy.hasUUID(stack);
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

	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		return Integer.MAX_VALUE;
	}

}
