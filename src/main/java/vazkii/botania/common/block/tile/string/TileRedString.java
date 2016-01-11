/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Nov 14, 2014, 5:04:22 PM (GMT)]
 */
package vazkii.botania.common.block.tile.string;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ITickable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.wand.ITileBound;
import vazkii.botania.common.block.string.BlockRedString;
import vazkii.botania.common.block.tile.TileMod;

public abstract class TileRedString extends TileMod implements ITileBound, ITickable {

	private BlockPos binding;

	@Override
	public void update() {
		if (!(worldObj.getBlockState(getPos()).getBlock() instanceof BlockRedString))
			return;

		EnumFacing dir = getOrientation();
		BlockPos pos_ = getPos();
		int range = getRange();
		BlockPos currBinding = getBinding();
		setBinding(null);

		for(int i = 0; i < range; i++) {
			pos_ = pos_.offset(dir);
			if(worldObj.isAirBlock(pos_))
				continue;

			TileEntity tile = worldObj.getTileEntity(pos_);
			if(tile instanceof TileRedString)
				continue;

			if(acceptBlock(pos_)) {
				setBinding(new BlockPos(pos_));
				if(currBinding == null || !currBinding.equals(pos_))
					onBound(pos_);
				break;
			}
		}
	}

	public int getRange() {
		return 8;
	}

	public abstract boolean acceptBlock(BlockPos pos);

	public void onBound(BlockPos pos) {
		// NO-OP
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public BlockPos getBinding() {
		return binding;
	}

	public void setBinding(BlockPos binding) {
		this.binding = binding;
	}

	public EnumFacing getOrientation() {
		return worldObj.getBlockState(getPos()).getValue(BotaniaStateProps.FACING);
	}

	public TileEntity getTileAtBinding() {
		BlockPos binding = getBinding();
		return binding == null ? null : worldObj.getTileEntity(binding);
	}

	public Block getBlockAtBinding() {
		BlockPos binding = getBinding();
		return binding == null ? Blocks.air : worldObj.getBlockState(binding).getBlock();
	}

}
