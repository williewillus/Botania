/**
 * This class was created by <Flaxbeard>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Aug 25, 2014, 2:57:16 PM (GMT)]
 */
package vazkii.botania.common.item.rod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import vazkii.botania.api.item.IManaProficiencyArmor;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.entity.EntityThrownItem;
import vazkii.botania.common.item.ItemMod;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lib.LibItemNames;
import vazkii.botania.common.lib.LibObfuscation;

public class ItemGravityRod extends ItemMod implements IManaUsingItem {

	private static final float RANGE = 3F;
	private static final int COST = 2;

	private static final String TAG_TICKS_TILL_EXPIRE = "ticksTillExpire";
	private static final String TAG_TICKS_COOLDOWN = "ticksCooldown";
	private static final String TAG_TARGET = "target";
	private static final String TAG_DIST = "dist";

	public ItemGravityRod() {
		setMaxStackSize(1);
		setUnlocalizedName(LibItemNames.GRAVITY_ROD);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity par3Entity, int p_77663_4_, boolean p_77663_5_) {
		if(!(par3Entity instanceof EntityPlayer))
			return;

		int ticksTillExpire = ItemNBTHelper.getInt(stack, TAG_TICKS_TILL_EXPIRE, 0);
		int ticksCooldown = ItemNBTHelper.getInt(stack, TAG_TICKS_COOLDOWN, 0);

		if(ticksTillExpire == 0) {
			ItemNBTHelper.setInt(stack, TAG_TARGET, -1);
			ItemNBTHelper.setDouble(stack, TAG_DIST, -1);
		}

		if(ticksCooldown > 0)
			ticksCooldown--;

		ticksTillExpire--;
		ItemNBTHelper.setInt(stack, TAG_TICKS_TILL_EXPIRE, ticksTillExpire);
		ItemNBTHelper.setInt(stack, TAG_TICKS_COOLDOWN, ticksCooldown);

		EntityPlayer player = (EntityPlayer) par3Entity;
		PotionEffect haste = player.getActivePotionEffect(Potion.digSpeed);
		float check = haste == null ? 0.16666667F : haste.getAmplifier() == 1 ? 0.5F : 0.4F;
		if(player.getCurrentEquippedItem() == stack && player.swingProgress == check && !world.isRemote)
			leftClick(player);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		int targetID = ItemNBTHelper.getInt(stack, TAG_TARGET, -1);
		int ticksCooldown = ItemNBTHelper.getInt(stack, TAG_TICKS_COOLDOWN, 0);
		double length = ItemNBTHelper.getDouble(stack, TAG_DIST, -1);
		if(ticksCooldown == 0) {
			Entity item = null;
			if(targetID != -1 && player.worldObj.getEntityByID(targetID) != null) {
				Entity taritem = player.worldObj.getEntityByID(targetID);

				boolean found = false;
				Vector3 target = Vector3.fromEntityCenter(player);
				List<Entity> entities = new ArrayList<Entity>();
				int distance = 1;
				while(entities.size() == 0 && distance < 25) {
					target.add(new Vector3(player.getLookVec()).multiply(distance));

					target.y += 0.5;
					entities = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(target.x - RANGE, target.y - RANGE, target.z - RANGE, target.x + RANGE, target.y + RANGE, target.z + RANGE));
					distance++;
					if(entities.contains(taritem))
						found = true;
				}

				if(found)
					item = player.worldObj.getEntityByID(targetID);
			}

			if(item == null) {
				Vector3 target = Vector3.fromEntityCenter(player);
				List<Entity> entities = new ArrayList<Entity>();
				int distance = 1;
				while(entities.size() == 0 && distance < 25) {
					target.add(new Vector3(player.getLookVec()).multiply(distance));

					target.y += 0.5;
					entities = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(target.x - RANGE, target.y - RANGE, target.z - RANGE, target.x + RANGE, target.y + RANGE, target.z + RANGE));
					distance++;
				}

				if(entities.size() > 0) {
					item = entities.get(0);
					length = 5.5D;
					if(item instanceof EntityItem)
						length = 2.0D;
				}
			}

			if(ManaItemHandler.requestManaExactForTool(stack, player, COST, true) && item != null) {
				if(item instanceof EntityItem)
					ObfuscationReflectionHelper.setPrivateValue(EntityItem.class, ((EntityItem) item), 5, LibObfuscation.PICKUP_DELAY);

				if(item instanceof EntityLivingBase) {
					EntityLivingBase targetEntity = (EntityLivingBase)item;
					targetEntity.fallDistance = 0.0F;
					if(targetEntity.getActivePotionEffect(Potion.moveSlowdown) == null)
						targetEntity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 2, 3, true, true));
				}

				Vector3 target3 = Vector3.fromEntityCenter(player);
				target3.add(new Vector3(player.getLookVec()).multiply(length));
				target3.y += 0.5;
				if(item instanceof EntityItem)
					target3.y += 0.25;

				for(int i = 0; i < 4; i++) {
					float r = 0.5F + (float) Math.random() * 0.5F;
					float b = 0.5F + (float) Math.random() * 0.5F;
					float s = 0.2F + (float) Math.random() * 0.1F;
					float m = 0.1F;
					float xm = ((float) Math.random() - 0.5F) * m;
					float ym = ((float) Math.random() - 0.5F) * m;
					float zm = ((float) Math.random() - 0.5F) * m;
					Botania.proxy.wispFX(world, item.posX + item.width / 2, item.posY + item.height / 2, item.posZ + item.width / 2, r, 0F, b, s, xm, ym, zm);
				}

				setEntityMotionFromVector(item, target3, 0.3333333F);

				ItemNBTHelper.setInt(stack, TAG_TARGET, item.getEntityId());
				ItemNBTHelper.setDouble(stack, TAG_DIST, length);
			}

			if(item != null)
				ItemNBTHelper.setInt(stack, TAG_TICKS_TILL_EXPIRE, 5);
		}
		return stack;
	}

	public static void setEntityMotionFromVector(Entity entity, Vector3 originalPosVector, float modifier) {
		Vector3 entityVector = Vector3.fromEntityCenter(entity);
		Vector3 finalVector = originalPosVector.copy().subtract(entityVector);

		if(finalVector.mag() > 1)
			finalVector.normalize();

		entity.motionX = finalVector.x * modifier;
		entity.motionY = finalVector.y * modifier;
		entity.motionZ = finalVector.z * modifier;
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

	public static void leftClick(EntityPlayer player) {
		ItemStack stack = player.getHeldItem();
		if(stack != null && stack.getItem() == ModItems.gravityRod) {
			int targetID = ItemNBTHelper.getInt(stack, TAG_TARGET, -1);
			ItemNBTHelper.getDouble(stack, TAG_DIST, -1);
			Entity item = null;
			if(targetID != -1 && player.worldObj.getEntityByID(targetID) != null) {
				Entity taritem = player.worldObj.getEntityByID(targetID);

				boolean found = false;
				Vector3 target = Vector3.fromEntityCenter(player);
				List<Entity> entities = new ArrayList<Entity>();
				int distance = 1;
				while(entities.size() == 0 && distance < 25) {
					target.add(new Vector3(player.getLookVec()).multiply(distance));

					target.y += 0.5;
					entities = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(target.x - RANGE, target.y - RANGE, target.z - RANGE, target.x + RANGE, target.y + RANGE, target.z + RANGE));
					distance++;
					if(entities.contains(taritem))
						found = true;
				}

				if(found) {
					item = player.worldObj.getEntityByID(targetID);
					ItemNBTHelper.setInt(stack, TAG_TARGET, -1);
					ItemNBTHelper.setDouble(stack, TAG_DIST, -1);
					Vector3 moveVector = new Vector3(player.getLookVec().normalize());
					if(item instanceof EntityItem) {
						ObfuscationReflectionHelper.setPrivateValue(EntityItem.class, ((EntityItem) item), 20, LibObfuscation.PICKUP_DELAY);
						float mot = IManaProficiencyArmor.Helper.hasProficiency(player) ? 2.25F : 1.5F;
						item.motionX = moveVector.x * mot;
						item.motionY = moveVector.y;
						item.motionZ = moveVector.z * mot;
						if(!player.worldObj.isRemote) {
							EntityThrownItem thrown = new EntityThrownItem(item.worldObj, item.posX, item.posY, item.posZ, (EntityItem) item);
							item.worldObj.spawnEntityInWorld(thrown);
						}
						item.setDead();
					} else {
						item.motionX = moveVector.x * 3.0F;
						item.motionY = moveVector.y * 1.5F;
						item.motionZ = moveVector.z * 3.0F;
					}
					ItemNBTHelper.setInt(stack, TAG_TICKS_COOLDOWN, 10);
				}
			}
		}
	}
}