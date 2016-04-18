/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jul 12, 2014, 7:59:00 PM (GMT)]
 */
package vazkii.botania.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import vazkii.botania.api.sound.BotaniaSoundEvents;
import vazkii.botania.common.Botania;

import java.util.List;

public class EntityMagicLandmine extends Entity {

	public EntityDoppleganger summoner;

	public EntityMagicLandmine(World par1World) {
		super(par1World);
		setSize(0F, 0F);
	}

	@Override
	public void onUpdate() {
		motionX = 0;
		motionY = 0;
		motionZ = 0;
		super.onUpdate();

		float range = 2.5F;

		float r = 0.2F;
		float g = 0F;
		float b = 0.2F;

		//Botania.proxy.wispFX(worldObj, posX, posY, posZ, r, g, b, 0.6F, -0.2F, 1);
		for(int i = 0; i < 6; i++)
			Botania.proxy.wispFX(worldObj, posX - range + Math.random() * range * 2, posY, posZ - range + Math.random() * range * 2, r, g, b, 0.4F, -0.015F, 1);

		if(ticksExisted >= 55) {
			worldObj.playSound(null, posX, posY, posZ, BotaniaSoundEvents.gaiaTrap, SoundCategory.NEUTRAL, 0.3F, 1F);

			float m = 0.35F;
			g = 0.4F;
			for(int i = 0; i < 25; i++)
				Botania.proxy.wispFX(worldObj, posX, posY + 1, posZ, r, g, b, 0.5F, (float) (Math.random() - 0.5F) * m, (float) (Math.random() - 0.5F) * m, (float) (Math.random() - 0.5F) * m);

			if(!worldObj.isRemote) {
				List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(posX - range, posY - range, posZ - range, posX + range, posY + range, posZ + range));
				for(EntityPlayer player : players) {
					player.attackEntityFrom(summoner == null ? DamageSource.generic : DamageSource.causeMobDamage(summoner), 10);
					player.addPotionEffect(new PotionEffect(MobEffects.blindness, 25, 0));
					PotionEffect wither = new PotionEffect(MobEffects.wither, 70, 3);
					wither.getCurativeItems().clear();
					player.addPotionEffect(wither);
				}
			}

			setDead();
		}
	}

	@Override
	protected void entityInit() {
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound var1) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound var1) {
	}

}
