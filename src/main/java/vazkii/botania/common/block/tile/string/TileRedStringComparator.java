/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Nov 16, 2014, 10:22:01 PM (GMT)]
 */
package vazkii.botania.common.block.tile.string;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TileRedStringComparator extends TileRedString {

	int comparatorValue = 0;

	@Override
	public void updateEntity() {
		super.updateEntity();
		BlockPos binding = getBinding();
		EnumFacing dir = getOrientation();
		IBlockState state = getStateAtBinding();
		int origVal = comparatorValue;

		if(state.hasComparatorInputOverride()) {
			comparatorValue = state.getComparatorInputOverride(worldObj, binding);
		} else comparatorValue = 0;

		if(origVal != comparatorValue)
			worldObj.updateComparatorOutputLevel(pos, worldObj.getBlockState(pos).getBlock());
	}

	public int getComparatorValue() {
		return comparatorValue;
	}

	@Override
	public boolean acceptBlock(BlockPos pos) {
		return worldObj.getBlockState(pos).hasComparatorInputOverride();
	}

}
