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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.api.sound.BotaniaSoundEvents;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.entity.EntityBabylonWeapon;
import vazkii.botania.common.lib.LibItemNames;

import java.util.Random;

public class ItemKingKey extends ItemRelic implements IManaUsingItem {

	private static final String TAG_WEAPONS_SPAWNED = "weaponsSpawned";
	private static final String TAG_CHARGING = "charging";

	public static final int WEAPON_TYPES = 12;

	public ItemKingKey() {
		super(LibItemNames.KING_KEY);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, EnumHand hand) {
		par3EntityPlayer.setActiveHand(hand);
		setCharging(par1ItemStack, true);
		return ActionResult.newResult(EnumActionResult.SUCCESS, par1ItemStack);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase living, int time) {
		int spawned = getWeaponsSpawned(stack);
		if(spawned == 20) {
			setCharging(stack, false);
			setWeaponsSpawned(stack, 0);
		}
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
		int spawned = getWeaponsSpawned(stack);

		if(count != getMaxItemUseDuration(stack) && spawned < 20 && !living.worldObj.isRemote && (!(living instanceof EntityPlayer) || ManaItemHandler.requestManaExact(stack, ((EntityPlayer) living), 150, true))) {
			Vector3 look = new Vector3(living.getLookVec());
			look.y = 0;
			look.normalize().negate().multiply(2);
			int div = spawned / 5;
			int mod = spawned % 5;

			Vector3 pl = look.copy().add(Vector3.fromEntityCenter(living)).add(0, 1.6, div * 0.1);

			Random rand = living.worldObj.rand;
			Vector3 axis = look.copy().normalize().crossProduct(new Vector3(-1, 0, -1)).normalize();
			Vector3 axis1 = axis.copy();

			double rot = mod * Math.PI / 4 - Math.PI / 2;

			axis1.multiply(div * 3.5 + 5).rotate(rot, look);
			if(axis1.y < 0)
				axis1.y = -axis1.y;

			Vector3 end = pl.copy().add(axis1);

			EntityBabylonWeapon weapon = new EntityBabylonWeapon(living.worldObj, living);
			weapon.posX = end.x;
			weapon.posY = end.y;
			weapon.posZ = end.z;
			weapon.rotationYaw = living.rotationYaw;
			weapon.setVariety(rand.nextInt(WEAPON_TYPES));
			weapon.setDelay(spawned);
			weapon.setRotation(MathHelper.wrapDegrees(-living.rotationYaw + 180));

			living.worldObj.spawnEntityInWorld(weapon);
			weapon.playSound(BotaniaSoundEvents.babylonSpawn, 1F, 1F + living.worldObj.rand.nextFloat() * 3F);
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
	public boolean usesMana(ItemStack stack) {
		return true;
	}

}
