/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jun 9, 2014, 8:51:55 PM (GMT)]
 */
package vazkii.botania.common.block.tile;

import com.google.common.base.Function;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.lexicon.ILexicon;
import vazkii.botania.api.lexicon.multiblock.Multiblock;
import vazkii.botania.api.lexicon.multiblock.MultiblockSet;
import vazkii.botania.api.lexicon.multiblock.component.StateInsensitiveComponent;
import vazkii.botania.api.recipe.ElvenPortalUpdateEvent;
import vazkii.botania.api.recipe.IElvenItem;
import vazkii.botania.api.recipe.RecipeElvenTrade;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.AlfPortalState;
import vazkii.botania.api.state.enums.LivingWoodVariant;
import vazkii.botania.api.state.enums.PylonVariant;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.item.ItemLexicon;
import vazkii.botania.common.lexicon.LexiconData;

import java.util.ArrayList;
import java.util.List;

public class TileAlfPortal extends TileMod {

	private static final BlockPos[] LIVINGWOOD_POSITIONS = {
		new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(-2, 1, 0),
		new BlockPos(2, 1, 0), new BlockPos(-2, 3, 0), new BlockPos(2, 3, 0),
		new BlockPos(-1, 4, 0), new BlockPos(1, 4, 0)
	};

	private static final BlockPos[] GLIMMERING_LIVINGWOOD_POSITIONS = {
		new BlockPos(-2, 2, 0), new BlockPos(2, 2, 0), new BlockPos(0, 4, 0)
	};

	private static final BlockPos[] PYLON_POSITIONS = {
		new BlockPos(-3, 1, 3), new BlockPos(3, 1, 3)
	};

	private static final BlockPos[] POOL_POSITIONS = {
		new BlockPos(-3, 0, 3), new BlockPos(3, 0, 3)
	};

	private static final BlockPos[] AIR_POSITIONS = {
		new BlockPos(-1, 1, 0), new BlockPos(0, 1, 0), new BlockPos(1, 1, 0),
		new BlockPos(-1, 2, 0), new BlockPos(0, 2, 0), new BlockPos(1, 2, 0),
		new BlockPos(-1, 3, 0), new BlockPos(0, 3, 0), new BlockPos(1, 3, 0)
	};

	private static final String TAG_TICKS_OPEN = "ticksOpen";
	private static final String TAG_TICKS_SINCE_LAST_ITEM = "ticksSinceLastItem";
	private static final String TAG_STACK_COUNT = "stackCount";
	private static final String TAG_STACK = "portalStack";
	private static final String TAG_PORTAL_FLAG = "_elvenPortal";

	List<ItemStack> stacksIn = new ArrayList<>();

	public int ticksOpen = 0;
	int ticksSinceLastItem = 0;
	private boolean closeNow = false;
	private boolean hasUnloadedParts = false;

	private static final Function<BlockPos, BlockPos> CONVERTER_X_Z = input -> new BlockPos(input.getZ(), input.getY(), input.getX());

	private static final Function<double[], double[]> CONVERTER_X_Z_FP = input -> new double[] { input[2], input[1], input[0] };

	private static final Function<BlockPos, BlockPos> CONVERTER_Z_SWAP = input -> new BlockPos(input.getX(), input.getY(), -input.getZ());

	public static MultiblockSet makeMultiblockSet() {
		Multiblock mb = new Multiblock();

		for(BlockPos l : LIVINGWOOD_POSITIONS)
			mb.addComponent(l.up(), ModBlocks.livingwood.getDefaultState());
		for(BlockPos g : GLIMMERING_LIVINGWOOD_POSITIONS)
			mb.addComponent(g.up(), ModBlocks.livingwood.getDefaultState().withProperty(BotaniaStateProps.LIVINGWOOD_VARIANT, LivingWoodVariant.GLIMMERING));
		for(BlockPos p : PYLON_POSITIONS)
			mb.addComponent(new BlockPos(-p.getX(), p.getY() + 1, -p.getZ()), ModBlocks.pylon.getDefaultState().withProperty(BotaniaStateProps.PYLON_VARIANT, PylonVariant.NATURA));
		for(BlockPos p : POOL_POSITIONS)
			mb.addComponent(new StateInsensitiveComponent(new BlockPos(-p.getX(), p.getY() + 1, -p.getZ()), ModBlocks.pool));

		mb.addComponent(new BlockPos(0, 1, 0), ModBlocks.alfPortal.getDefaultState());
		mb.setRenderOffset(new BlockPos(0, -1, 0));

		return mb.makeSet();
	}

	@Override
	public void updateEntity() {
		IBlockState iBlockState = worldObj.getBlockState(getPos());
		if(iBlockState.getValue(BotaniaStateProps.ALFPORTAL_STATE) == AlfPortalState.OFF) {
			ticksOpen = 0;
			return;
		}
		AlfPortalState state = iBlockState.getValue(BotaniaStateProps.ALFPORTAL_STATE);
		AlfPortalState newState = getValidState();

		if(!hasUnloadedParts) {
			ticksOpen++;

			AxisAlignedBB aabb = getPortalAABB();
			boolean open = ticksOpen > 60;
			ElvenPortalUpdateEvent event = new ElvenPortalUpdateEvent(this, aabb, open, stacksIn);
			MinecraftForge.EVENT_BUS.post(event);

			if(ticksOpen > 60) {
				ticksSinceLastItem++;
				if(ConfigHandler.elfPortalParticlesEnabled)
					blockParticle(state);

				List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class, aabb);
				if(!worldObj.isRemote)
					for(EntityItem item : items) {
						if(item.isDead)
							continue;

						ItemStack stack = item.getEntityItem();
						if(stack != null && (!(stack.getItem() instanceof IElvenItem) || !((IElvenItem) stack.getItem()).isElvenItem(stack)) && !item.getEntityData().hasKey(TAG_PORTAL_FLAG)) {
							item.setDead();
							addItem(stack);
							ticksSinceLastItem = 0;
						}
					}

				if(ticksSinceLastItem >= 4) {
					if(!worldObj.isRemote)
						resolveRecipes();
				}
			}
		} else closeNow = false;

		if(closeNow) {
			worldObj.setBlockState(getPos(), ModBlocks.alfPortal.getDefaultState(), 1 | 2);
			for(int i = 0; i < 36; i++)
				blockParticle(state);
			closeNow = false;
		} else if(newState != state) {
			if(newState == AlfPortalState.OFF)
				for(int i = 0; i < 36; i++)
					blockParticle(state);
			worldObj.setBlockState(getPos(), worldObj.getBlockState(getPos()).withProperty(BotaniaStateProps.ALFPORTAL_STATE, newState), 1 | 2);
		}

		hasUnloadedParts = false;
	}

	private void blockParticle(AlfPortalState state) {
		int i = worldObj.rand.nextInt(AIR_POSITIONS.length);
		double[] pos = new double[] {
				AIR_POSITIONS[i].getX() + 0.5F, AIR_POSITIONS[i].getY() + 0.5F, AIR_POSITIONS[i].getZ() + 0.5F
		};
		if(state == AlfPortalState.ON_X)
			pos = CONVERTER_X_Z_FP.apply(pos);

		float motionMul = 0.2F;
		Botania.proxy.wispFX(getWorld(), getPos().getX() + pos[0], getPos().getY() + pos[1], getPos().getZ() + pos[2], (float) (Math.random() * 0.25F), (float) (Math.random() * 0.5F + 0.5F), (float) (Math.random() * 0.25F), (float) (Math.random() * 0.15F + 0.1F), (float) (Math.random() - 0.5F) * motionMul, (float) (Math.random() - 0.5F) * motionMul, (float) (Math.random() - 0.5F) * motionMul);
	}

	public boolean onWanded() {
		AlfPortalState state = worldObj.getBlockState(getPos()).getValue(BotaniaStateProps.ALFPORTAL_STATE);
		if(state == AlfPortalState.OFF) {
			AlfPortalState newState = getValidState();
			if(newState != AlfPortalState.OFF) {
				worldObj.setBlockState(getPos(), worldObj.getBlockState(getPos()).withProperty(BotaniaStateProps.ALFPORTAL_STATE, newState), 1 | 2);
				return true;
			}
		}

		return false;
	}

	AxisAlignedBB getPortalAABB() {
		AxisAlignedBB aabb = new AxisAlignedBB(pos.add(-1, 1, 0), pos.add(2, 4, 1));
		if(worldObj.getBlockState(getPos()).getValue(BotaniaStateProps.ALFPORTAL_STATE) == AlfPortalState.ON_X)
			aabb = new AxisAlignedBB(pos.add(0, 1, -1), pos.add(1, 4, 2));

		return aabb;
	}

	void addItem(ItemStack stack) {
		int size = stack.stackSize;
		stack.stackSize = 1;
		for(int i = 0; i < size; i++)
			stacksIn.add(stack.copy());
	}

	void resolveRecipes() {
		int i = 0;
		for(ItemStack stack : stacksIn) {
			if(stack != null && stack.getItem() instanceof ILexicon) {
				((ILexicon) stack.getItem()).unlockKnowledge(stack, BotaniaAPI.elvenKnowledge);
				ItemLexicon.setForcedPage(stack, LexiconData.elvenMessage.getUnlocalizedName());
				spawnItem(stack);
				stacksIn.remove(i);
				return;
			}
			i++;
		}

		for(RecipeElvenTrade recipe : BotaniaAPI.elvenTradeRecipes) {
			if(recipe.matches(stacksIn, false)) {
				recipe.matches(stacksIn, true);
				spawnItem(recipe.getOutput().copy());
				break;
			}
		}
	}

	void spawnItem(ItemStack stack) {
		EntityItem item = new EntityItem(worldObj, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, stack);
		item.getEntityData().setBoolean(TAG_PORTAL_FLAG, true);
		worldObj.spawnEntityInWorld(item);
		ticksSinceLastItem = 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound cmp) {
		super.writeToNBT(cmp);

		cmp.setInteger(TAG_STACK_COUNT, stacksIn.size());
		int i = 0;
		for(ItemStack stack : stacksIn) {
			NBTTagCompound stackcmp = new NBTTagCompound();
			stack.writeToNBT(stackcmp);
			cmp.setTag(TAG_STACK + i, stackcmp);
			i++;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound cmp) {
		super.readFromNBT(cmp);

		int count = cmp.getInteger(TAG_STACK_COUNT);
		stacksIn.clear();
		for(int i = 0; i < count; i++) {
			NBTTagCompound stackcmp = cmp.getCompoundTag(TAG_STACK + i);
			ItemStack stack = ItemStack.loadItemStackFromNBT(stackcmp);
			stacksIn.add(stack);
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound cmp) {
		cmp.setInteger(TAG_TICKS_OPEN, ticksOpen);
		cmp.setInteger(TAG_TICKS_SINCE_LAST_ITEM, ticksSinceLastItem);
	}

	@Override
	public void readCustomNBT(NBTTagCompound cmp) {
		ticksOpen = cmp.getInteger(TAG_TICKS_OPEN);
		ticksSinceLastItem = cmp.getInteger(TAG_TICKS_SINCE_LAST_ITEM);
	}

	private AlfPortalState getValidState() {
		if(checkConverter(null))
			return AlfPortalState.ON_Z;

		if(checkConverter(CONVERTER_X_Z))
			return AlfPortalState.ON_X;

		return AlfPortalState.OFF;
	}

	private boolean checkConverter(Function<BlockPos, BlockPos> baseConverter) {
		return checkMultipleConverters(baseConverter) || checkMultipleConverters(CONVERTER_Z_SWAP, baseConverter);
	}

	@SafeVarargs
	private final boolean checkMultipleConverters(Function<BlockPos, BlockPos>... converters) {
		if(!check2DArray(AIR_POSITIONS, Blocks.air.getDefaultState(), true, converters))
			return false;
		if(!check2DArray(LIVINGWOOD_POSITIONS, ModBlocks.livingwood.getDefaultState().withProperty(BotaniaStateProps.LIVINGWOOD_VARIANT, LivingWoodVariant.DEFAULT), false, converters))
			return false;
		if(!check2DArray(GLIMMERING_LIVINGWOOD_POSITIONS, ModBlocks.livingwood.getDefaultState().withProperty(BotaniaStateProps.LIVINGWOOD_VARIANT, LivingWoodVariant.GLIMMERING), false, converters))
			return false;
		if(!check2DArray(PYLON_POSITIONS, ModBlocks.pylon.getDefaultState().withProperty(BotaniaStateProps.PYLON_VARIANT, PylonVariant.NATURA), false, converters))
			return false;
		if(!check2DArray(POOL_POSITIONS, ModBlocks.pool.getDefaultState(), true, converters))
			return false;

		lightPylons(converters);
		return true;
	}

	@SafeVarargs
	private final void lightPylons(Function<BlockPos, BlockPos>... converters) {
		if(ticksOpen < 50)
			return;

		int cost = ticksOpen == 50 ? 75000 : 2;

		for(BlockPos pos : PYLON_POSITIONS) {
			for(Function<BlockPos, BlockPos> f : converters)
				if(f != null)
					pos = f.apply(pos);

			TileEntity tile = worldObj.getTileEntity(getPos().add(pos));
			if(tile instanceof TilePylon) {
				TilePylon pylon = (TilePylon) tile;
				pylon.activated = true;
				pylon.centerPos = getPos();
			}

			tile = worldObj.getTileEntity(getPos().add(pos).down());
			if(tile instanceof TilePool) {
				TilePool pool = (TilePool) tile;
				if(pool.getCurrentMana() < cost)
					closeNow = true;
				else if(!worldObj.isRemote)
					pool.recieveMana(-cost);
			}
		}
	}

	private boolean check2DArray(BlockPos[] positions, IBlockState state, boolean onlyCheckBlock, Function<BlockPos, BlockPos>... converters) {
		for(BlockPos pos : positions) {
			for(Function<BlockPos, BlockPos> f : converters)
				if(f != null)
					pos = f.apply(pos);

			if(!checkPosition(pos, state, onlyCheckBlock))
				return false;
		}

		return true;
	}

	private boolean checkPosition(BlockPos pos, IBlockState state, boolean onlyCheckBlock) {
		BlockPos pos_ = getPos().add(pos);
		if(!worldObj.isBlockLoaded(pos_)) {
			hasUnloadedParts = true;
			return true; // Don't fuck everything up if there's a chunk unload
		}

		IBlockState stateat = worldObj.getBlockState(pos_);
		Block blockat = stateat.getBlock();

		if(state.getBlock() == Blocks.air ? blockat.isAir(stateat, worldObj, pos_) : blockat == state.getBlock()) {
			return onlyCheckBlock || stateat == state;
		}

		return false;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}
}
