/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jun 28, 2015, 10:01:01 PM (GMT)]
 */
package vazkii.botania.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.block.tile.TileSimpleInventory;
import vazkii.botania.common.block.tile.TileSparkChanger;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibBlockNames;

import java.util.Random;

public class BlockSparkChanger extends BlockMod implements ILexiconable {

	private static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 1, 3.0/16, 1);

	private final Random random = new Random();

	public BlockSparkChanger() {
		super(Material.rock, LibBlockNames.SPARK_CHANGER);
		setHardness(2.0F);
		setResistance(10.0F);
		setSoundType(SoundType.STONE);
		setDefaultState(blockState.getBaseState().withProperty(BotaniaStateProps.POWERED, true));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB;
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BotaniaStateProps.POWERED);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BotaniaStateProps.POWERED) ? 8 : 0;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(BotaniaStateProps.POWERED, meta == 8);
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isPassable(IBlockAccess p_149655_1_, BlockPos pos) {
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block) {
		boolean power = world.isBlockIndirectlyGettingPowered(pos) > 0 || world.isBlockIndirectlyGettingPowered(pos.up()) > 0;
		boolean powered = state.getValue(BotaniaStateProps.POWERED);

		if(power && !powered) {
			((TileSparkChanger) world.getTileEntity(pos)).doSwap();
			world.setBlockState(pos, state.withProperty(BotaniaStateProps.POWERED, true), 4);
		} else if(!power && powered)
			world.setBlockState(pos, state.withProperty(BotaniaStateProps.POWERED, false), 4);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack pstack, EnumFacing s, float xs, float ys, float zs) {
		TileSparkChanger changer = (TileSparkChanger) world.getTileEntity(pos);
		ItemStack cstack = changer.getItemHandler().getStackInSlot(0);
		if(cstack != null) {
			changer.getItemHandler().setStackInSlot(0, null);
			world.updateComparatorOutputLevel(pos, this);
			changer.markDirty();
			if(!player.inventory.addItemStackToInventory(cstack))
				player.dropPlayerItemWithRandomChoice(cstack, false);
			return true;
		} else if(pstack != null && pstack.getItem() == ModItems.sparkUpgrade) {
			changer.getItemHandler().setStackInSlot(0, pstack.copy().splitStack(1));
			world.updateComparatorOutputLevel(pos, this);
			changer.markDirty();

			pstack.stackSize--;
			if(pstack.stackSize == 0)
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);

			return true;
		}

		return false;
	}

	@Override
	public void breakBlock(World par1World, BlockPos pos, IBlockState state) {
		TileSimpleInventory inv = (TileSimpleInventory) par1World.getTileEntity(pos);

		if (inv != null) {
			for (int j1 = 0; j1 < inv.getSizeInventory(); ++j1) {
				ItemStack itemstack = inv.getItemHandler().getStackInSlot(j1);

				if (itemstack != null) {
					float f = random.nextFloat() * 0.8F + 0.1F;
					float f1 = random.nextFloat() * 0.8F + 0.1F;
					EntityItem entityitem;

					for (float f2 = random.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; par1World.spawnEntityInWorld(entityitem)) {
						int k1 = random.nextInt(21) + 10;

						if (k1 > itemstack.stackSize)
							k1 = itemstack.stackSize;

						itemstack.stackSize -= k1;
						entityitem = new EntityItem(par1World, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, new ItemStack(itemstack.getItem(), k1, itemstack.getItemDamage()));
						float f3 = 0.05F;
						entityitem.motionX = (float)random.nextGaussian() * f3;
						entityitem.motionY = (float)random.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float)random.nextGaussian() * f3;

						if (itemstack.hasTagCompound())
							entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
					}
				}
			}

			par1World.updateComparatorOutputLevel(pos, state.getBlock());
		}

		super.breakBlock(par1World, pos, state);
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		TileSparkChanger changer = (TileSparkChanger) world.getTileEntity(pos);
		ItemStack stack = changer.getItemHandler().getStackInSlot(0);
		if(stack == null)
			return 0;
		return stack.getItemDamage() + 1;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileSparkChanger();
	}

	@Override
	public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
		return LexiconData.sparkChanger;
	}

}
