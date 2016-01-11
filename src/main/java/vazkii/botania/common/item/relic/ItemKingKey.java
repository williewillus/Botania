/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Aug 16, 2015, 2:54:35 PM (GMT)]
 */
package vazkii.botania.common.item.relic;

import java.util.Random;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.entity.EntityBabylonWeapon;
import vazkii.botania.common.lib.LibItemNames;

public class ItemKingKey extends ItemRelic implements IManaUsingItem {

	private static final String TAG_WEAPONS_SPAWNED = "weaponsSpawned";
	private static final String TAG_CHARGING = "charging";

	public static final int WEAPON_TYPES = 12;

	public ItemKingKey() {
		super(LibItemNames.KING_KEY);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		par3EntityPlayer.setItemInUse(par1ItemStack, getMaxItemUseDuration(par1ItemStack));
		setCharging(par1ItemStack, true);
		return par1ItemStack;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int time) {
		int spawned = getWeaponsSpawned(stack);
		if(spawned == 20) {
			setCharging(stack, false);
			setWeaponsSpawned(stack, 0);
		}
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
		int spawned = getWeaponsSpawned(stack);

		if(count != getMaxItemUseDuration(stack) && spawned < 20 && !player.worldObj.isRemote && ManaItemHandler.requestManaExact(stack, player, 150, true)) {
			Vector3 look = new Vector3(player.getLookVec());
			look.y = 0;
			look.normalize().negate().multiply(2);
			int div = spawned / 5;
			int mod = spawned % 5;

			Vector3 pl = look.copy().add(Vector3.fromEntityCenter(player)).add(0, 1.6, div * 0.1);

			Random rand = player.worldObj.rand;
			Vector3 axis = look.copy().normalize().crossProduct(new Vector3(-1, 0, -1)).normalize();
			Vector3 axis1 = axis.copy();

			double rot = mod * Math.PI / 4 - Math.PI / 2;

			axis1.multiply(div * 3.5 + 5).rotate(rot, look);
			if(axis1.y < 0)
				axis1.y = -axis1.y;

			Vector3 end = pl.copy().add(axis1);

			EntityBabylonWeapon weapon = new EntityBabylonWeapon(player.worldObj, player);
			weapon.posX = end.x;
			weapon.posY = end.y;
			weapon.posZ = end.z;
			weapon.rotationYaw = player.rotationYaw;
			weapon.setVariety(rand.nextInt(WEAPON_TYPES));
			weapon.setDelay(spawned);
			weapon.setRotation(MathHelper.wrapAngleTo180_float(-player.rotationYaw + 180));

			player.worldObj.spawnEntityInWorld(weapon);
			player.worldObj.playSoundAtEntity(weapon, "botania:babylonSpawn", 1F, 1F + player.worldObj.rand.nextFloat() * 3F);
			setWeaponsSpawned(stack, spawned + 1);
		}
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack) {
		return EnumAction.BOW;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack) {
		return 72000;
	}


	public static boolean isCharging(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_CHARGING, false);
	}

	public static int getWeaponsSpawned(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_WEAPONS_SPAWNED, 0);
	}

	public static void setCharging(ItemStack stack, boolean charging) {
		ItemNBTHelper.setBoolean(stack, TAG_CHARGING, charging);
	}

	public static void setWeaponsSpawned(ItemStack stack, int count) {
		ItemNBTHelper.setInt(stack, TAG_WEAPONS_SPAWNED, count);
	}

	@Override
	public boolean isFull3D() {
		return true;
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

}
