/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 22, 2015, 7:46:55 PM (GMT)]
 */
package vazkii.botania.common.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.BotaniaCreativeTab;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.item.block.ItemBlockWithMetadataAndName;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.common.lib.LibMisc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class BlockModDoubleFlower extends BlockDoublePlant implements ILexiconable {
	private static final int COUNT = 8;

	private final boolean second;

	public BlockModDoubleFlower(boolean second) {
		this.second = second;
		this.setHardness(0.0F);
		this.setSoundType(SoundType.PLANT);
		String name = LibBlockNames.DOUBLE_FLOWER + (second ? 2 : 1);
		GameRegistry.register(this, new ResourceLocation(LibMisc.MOD_ID, name));
		GameRegistry.register(new ItemBlockWithMetadataAndName(this), getRegistryName());
		setUnlocalizedName(name);
		setHardness(0F);
		setTickRandomly(false);
		setCreativeTab(BotaniaCreativeTab.INSTANCE);
	}

	@Override
	public abstract BlockStateContainer createBlockState();

	@Override
	public abstract int getMetaFromState(IBlockState state);

	@Override
	public abstract IBlockState getStateFromMeta(int meta);

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean fuckifiknow) {
		return false;
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return false;
	}

	@Override
	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		return true;
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if(state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
			if(world.getBlockState(pos.down()).getBlock() == this) {
				if (!player.capabilities.isCreativeMode) {
					// IBlockState iblockstate = worldIn.getBlockState(pos.down());
					// BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = (BlockDoublePlant.EnumPlantType) iblockstate.getValue(VARIANT);

					//if (blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.FERN && blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.GRASS) {
						// worldIn.destroyBlock(pos.down(), true);
					//} else if (!world.isRemote) {
					//	if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.shears) {
					//		this.onHarvest(worldIn, pos, iblockstate, player);
					//		world.setBlockToAir(pos.down());
					//	} else {
					//		world.destroyBlock(pos.down(), true);
					//	}
					//} else {
						world.setBlockToAir(pos.down());
					//}
				} else {
					world.setBlockToAir(pos.down());
				}
			}
		} else if(player.capabilities.isCreativeMode && world.getBlockState(pos.up()).getBlock() == this)
			world.setBlockState(pos.up(), Blocks.air.getDefaultState(), 2);
		player.addStat(StatList.getBlockStats(this));
		//super.onBlockHarvested(p_149681_1_, p_149681_2_, p_149681_3_, p_149681_4_, p_149681_5_, p_149681_6_);
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
		return true;
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<>();
		IBlockState state = world.getBlockState(pos);
		IBlockState stateBelow = world.getBlockState(pos.down());

		if (stateBelow.getBlock() == this && stateBelow.getValue(HALF) == EnumBlockHalf.LOWER && state.getValue(HALF) == EnumBlockHalf.UPPER) {
			ret.add(new ItemStack(this, 1, getMetaFromState(world.getBlockState(pos.down()))));
		}

		if (state.getValue(HALF) == EnumBlockHalf.LOWER) {
			ret.add(new ItemStack(this, 1, getMetaFromState(state)));
		}

		return ret;
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return ImmutableList.of();
	}

	@Override
	public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List<ItemStack> p_149666_3_) {
		for(int i = 0; i < COUNT; ++i)
			p_149666_3_.add(new ItemStack(p_149666_1_, 1, i));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Block.EnumOffsetType getOffsetType()
	{
		return Block.EnumOffsetType.NONE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random par5Random) {
		state = getActualState(state, world, pos);
		int hex = state.getValue(second ? BotaniaStateProps.DOUBLEFLOWER_VARIANT_2 : BotaniaStateProps.DOUBLEFLOWER_VARIANT_1).getMapColor().colorValue;
		int r = (hex & 0xFF0000) >> 16;
		int g = (hex & 0xFF00) >> 8;
		int b = (hex & 0xFF);

		if(par5Random.nextDouble() < ConfigHandler.flowerParticleFrequency)
			Botania.proxy.sparkleFX(world, pos.getX() + 0.3 + par5Random.nextFloat() * 0.5, pos.getY() + 0.5 + par5Random.nextFloat() * 0.5, pos.getZ() + 0.3 + par5Random.nextFloat() * 0.5, r / 255F, g / 255F, b / 255F, par5Random.nextFloat(), 5);

	}

	@Override
	public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
		return LexiconData.flowers;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
			IBlockState iblockstate = worldIn.getBlockState(pos.down());

			if (iblockstate.getBlock() == this) {
				PropertyEnum<EnumDyeColor> prop = second ? BotaniaStateProps.DOUBLEFLOWER_VARIANT_2 : BotaniaStateProps.DOUBLEFLOWER_VARIANT_1;
				state = state.withProperty(prop, iblockstate.getValue(prop));
			}
		}

		return state.withProperty(VARIANT, EnumPlantType.SUNFLOWER).withProperty(FACING, EnumFacing.SOUTH);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		state = state.getBlock().getActualState(state, world, pos);
		PropertyEnum<EnumDyeColor> prop = second ? BotaniaStateProps.DOUBLEFLOWER_VARIANT_2 : BotaniaStateProps.DOUBLEFLOWER_VARIANT_1;
		return new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getValue(prop).ordinal() - (second ? 8 : 0));
	}

}
