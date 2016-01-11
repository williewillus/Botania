/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Aug 28, 2015, 5:27:43 PM (GMT)]
 */
package vazkii.botania.common.block.tile.mana;

import net.minecraft.block.BlockFurnace;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.common.block.subtile.functional.SubTileExoflame;
import vazkii.botania.common.block.tile.TileMod;

public class TileBellows extends TileMod {

	private static final String TAG_ACTIVE = "active";

	public float movePos;
	public boolean active = false;
	public float moving = 0F;

	public void interact() {
		if(moving == 0F)
			setActive(true);
	}

	@Override
	public void updateEntity() {
		boolean disable = true;
		TileEntity tile = getLinkedTile();
		if(!active && tile instanceof TilePool) {
			TilePool pool = (TilePool) tile;
			boolean transfer = pool.isDoingTransfer;
			if(transfer) {
				if(!active && pool.ticksDoingTransfer >= getBlockMetadata() * 2 - 2)
					setActive(true);
				disable = false;
			}
		}

		float max = 0.9F;
		float min = 0F;

		float incr = max / 20F;

		if(movePos < max && active && moving >= 0F) {
			if(moving == 0F)
				worldObj.playSoundEffect(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, "botania:bellows", 0.1F, 3F);

			if(tile instanceof TileEntityFurnace) {
				TileEntityFurnace furnace = (TileEntityFurnace) tile;
				if(SubTileExoflame.canFurnaceSmelt(furnace)) {
					furnace.setField(2, Math.min(199, furnace.getField(2) + 20)); // cookTime
					furnace.setField(0, Math.max(0, furnace.getField(0) - 10)); // burnTime
				}

				if(furnace.getBlockType() == Blocks.lit_furnace) {
					// Copypasta from BlockFurnace
					EnumFacing enumfacing = worldObj.getBlockState(furnace.getPos()).getValue(BlockFurnace.FACING);
					double d0 = (double)pos.getX() + 0.5D;
					double d1 = (double)pos.getY() + worldObj.rand.nextDouble() * 6.0D / 16.0D;
					double d2 = (double)pos.getZ() + 0.5D;
					double d3 = 0.52D;
					double d4 = worldObj.rand.nextDouble() * 0.6D - 0.3D;

					switch (enumfacing)
					{
						case WEST:
							worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 - d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D, new int[0]);
							worldObj.spawnParticle(EnumParticleTypes.FLAME, d0 - d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D, new int[0]);
							break;
						case EAST:
							worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D, new int[0]);
							worldObj.spawnParticle(EnumParticleTypes.FLAME, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D, new int[0]);
							break;
						case NORTH:
							worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 - d3, 0.0D, 0.0D, 0.0D, new int[0]);
							worldObj.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 - d3, 0.0D, 0.0D, 0.0D, new int[0]);
							break;
						case SOUTH:
							worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 + d3, 0.0D, 0.0D, 0.0D, new int[0]);
							worldObj.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 + d3, 0.0D, 0.0D, 0.0D, new int[0]);
					}
				}
			}

			movePos += incr * 3;
			moving = incr * 3;
			if(movePos >= max) {
				movePos = Math.min(max, movePos);
				moving = 0F;
				if(disable)
					setActive(false);
			}
		} else if(movePos > min) {
			movePos -= incr;
			moving = -incr;
			if(movePos <= min) {
				movePos = Math.max(min, movePos);
				moving = 0F;
			}
		}

	}

	public TileEntity getLinkedTile() {
		return null;
//		EnumFacing side = ((EnumFacing) worldObj.getBlockState(getPos()).getValue(BotaniaStateProps.CARDINALS));
//		return worldObj.getTileEntity(getPos().offset(side));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound cmp) {
		cmp.setBoolean(TAG_ACTIVE, active);
	}

	@Override
	public void readCustomNBT(NBTTagCompound cmp) {
		active = cmp.getBoolean(TAG_ACTIVE);
	}

	public void setActive(boolean active) {
		if(!worldObj.isRemote) {
			boolean diff = this.active != active;
			this.active = active;
			if(diff)
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldObj, pos);
		}
	}


}
