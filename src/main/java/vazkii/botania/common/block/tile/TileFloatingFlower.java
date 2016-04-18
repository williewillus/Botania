/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jul 8, 2014, 10:17:28 PM (GMT)]
 */
package vazkii.botania.common.block.tile;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import vazkii.botania.api.item.IFloatingFlower;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.block.ModBlocks;

public class TileFloatingFlower extends TileMod implements IFloatingFlower {

	public static final String TAG_ISLAND_TYPE = "islandType";
	public static ItemStack forcedStack = null;
	IslandType type = IslandType.GRASS;

	@Override
	public ItemStack getDisplayStack() {
		if(forcedStack != null) {
			ItemStack retStack = forcedStack;
			forcedStack = null;
			return retStack;
		}
		EnumDyeColor color = worldObj.getBlockState(getPos()).getBlock() != ModBlocks.floatingFlower ? EnumDyeColor.WHITE
				: worldObj.getBlockState(getPos()).getValue(BotaniaStateProps.COLOR);
		return new ItemStack(ModBlocks.shinyFlower, 1, color.getMetadata());
	}

	@Override
	public IslandType getIslandType() {
		return type;
	}

	@Override
	public void setIslandType(IslandType type) {
		this.type = type;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound cmp) {
		cmp.setString(TAG_ISLAND_TYPE, type.toString());
	}

	@Override
	public void readCustomNBT(NBTTagCompound cmp) {
		type = IslandType.ofType(cmp.getString(TAG_ISLAND_TYPE));
	}

}
