/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jun 26, 2014, 7:32:16 PM (GMT)]
 */
package vazkii.botania.common.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;

import java.util.Map;

public class EntityVineBall extends EntityThrowable {

	private static final DataParameter<Float> GRAVITY = EntityDataManager.createKey(EntityVineBall.class, DataSerializers.FLOAT);
	private static final Map<EnumFacing, PropertyBool> propMap = ImmutableMap.of(EnumFacing.NORTH, BlockVine.NORTH, EnumFacing.SOUTH, BlockVine.SOUTH,
			EnumFacing.WEST, BlockVine.WEST, EnumFacing.EAST, BlockVine.EAST);

	public EntityVineBall(World par1World) {
		super(par1World);
	}

	public EntityVineBall(EntityLivingBase thrower, boolean gravity) {
		super(thrower.worldObj, thrower);
		dataManager.set(GRAVITY, gravity ? 0.03F : 0F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(GRAVITY, 0F);
	}

	@Override
	protected void onImpact(RayTraceResult var1) {

		if(worldObj.isRemote) {
			for(int j = 0; j < 16; j++) {
				this.worldObj.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX, this.posY, this.posZ, Math.random() * 0.2 - 0.1, Math.random() * 0.25, Math.random() * 0.2 - 0.1, Item.getIdFromItem(ModItems.vineBall));
			}
		} else {
			if(var1 != null) {
				EnumFacing dir = var1.sideHit;

				if(dir != null && dir.getAxis() != EnumFacing.Axis.Y) {
					BlockPos pos = var1.getBlockPos().offset(dir);
					while(pos.getY() > 0) {
						IBlockState state = worldObj.getBlockState(pos);
						Block block = state.getBlock();
						if(block.isAir(state, worldObj, pos)) {
							IBlockState stateSet = ModBlocks.solidVines.getDefaultState().withProperty(propMap.get(dir.getOpposite()), true);
							worldObj.setBlockState(pos, stateSet, 1 | 2);
							worldObj.playAuxSFX(2001, pos, Block.getStateId(stateSet));
							pos = pos.down();
						} else break;
					}
				}

			}

			setDead();
		}
	}

	@Override
	protected float getGravityVelocity() {
		return dataManager.get(GRAVITY);
	}

}
