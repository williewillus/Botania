/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Sep 23, 2015, 11:40:51 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.lib.LibItemNames;

public class ItemGaiaHead extends ItemMod {

	public ItemGaiaHead() {
		super(LibItemNames.GAIA_HEAD);
	}

	// Copied from vanila skull itemBlock. Relevant edits are indicated.
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (facing == EnumFacing.DOWN) {
			return EnumActionResult.FAIL;
		} else {
			if (worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos)) {
				facing = EnumFacing.UP;
				pos = pos.down();
			}
			IBlockState iblockstate = worldIn.getBlockState(pos);
			Block block = iblockstate.getBlock();
			boolean flag = block.isReplaceable(worldIn, pos);

			if (!flag) {
				if (!worldIn.getBlockState(pos).getMaterial().isSolid() && !worldIn.isSideSolid(pos, facing, true)) {
					return EnumActionResult.FAIL;
				}

				pos = pos.offset(facing);
			}

			if (playerIn.canPlayerEdit(pos, facing, stack) && Blocks.skull.canPlaceBlockAt(worldIn, pos)) {
				if (worldIn.isRemote) {
					return EnumActionResult.SUCCESS;
				} else {
					worldIn.setBlockState(pos, ModBlocks.gaiaHead.getDefaultState().withProperty(BlockSkull.FACING, facing), 11); // Botania - skull -> gaia head
					int i = 0;

					if (facing == EnumFacing.UP) {
						i = MathHelper.floor_double((double) (playerIn.rotationYaw * 16.0F / 360.0F) + 0.5D) & 15;
					}

					TileEntity tileentity = worldIn.getTileEntity(pos);

					if (tileentity instanceof TileEntitySkull) {
						TileEntitySkull tileentityskull = (TileEntitySkull) tileentity;

						if (stack.getMetadata() == 3) // Botania - do not retrieve skins
						{
							/*GameProfile gameprofile = null;

							if (stack.hasTagCompound())
							{
								NBTTagCompound nbttagcompound = stack.getTagCompound();

								if (nbttagcompound.hasKey("SkullOwner", 10))
								{
									gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
								}
								else if (nbttagcompound.hasKey("SkullOwner", 8) && !nbttagcompound.getString("SkullOwner").isEmpty())
								{
									gameprofile = new GameProfile((UUID)null, nbttagcompound.getString("SkullOwner"));
								}
							}

							tileentityskull.setPlayerProfile(gameprofile);*/
						} else {
							tileentityskull.setType(stack.getMetadata());
						}

						tileentityskull.setSkullRotation(i);
						Blocks.skull.checkWitherSpawn(worldIn, pos, tileentityskull);
					}

					--stack.stackSize;
					return EnumActionResult.SUCCESS;
				}
			} else {
				return EnumActionResult.FAIL;
			}
		}
	}
}
