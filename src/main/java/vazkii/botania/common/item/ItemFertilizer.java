/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 29, 2014, 10:41:52 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.lib.LibItemNames;

import java.util.ArrayList;
import java.util.List;

public class ItemFertilizer extends ItemMod {

	public ItemFertilizer() {
		super(LibItemNames.FERTILIZER);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, BlockPos pos, EnumHand hand, EnumFacing side, float par8, float par9, float par10) {
		final int range = 3;
		if(!par3World.isRemote) {
			List<BlockPos> validCoords = new ArrayList<>();

			for(int i = -range - 1; i < range; i++)
				for(int j = -range - 1; j < range; j++) {
					for(int k = 2; k >= -2; k--) {
						BlockPos pos_ = pos.add(i + 1, k + 1, j + 1);
						if(par3World.isAirBlock(pos_) && (!par3World.provider.getHasNoSky() || pos_.getY() < 255) && ModBlocks.flower.canPlaceBlockAt(par3World, pos_))
							validCoords.add(pos_);
					}
				}

			int flowerCount = Math.min(validCoords.size(), par3World.rand.nextBoolean() ? 3 : 4);
			for(int i = 0; i < flowerCount; i++) {
				BlockPos coords = validCoords.get(par3World.rand.nextInt(validCoords.size()));
				validCoords.remove(coords);
				par3World.setBlockState(coords, ModBlocks.flower.getDefaultState().withProperty(BotaniaStateProps.COLOR, EnumDyeColor.byMetadata(par3World.rand.nextInt(16))), 1 | 2);
			}
			par1ItemStack.stackSize--;
		} else {
			for(int i = 0; i < 15; i++) {
				double x = pos.getX() - range + par3World.rand.nextInt(range * 2 + 1) + Math.random();
				double y = pos.getY() + 1;
				double z = pos.getZ() - range + par3World.rand.nextInt(range * 2 + 1) + Math.random();
				float red = (float) Math.random();
				float green = (float) Math.random();
				float blue = (float) Math.random();
				Botania.proxy.wispFX(par3World, x, y, z, red, green, blue, 0.15F + (float) Math.random() * 0.25F, -(float) Math.random() * 0.1F - 0.05F);
			}
		}

		return EnumActionResult.SUCCESS;
	}
}
