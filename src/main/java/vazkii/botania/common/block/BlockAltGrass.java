/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [17/11/2015, 18:33:30 (GMT)]
 */
package vazkii.botania.common.block;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.registry.GameRegistry;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.AltGrassVariant;
import vazkii.botania.common.Botania;
import vazkii.botania.common.item.block.ItemBlockWithMetadataAndName;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibBlockNames;

import java.util.List;
import java.util.Random;

public class BlockAltGrass extends BlockMod implements ILexiconable {

	public BlockAltGrass() {
		super(Material.grass, LibBlockNames.ALT_GRASS);
		setHardness(0.6F);
		setSoundType(SoundType.PLANT);
		setDefaultState(blockState.getBaseState().withProperty(BotaniaStateProps.ALTGRASS_VARIANT, AltGrassVariant.DRY));
		setTickRandomly(true);
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BotaniaStateProps.ALTGRASS_VARIANT);
	}

	@Override
	public boolean isToolEffective(String type, IBlockState state) {
		return type.equals("shovel");
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BotaniaStateProps.ALTGRASS_VARIANT).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(BotaniaStateProps.ALTGRASS_VARIANT, AltGrassVariant.values()[meta]);
	}

	@Override
	public void registerItemForm() {
		GameRegistry.register(new ItemBlockWithMetadataAndName(this), getRegistryName());
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
		for(int i = 0; i < 6; i++)
			list.add(new ItemStack(item, 1, i));
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if(!world.isRemote && state.getBlock() == this && world.getLight(pos.up()) >= 9) {
			AltGrassVariant variant = state.getValue(BotaniaStateProps.ALTGRASS_VARIANT);
			for(int l = 0; l < 4; ++l) {
				BlockPos pos1 = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);
				
				world.getBlockState(pos1.up()).getBlock();

				if(world.getBlockState(pos1).getBlock() == Blocks.dirt && world.getBlockState(pos1).getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT && world.getLight(pos1.up()) >= 4 && world.getBlockLightOpacity(pos1.up()) <= 2)
					world.setBlockState(pos1, this.getDefaultState().withProperty(BotaniaStateProps.ALTGRASS_VARIANT, variant), 1 | 2);
			}
		}
	}

	@Override
    public Item getItemDropped(IBlockState state, Random p_149650_2_, int p_149650_3_) {
        return Blocks.dirt.getItemDropped(state, p_149650_2_, p_149650_3_);
    }

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(this, 1, getMetaFromState(world.getBlockState(pos)));
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
		EnumPlantType type = plantable.getPlantType(world, pos.down());
		return type == EnumPlantType.Plains || type == EnumPlantType.Beach;
	}

	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random r) {
		if (state.getBlock() != this)
			return;
		AltGrassVariant variant = state.getValue(BotaniaStateProps.ALTGRASS_VARIANT);
		switch(variant) {
		case DRY:
			break;
		case GOLDEN:
			break;
		case VIVID:
			break; 
		case SCORCHED:
			if(r.nextInt(80) == 0)
	        	world.spawnParticle(EnumParticleTypes.FLAME, pos.getX() + r.nextFloat(), pos.getY() + 1.1, pos.getZ() + r.nextFloat(), 0, 0, 0);
			break;
		case INFUSED:
			if(r.nextInt(100) == 0)
				Botania.proxy.sparkleFX(world, pos.getX() + r.nextFloat(), pos.getY() + 1.05, pos.getZ() + r.nextFloat(), 0F, 1F, 1F, r.nextFloat() * 0.2F + 1F, 5);
			break; 
		case MUTATED:
			if(r.nextInt(100) == 0) {
				if(r.nextInt(100) > 25)
					Botania.proxy.sparkleFX(world, pos.getX() + r.nextFloat(), pos.getY() + 1.05, pos.getZ() + r.nextFloat(), 1F, 0F, 1F, r.nextFloat() * 0.2F + 1F, 5);
				else Botania.proxy.sparkleFX(world, pos.getX() + r.nextFloat(), pos.getY() + 1.05, pos.getZ() + r.nextFloat(), 1F, 1F, 0F, r.nextFloat() * 0.2F + 1F, 5);
			}
			break;
		}
	}

	@Override
	public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
		return LexiconData.grassSeeds;
	}
}
