/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 26, 2015, 5:59:21 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.lib.LibItemNames;

public class ItemOvergrowthSeed extends ItemMod {

	public ItemOvergrowthSeed() {
		super(LibItemNames.OVERGROWTH_SEED);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xs, float ys, float zs) {
		IBlockState state = world.getBlockState(pos);
		ItemStack blockStack = new ItemStack(state.getBlock());
		int[] ids = OreDictionary.getOreIDs(blockStack);
		for(int i : ids) {
			String name = OreDictionary.getOreName(i);
			if(name.equals("grass")) {
				world.playAuxSFX(2001, pos, Block.getStateId(state));
				world.setBlockState(pos, ModBlocks.enchantedSoil.getDefaultState());
				stack.stackSize--;

				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

}
