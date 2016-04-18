/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Sep 21, 2015, 4:56:52 PM (GMT)]
 */
package vazkii.botania.common.block.string;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.block.tile.string.TileRedString;
import vazkii.botania.common.block.tile.string.TileRedStringInterceptor;
import vazkii.botania.common.lib.LibBlockNames;

import java.util.Random;

public class BlockRedStringInterceptor extends BlockRedString {

	public BlockRedStringInterceptor() {
		super(LibBlockNames.RED_STRING_INTERCEPTOR);
		MinecraftForge.EVENT_BUS.register(this);
		setDefaultState(blockState.getBaseState().withProperty(BotaniaStateProps.FACING, EnumFacing.DOWN).withProperty(BotaniaStateProps.POWERED, false));
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BotaniaStateProps.FACING, BotaniaStateProps.POWERED);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = state.getValue(BotaniaStateProps.FACING).getIndex();
		if (state.getValue(BotaniaStateProps.POWERED)) {
			meta |= 8;
		} else {
			meta &= -9;
		}
		return meta;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean powered = (meta & 8) != 0;
		meta &= -9;
		EnumFacing facing = EnumFacing.getFront(meta);
		return getDefaultState().withProperty(BotaniaStateProps.FACING, facing).withProperty(BotaniaStateProps.POWERED, powered);
	}

	@SubscribeEvent
	public void onInteract(PlayerInteractEvent.RightClickBlock event) {
		TileRedStringInterceptor.onInteract(event.getEntityPlayer(), event.getWorld(), event.getPos(), event.getHand());
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return state.getValue(BotaniaStateProps.POWERED) ? 15 : 0;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random update) {
		world.setBlockState(pos, state.withProperty(BotaniaStateProps.POWERED, false), 1 | 2);
	}

	@Override
	public int tickRate(World p_149738_1_) {
		return 2;
	}

	@Override
	public TileRedString createTileEntity(World world, IBlockState state) {
		return new TileRedStringInterceptor();
	}

}
