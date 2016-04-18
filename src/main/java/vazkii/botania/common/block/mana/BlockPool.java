/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 26, 2014, 12:22:58 AM (GMT)]
 */
package vazkii.botania.common.block.mana;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.PoolVariant;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.api.wand.IWandable;
import vazkii.botania.common.achievement.ICraftAchievement;
import vazkii.botania.common.achievement.ModAchievements;
import vazkii.botania.common.block.BlockMod;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.item.block.ItemBlockPool;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibBlockNames;

import java.util.ArrayList;
import java.util.List;

public class BlockPool extends BlockMod implements IWandHUD, IWandable, ILexiconable, ICraftAchievement {

	private static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);

	private boolean lastFragile = false;

	public BlockPool() {
		super(Material.rock, LibBlockNames.POOL);
		setHardness(2.0F);
		setResistance(10.0F);
		setSoundType(SoundType.STONE);
		setDefaultState(blockState.getBaseState().withProperty(BotaniaStateProps.POOL_VARIANT, PoolVariant.DEFAULT));
		BotaniaAPI.blacklistBlockFromMagnet(this, Short.MAX_VALUE);
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BotaniaStateProps.POOL_VARIANT);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BotaniaStateProps.POOL_VARIANT).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta > PoolVariant.values().length) {
			meta = 0;
		}
		return getDefaultState().withProperty(BotaniaStateProps.POOL_VARIANT, PoolVariant.values()[meta]);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB;
	}

	@Override
	public void registerItemForm() {
		GameRegistry.register(new ItemBlockPool(this), getRegistryName());
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getBlock().getMetaFromState(state);
	}

	@Override
	public void breakBlock(World par1World, BlockPos pos, IBlockState state) {
		TilePool pool = (TilePool) par1World.getTileEntity(pos);
		lastFragile = pool.fragile;
		super.breakBlock(par1World, pos, state);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ArrayList<ItemStack> drops = new ArrayList<>();

		if(!lastFragile)
			drops.add(new ItemStack(this, 1, state.getBlock().getMetaFromState(state)));

		return drops;
	}

	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2, List<ItemStack> par3) {
		par3.add(new ItemStack(par1, 1, 0));
		par3.add(new ItemStack(par1, 1, 2));
		par3.add(new ItemStack(par1, 1, 3));
		par3.add(new ItemStack(par1, 1, 1));
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TilePool();
	}

	@Override
	public void onEntityCollidedWithBlock(World par1World, BlockPos pos, IBlockState state, Entity par5Entity) {
		if(par5Entity instanceof EntityItem) {
			TilePool tile = (TilePool) par1World.getTileEntity(pos);
			if(tile.collideEntityItem((EntityItem) par5Entity))
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(par1World, pos);
		}
	}

	private static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0, 0, 0, 1, 1/16.0, 1);
	private static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0, 0, 15/16.0, 1, 0.5, 1);
	private static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0, 0, 0, 1, 0.5, 1/16.0);
	private static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0, 0, 0, 1/16.0, 0.5, 1);
	private static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(15/16.0, 0, 0, 1, 0.5, 1);


	@Override
	public void addCollisionBoxToList(IBlockState state, World p_149743_1_, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> boxes, Entity entity) {
		addCollisionBoxToList(pos, entityBox, boxes, BOTTOM_AABB);
		addCollisionBoxToList(pos, entityBox, boxes, NORTH_AABB);
		addCollisionBoxToList(pos, entityBox, boxes, SOUTH_AABB);
		addCollisionBoxToList(pos, entityBox, boxes, WEST_AABB);
		addCollisionBoxToList(pos, entityBox, boxes, EAST_AABB);
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.DOWN;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World par1World, BlockPos pos) {
		TilePool pool = (TilePool) par1World.getTileEntity(pos);
		int val = (int) ((double) pool.getCurrentMana() / (double) pool.manaCap * 15.0);
		if(pool.getCurrentMana() > 0)
			val = Math.max(val, 1);

		return val;
	}

	@Override
	public void renderHUD(Minecraft mc, ScaledResolution res, World world, BlockPos pos) {
		((TilePool) world.getTileEntity(pos)).renderHUD(mc, res);
	}

	@Override
	public boolean onUsedByWand(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing side) {
		((TilePool) world.getTileEntity(pos)).onWanded(player, stack);
		return true;
	}

	@Override
	public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
		return world.getBlockState(pos).getValue(BotaniaStateProps.POOL_VARIANT) == PoolVariant.FABULOUS ? LexiconData.rainbowRod : LexiconData.pool;
	}

	@Override
	public Achievement getAchievementOnCraft(ItemStack stack, EntityPlayer player, IInventory matrix) {
		return ModAchievements.manaPoolPickup;
	}
}
