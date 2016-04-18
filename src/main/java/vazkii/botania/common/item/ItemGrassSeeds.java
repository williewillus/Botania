/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Apr 9, 2014, 5:11:34 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import vazkii.botania.api.item.IFloatingFlower.IslandType;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.AltGrassVariant;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.lib.LibItemNames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ItemGrassSeeds extends ItemMod implements IFloatingFlowerVariant {

	/**
	 * Represents a map of dimension IDs to a set of all block swappers
	 * active in that dimension.
	 */
	private static Map<Integer, Set<BlockSwapper>> blockSwappers = new HashMap<>();

	private static final IslandType[] ISLAND_TYPES = {
		IslandType.GRASS, IslandType.PODZOL, IslandType.MYCEL,
		IslandType.DRY, IslandType.GOLDEN, IslandType.VIVID,
		IslandType.SCORCHED, IslandType.INFUSED, IslandType.MUTATED
	};

	private static final int SUBTYPES = 9;

	public ItemGrassSeeds() {
		super(LibItemNames.GRASS_SEEDS);
		setHasSubtypes(true);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void getSubItems(Item par1, CreativeTabs par2, List<ItemStack> par3) {
		for(int i = 0; i < SUBTYPES; i++)
			par3.add(new ItemStack(par1, 1, i));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + stack.getItemDamage();
	}

	@Override
	public EnumActionResult onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, BlockPos pos, EnumHand hand, EnumFacing side, float par8, float par9, float par10) {
		IBlockState state = par3World.getBlockState(pos);

		if((state.getBlock() == Blocks.dirt && state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT) || (state.getBlock() == Blocks.grass && par1ItemStack.getItemDamage() != 0)) {
			int meta = par1ItemStack.getItemDamage();

			BlockSwapper swapper = addBlockSwapper(par3World, pos, meta);
			par3World.setBlockState(pos, swapper.stateToSet, 1 | 2);
			for(int i = 0; i < 50; i++) {
				double x = (Math.random() - 0.5) * 3;
				double y = Math.random() - 0.5 + 1;
				double z = (Math.random() - 0.5) * 3;

				float r = 0F;
				float g = 0.4F;
				float b = 0F;
				switch(meta) {
				case 1: {
					r = 0.5F;
					g = 0.37F;
					b = 0F;
					break;
				}
				case 2: {
					r = 0.27F;
					g = 0F;
					b = 0.33F;
					break;
				}
				case 3: {
					r = 0.4F;
					g = 0.5F;
					b = 0.05F;
					break;
				}
				case 4: {
					r = 0.75F;
					g = 0.7F;
					b = 0F;
					break;
				}
				case 5: {
					r = 0F;
					g = 0.5F;
					b = 0.1F;
					break;
				}
				case 6: {
					r = 0.75F;
					g = 0F;
					b = 0F;
					break;
				}
				case 7: {
					r = 0F;
					g = 0.55F;
					b = 0.55F;
					break;
				}
				case 8: {
					r = 0.4F;
					g = 0.1F;
					b = 0.4F;
					break;
				}
				}

				float velMul = 0.025F;

				Botania.proxy.wispFX(par3World, pos.getX() + 0.5 + x, pos.getY() + 0.5 + y, pos.getZ() + 0.5 + z, r, g, b, (float) Math.random() * 0.15F + 0.15F, (float) -x * velMul, (float) -y * velMul, (float) -z * velMul);
			}

			par1ItemStack.stackSize--;
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}

	@SubscribeEvent
	public void onTickEnd(TickEvent.WorldTickEvent event) {
		// Block swapper updates should only occur on the server
		if(event.world.isRemote)
			return;

		if(event.phase == Phase.END) {
			int dim = event.world.provider.getDimension();
			if(blockSwappers.containsKey(dim)) {
				Set<BlockSwapper> swappers = blockSwappers.get(dim);

				Iterator<BlockSwapper> iter = swappers.iterator();

				while(iter.hasNext()) {
					BlockSwapper next = iter.next();
					if(next == null || !next.tick())
						iter.remove();
				}
			}
		}
	}

	/**
	 * Adds a grass seed block swapper to the world at the provided positiona
	 * and with the provided meta (which designates the type of the grass
	 * being spread).
	 * 
	 * Block swappers are only actually created on the server, so a client
	 * calling this method will recieve a marker block swapper which contains
	 * the provided information but is not ticked.
	 * @param world The world the swapper will be in.
	 * @param pos The position of the swapper.
	 * @param meta The meta value representing the type of block being swapped.
	 * @return The created block swapper.
	 */
	private static BlockSwapper addBlockSwapper(World world, BlockPos pos, int meta) {
		BlockSwapper swapper = swapperFromMeta(world, pos, meta);

		// Block swappers are only registered on the server
		if(world.isRemote)
			return swapper;

		// If a set for the dimension doesn't exist, create it.
		int dim = world.provider.getDimension();
		if(!blockSwappers.containsKey(dim))
			blockSwappers.put(dim, new HashSet<>());

		// Add the block swapper
		blockSwappers.get(dim).add(swapper);

		return swapper;
	}

	private static BlockSwapper swapperFromMeta(World world, BlockPos pos, int meta) {
		switch(meta) {
		case 1 : return new BlockSwapper(world, pos,  Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL));
		case 2 : return new BlockSwapper(world, pos,  Blocks.mycelium.getDefaultState());
		case 3 : return new BlockSwapper(world, pos,  ModBlocks.altGrass.getDefaultState().withProperty(BotaniaStateProps.ALTGRASS_VARIANT, AltGrassVariant.DRY));
		case 4 : return new BlockSwapper(world, pos,  ModBlocks.altGrass.getDefaultState().withProperty(BotaniaStateProps.ALTGRASS_VARIANT, AltGrassVariant.GOLDEN));
		case 5 : return new BlockSwapper(world, pos,  ModBlocks.altGrass.getDefaultState().withProperty(BotaniaStateProps.ALTGRASS_VARIANT, AltGrassVariant.VIVID));
		case 6 : return new BlockSwapper(world, pos,  ModBlocks.altGrass.getDefaultState().withProperty(BotaniaStateProps.ALTGRASS_VARIANT, AltGrassVariant.SCORCHED));
		case 7 : return new BlockSwapper(world, pos,  ModBlocks.altGrass.getDefaultState().withProperty(BotaniaStateProps.ALTGRASS_VARIANT, AltGrassVariant.INFUSED));
		case 8 : return new BlockSwapper(world, pos,  ModBlocks.altGrass.getDefaultState().withProperty(BotaniaStateProps.ALTGRASS_VARIANT, AltGrassVariant.MUTATED));
		default : return new BlockSwapper(world, pos,  Blocks.grass.getDefaultState());
		}
	}

	/**
	 * A block swapper for the Pasture Seeds, which swaps dirt and grass blocks
	 * centered around a provided point to a provided block/metadata.
	 */
	private static class BlockSwapper {
		
		/**
		 * The range of the block swapper, in blocks.
		 */
		public static final int RANGE = 3;

		/**
		 * The range around which a block can spread in a single tick.
		 */
		public static final int TICK_RANGE = 1;

		private final World world;
		private final Random rand;
		private final IBlockState stateToSet;

		private BlockPos startCoords;
		private int ticksExisted = 0;

		/**
		 * Constructs a new block swapper with the provided world, starting
		 * coordinates, target block, and target metadata.
		 * @param world The world to swap blocks in.
		 * @param coords The central coordinates to swap blocks around.
		 * @param state The target blockstate to swap dirt and grass to.
		 */
		public BlockSwapper(World world, BlockPos coords, IBlockState state) {
			this.world = world;
			this.stateToSet = state;
			this.rand = new Random(coords.hashCode());
			this.startCoords = coords;
		}

		/**
		 * Ticks this block swapper, allowing it to make an action during
		 * this game tick. This method should return "false" when the swapper
		 * has finished operation and should be removed from the world.
		 * @return true if the swapper should continue to exist, false if it
		 * should be removed.
		 */
		public boolean tick() {
			++ticksExisted;
			
			// Go through all blocks in the specified RANGE, and then
			// try and spread around that block if it is our target block already
			for(int i = -RANGE; i <= RANGE; i++) {
				for(int j = -RANGE; j <= RANGE; j++) {
					BlockPos pos = startCoords.add(i, 0, j);
					IBlockState state = world.getBlockState(pos);

					if(state == stateToSet) {
						// Only make changes every 20 ticks
						if(ticksExisted % 20 != 0) continue;

						tickBlock(pos);
					}
				}
			}

			// This swapper should exist for 80 ticks
			return ticksExisted < 80;
		}

		/**
		 * Tick a specific block position, finding the valid blocks
		 * immediately adjacent to it and then replacing one at random.
		 * @param pos The positions to use.
		 */
		public void tickBlock(BlockPos pos) {
			List<BlockPos> validCoords = new ArrayList<>();

			// Go around this block and aggregate valid blocks.
			for(int xOffset = -TICK_RANGE; xOffset <= TICK_RANGE; xOffset++) {
				for(int zOffset = -TICK_RANGE; zOffset <= TICK_RANGE; zOffset++) {
					// Skip the current block
					if(xOffset == 0 && zOffset == 0) continue;

					if(isValidSwapPosition(pos.add(xOffset, 0, zOffset)))
						validCoords.add(pos.add(xOffset, 0, zOffset));
				}
			}

			// If we can make changes, and have at least 1 block to swap,
			// then swap a random block from the valid blocks we could swap.
			if(!validCoords.isEmpty() && !world.isRemote) {
				BlockPos toSwap = validCoords.get(rand.nextInt(validCoords.size()));

				world.setBlockState(toSwap, stateToSet, 1 | 2);
			}
		}

		/**
		 * Determines if a given position is a valid location to spread to, which
		 * means that the block must be either dirt or grass (with meta 0),
		 * and have a block above it which does not block grass growth.
		 * @param pos The position to check.
		 * @return True if the position is valid to swap, false otherwise.
		 */
		public boolean isValidSwapPosition(BlockPos pos) {
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			// Valid blocks to spread to are either dirt or grass, and do not
			// have blocks which block grass growth.

			// See http://minecraft.gamepedia.com/Grass_Block
			// The major rule is that a block which reduces light
			// levels by 2 or more blocks grass growth.

			return (block == Blocks.dirt || block == Blocks.grass)
				&& (block != Blocks.dirt || state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT)
				&& (world.getBlockState(pos.up()).getLightOpacity(world, pos.up()) <= 1);
		}
	}

	public IslandType getIslandType(ItemStack stack) {
		return ISLAND_TYPES[Math.min(stack.getItemDamage(), ISLAND_TYPES.length - 1)];
	}

}
