/**
 * This class was created by <Flaxbeard>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [? (GMT)]
 */
package vazkii.botania.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import vazkii.botania.common.core.handler.MethodHandles;
import vazkii.botania.common.core.helper.Vector3;

import java.util.List;

public class EntityThrownItem extends EntityItem {

	public EntityThrownItem(World par1World) {
		super(par1World);
	}

	public EntityThrownItem(World p_i1710_1_, double p_i1710_2_,
			double p_i1710_4_, double p_i1710_6_, EntityItem item) {
		super(p_i1710_1_, p_i1710_2_, p_i1710_4_, p_i1710_6_, item.getEntityItem());

		int pickupDelay = 0;
		try {
			pickupDelay = (int) MethodHandles.pickupDelay_getter.invokeExact(item);
		} catch (Throwable ignored) {}

		item.setPickupDelay(pickupDelay);
		motionX = item.motionX;
		motionY = item.motionY;
		motionZ = item.motionZ;
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		return true;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		Vec3d vec3 = new Vec3d(posX, posY, posZ);
		Vec3d vec31 = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);

		RayTraceResult RayTraceResult = worldObj.rayTraceBlocks(vec3, vec31);


		if (!worldObj.isRemote)
		{
			Entity entity = null;
			List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(motionX*2, motionY*2, motionZ*2).expand(2.0D, 2.0D, 2.0D));
			double d0 = 0.0D;

			for (int j = 0; j < list.size(); ++j)
			{
				Entity entity1 = (Entity)list.get(j);

				int pickupDelay;
				try {
					pickupDelay = (int) MethodHandles.pickupDelay_getter.invokeExact(this);
				} catch (Throwable ignored) { continue; }

				if (entity1.canBeCollidedWith() && (!(entity1 instanceof EntityPlayer) || pickupDelay == 0))
				{
					float f = 1.0F;
					AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f, f, f);
					RayTraceResult RayTraceResult1 = axisalignedbb.calculateIntercept(vec3, vec31);

					if (RayTraceResult1 != null)
					{
						double d1 = vec3.distanceTo(RayTraceResult1.hitVec);

						if (d1 < d0 || d0 == 0.0D)
						{
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if (entity != null)
			{
				RayTraceResult = new RayTraceResult(entity);
			}
		}

		if (RayTraceResult != null)
		{
			if (RayTraceResult.typeOfHit == net.minecraft.util.math.RayTraceResult.Type.BLOCK && worldObj.getBlockState(RayTraceResult.getBlockPos()).getBlock() == Blocks.portal)
			{
				setPortal(RayTraceResult.getBlockPos());
			}
			else
			{
				if (RayTraceResult.entityHit != null) {
					RayTraceResult.entityHit.attackEntityFrom(DamageSource.magic, 2.0F);
					if (!worldObj.isRemote) {
						Entity item = getEntityItem().getItem().createEntity(worldObj, this, getEntityItem());
						if (item == null) {
							item = new EntityItem(worldObj, posX, posY, posZ, getEntityItem());
							worldObj.spawnEntityInWorld(item);
							item.motionX = motionX*0.25F;
							item.motionY = motionY*0.25F;
							item.motionZ = motionZ*0.25F;

						}
						else
						{
							item.motionX = motionX*0.25F;
							item.motionY = motionY*0.25F;
							item.motionZ = motionZ*0.25F;
						}
					}
					setDead();

				}
			}
		}

		Vector3 vec3m = new Vector3(motionX, motionY, motionZ);
		if (vec3m.mag() < 1.0F) {
			if (!worldObj.isRemote) {
				Entity item = getEntityItem().getItem().createEntity(worldObj, this, getEntityItem());
				if (item == null) {
					item = new EntityItem(worldObj, posX, posY, posZ, getEntityItem());
					worldObj.spawnEntityInWorld(item);
					item.motionX = motionX;
					item.motionY = motionY;
					item.motionZ = motionZ;
				}
				else
				{
					item.motionX = motionX;
					item.motionY = motionY;
					item.motionZ = motionZ;
				}
			}
			setDead();
		}
	}
}
