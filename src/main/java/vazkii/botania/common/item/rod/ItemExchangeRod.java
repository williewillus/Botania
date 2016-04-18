/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Aug 20, 2015, 8:08:34 PM (GMT)]
 */
package vazkii.botania.common.item.rod;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.IBlockProvider;
import vazkii.botania.api.item.IManaProficiencyArmor;
import vazkii.botania.api.item.IWireframeCoordinateListProvider;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.core.handler.ItemsRemainingRenderHandler;
import vazkii.botania.common.block.BlockCamo;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.item.ItemMod;
import vazkii.botania.common.lib.LibItemNames;

import java.util.ArrayList;
import java.util.List;

public class ItemExchangeRod extends ItemMod implements IManaUsingItem, IWireframeCoordinateListProvider {

	private static final int RANGE = 3;
	private static final int COST = 40;

	private static final String TAG_BLOCK_NAME = "blockName";
	private static final String TAG_BLOCK_META = "blockMeta";
	private static final String TAG_TARGET_BLOCK_NAME = "targetBlockName";
	private static final String TAG_TARGET_BLOCK_META = "targetBlockMeta";
	private static final String TAG_SWAPPING = "swapping";
	private static final String TAG_SELECT_X = "selectX";
	private static final String TAG_SELECT_Y = "selectY";
	private static final String TAG_SELECT_Z = "selectZ";
	private static final String TAG_EXTRA_RANGE = "extraRange";

	public ItemExchangeRod() {
		super(LibItemNames.EXCHANGE_ROD);
		setMaxStackSize(1);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, BlockPos pos, EnumHand hand, EnumFacing side, float par8, float par9, float par10) {
		IBlockState wstate = par3World.getBlockState(pos);

		if(par2EntityPlayer.isSneaking()) {
			TileEntity tile = par3World.getTileEntity(pos);
			if(tile == null) {
				if(BlockCamo.isValidBlock(wstate)) {
					Item item = Item.getItemFromBlock(wstate.getBlock());

					boolean set = setBlock(par1ItemStack, wstate.getBlock(), !item.getHasSubtypes() ? 0 : wstate.getBlock().getMetaFromState(wstate));
					par2EntityPlayer.setItemStackToSlot(hand == EnumHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, par1ItemStack);

					displayRemainderCounter(par2EntityPlayer, par1ItemStack);
					return EnumActionResult.SUCCESS;
				}
			}
		} else if(canExchange(par1ItemStack) && !ItemNBTHelper.getBoolean(par1ItemStack, TAG_SWAPPING, false)) {
			Block block = getBlock(par1ItemStack);
			int meta = getBlockMeta(par1ItemStack);
			List<BlockPos> swap = getBlocksToSwap(par3World, par1ItemStack, block.getStateFromMeta(meta), pos, null);
			if(swap.size() > 0) {
				ItemNBTHelper.setBoolean(par1ItemStack, TAG_SWAPPING, true);
				ItemNBTHelper.setInt(par1ItemStack, TAG_SELECT_X, pos.getX());
				ItemNBTHelper.setInt(par1ItemStack, TAG_SELECT_Y, pos.getY());
				ItemNBTHelper.setInt(par1ItemStack, TAG_SELECT_Z, pos.getZ());
				setTargetBlock(par1ItemStack, wstate.getBlock(), wstate.getBlock().getMetaFromState(wstate));
				if(par3World.isRemote)
					par2EntityPlayer.swingArm(hand);
			}
		}

		return EnumActionResult.SUCCESS;
	}

	@SubscribeEvent
	public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
		ItemStack stack = event.getItemStack();
		if(stack != null && stack.getItem() == this && canExchange(stack) && ManaItemHandler.requestManaExactForTool(stack, event.getEntityPlayer(), COST, false)) {
			if(exchange(event.getWorld(), event.getEntityPlayer(), event.getPos(), stack, getBlock(stack).getStateFromMeta(getBlockMeta(stack))))
				ManaItemHandler.requestManaExactForTool(stack, event.getEntityPlayer(), COST, true);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int something, boolean somethingelse) {
		if(!canExchange(stack) || !(entity instanceof EntityPlayer))
			return;

		EntityPlayer player = (EntityPlayer) entity;

		int extraRange = ItemNBTHelper.getInt(stack, TAG_EXTRA_RANGE, 1);
		int extraRangeNew = IManaProficiencyArmor.Helper.hasProficiency(player) ? 3 : 1;
		if(extraRange != extraRangeNew)
			ItemNBTHelper.setInt(stack, TAG_EXTRA_RANGE, extraRangeNew);

		Block block = getBlock(stack);
		int meta = getBlockMeta(stack);
		if(ItemNBTHelper.getBoolean(stack, TAG_SWAPPING, false)) {
			if(!ManaItemHandler.requestManaExactForTool(stack, player, COST, false)) {
				ItemNBTHelper.setBoolean(stack, TAG_SWAPPING, false);
				return;
			}

			int x = ItemNBTHelper.getInt(stack, TAG_SELECT_X, 0);
			int y = ItemNBTHelper.getInt(stack, TAG_SELECT_Y, 0);
			int z = ItemNBTHelper.getInt(stack, TAG_SELECT_Z, 0);
			Block targetBlock = getTargetBlock(stack);
			int targetMeta = getTargetBlockMeta(stack);
			List<BlockPos> swap = getBlocksToSwap(world, stack, block.getStateFromMeta(meta), new BlockPos(x, y, z), targetBlock.getStateFromMeta(targetMeta));
			if(swap.size() == 0) {
				ItemNBTHelper.setBoolean(stack, TAG_SWAPPING, false);
				return;
			}

			BlockPos coords = swap.get(world.rand.nextInt(swap.size()));
			boolean exchange = exchange(world, player, coords, stack, block.getStateFromMeta(meta));
			if(exchange)
				ManaItemHandler.requestManaExactForTool(stack, player, COST, true);
			else ItemNBTHelper.setBoolean(stack, TAG_SWAPPING, false);
		}
	}

	public List<BlockPos> getBlocksToSwap(World world, ItemStack stack, IBlockState swapState, BlockPos pos, IBlockState targetState) {
		// If we have no target block passed in, infer it to be
		// the block which the swapping is centered on (presumably the block
		// which the player is looking at)
		if(targetState == null) {
			targetState = world.getBlockState(pos);
		}

		// Our result list
		List<BlockPos> coordsList = new ArrayList<>();

		// We subtract 1 from the effective range as the center tile is included
		// So, with a range of 3, we are visiting tiles at -2, -1, 0, 1, 2
		int effRange = RANGE + ItemNBTHelper.getInt(stack, TAG_EXTRA_RANGE, 1) - 1;
		
		// Iterate in all 3 dimensions through our possible positions.
		for(int offsetX = -effRange; offsetX <= effRange; offsetX++)
			for(int offsetY = -effRange; offsetY <= effRange; offsetY++)
				for(int offsetZ = -effRange; offsetZ <= effRange; offsetZ++) {
					BlockPos pos_ = pos.add(offsetX, offsetY, offsetZ);

					IBlockState currentState = world.getBlockState(pos_);
					
					// If this block is not our target, ignore it, as we don't need
					// to consider replacing it
					if(currentState != targetState)
						continue;
					
					// If this block is already the block we're swapping to,
					// we don't need to swap again
					if(currentState == swapState)
						continue;
					
					// Check to see if the block is visible on any side:
					for(EnumFacing dir : EnumFacing.VALUES) {
						BlockPos adjPos = pos_.offset(dir);
						IBlockState adjState = world.getBlockState(adjPos);

						// If the side of the adjacent block facing this block is
						// _not_ solid, then this block is considered "visible"
						// and should be replaced.
						
						// If there is a rendering-specific way to check for this,
						// that should be placed in preference to this.
						if(!adjState.isSideSolid(world, adjPos, dir.getOpposite())) {
							coordsList.add(pos_);
							break;
						}
					}
				}

		return coordsList;
	}

	public boolean exchange(World world, EntityPlayer player, BlockPos pos, ItemStack stack, IBlockState state) {
		TileEntity tile = world.getTileEntity(pos);
		if(tile != null)
			return false;

		ItemStack placeStack = removeFromInventory(player, stack, state.getBlock(), state.getBlock().getMetaFromState(state), false);
		if(placeStack != null) {
			IBlockState stateAt = world.getBlockState(pos);
			Block blockAt = stateAt.getBlock();
			if(!blockAt.isAir(world.getBlockState(pos), world, pos) && stateAt.getPlayerRelativeBlockHardness(player, world, pos) > 0 && stateAt != state) {
				if(!world.isRemote) {
					if(!player.capabilities.isCreativeMode) {
						List<ItemStack> drops = blockAt.getDrops(world, pos, stateAt, 0);
						for(ItemStack drop : drops)
							world.spawnEntityInWorld(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
						removeFromInventory(player, stack, state.getBlock(), state.getBlock().getMetaFromState(state), true);
					}
					world.playAuxSFX(2001, pos, Block.getStateId(state));
					world.setBlockState(pos, state, 1 | 2);
					state.getBlock().onBlockPlacedBy(world, pos, state, player, placeStack);
				}
				displayRemainderCounter(player, stack);
				return true;
			}
		}

		return false;
	}

	public boolean canExchange(ItemStack stack) {
		Block block = getBlock(stack);
		return block != null && block != Blocks.air;
	}

	public static ItemStack removeFromInventory(EntityPlayer player, IInventory inv, ItemStack stack, Block block, int meta, boolean doit) {
		List<ItemStack> providers = new ArrayList<>();
		for(int i = inv.getSizeInventory() - 1; i >= 0; i--) {
			ItemStack invStack = inv.getStackInSlot(i);
			if(invStack == null)
				continue;

			Item item = invStack.getItem();
			if(item == Item.getItemFromBlock(block) && invStack.getItemDamage() == meta) {
				ItemStack retStack = invStack.copy();
				if(doit) {
					invStack.stackSize--;
					if(invStack.stackSize == 0)
						inv.setInventorySlotContents(i, null);
				}
				return retStack;
			}

			if(item instanceof IBlockProvider)
				providers.add(invStack);
		}

		for(ItemStack provStack : providers) {
			IBlockProvider prov = (IBlockProvider) provStack.getItem();
			if(prov.provideBlock(player, stack, provStack, block, meta, doit))
				return new ItemStack(block, 1, meta);
		}

		return null;
	}

	public static ItemStack removeFromInventory(EntityPlayer player, ItemStack stack, Block block, int meta, boolean doit) {
		if(player.capabilities.isCreativeMode)
			return new ItemStack(block, 1, meta);

		ItemStack outStack = removeFromInventory(player, BotaniaAPI.internalHandler.getBaublesInventory(player), stack, block, meta, doit);
		if (outStack == null)
			outStack = removeFromInventory(player, player.inventory, stack, block, meta, doit);
		return outStack;
	}

	public static int getInventoryItemCount(EntityPlayer player, ItemStack stack, Block block, int meta) {
		if(player.capabilities.isCreativeMode)
			return -1;

		int baubleCount = getInventoryItemCount(player, BotaniaAPI.internalHandler.getBaublesInventory(player), stack, block, meta);
		if (baubleCount == -1) return -1;

		int count = getInventoryItemCount(player, player.inventory, stack, block, meta);
		if (count == -1) return -1;
		
		return count+baubleCount;
	}

	public static int getInventoryItemCount(EntityPlayer player, IInventory inv, ItemStack stack, Block block, int meta) {
		if(player.capabilities.isCreativeMode)
			return -1;

		int count = 0;
		for(int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack invStack = inv.getStackInSlot(i);
			if(invStack == null)
				continue;

			Item item = invStack.getItem();
			if(item == Item.getItemFromBlock(block) && invStack.getItemDamage() == meta)
				count += invStack.stackSize;

			if(item instanceof IBlockProvider) {
				IBlockProvider prov = (IBlockProvider) item;
				int provCount = prov.getBlockCount(player, stack, invStack, block, meta);
				if(provCount == -1)
					return -1;
				count += provCount;
			}
		}

		return count;
	}

	public void displayRemainderCounter(EntityPlayer player, ItemStack stack) {
		Block block = getBlock(stack);
		int meta = getBlockMeta(stack);
		int count = getInventoryItemCount(player, stack, block, meta);
		if(!player.worldObj.isRemote)
			ItemsRemainingRenderHandler.set(new ItemStack(block, 1, meta), count);
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

	private boolean setBlock(ItemStack stack, Block block, int meta) {
		ItemNBTHelper.setString(stack, TAG_BLOCK_NAME, Block.blockRegistry.getNameForObject(block).toString());
		ItemNBTHelper.setInt(stack, TAG_BLOCK_META, meta);
		return true;
	}

	@Override
	public String getItemStackDisplayName(ItemStack par1ItemStack) {
		Block block = getBlock(par1ItemStack);
		int meta = getBlockMeta(par1ItemStack);
		return super.getItemStackDisplayName(par1ItemStack) + (block == null ? "" : " (" + TextFormatting.GREEN + new ItemStack(block, 1, meta).getDisplayName() + TextFormatting.RESET + ")");
	}

	public static String getBlockName(ItemStack stack) {
		return ItemNBTHelper.getString(stack, TAG_BLOCK_NAME, "");
	}

	public static Block getBlock(ItemStack stack) {
		Block block = Block.getBlockFromName(getBlockName(stack));
		return block;
	}

	public static int getBlockMeta(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_BLOCK_META, 0);
	}

	private boolean setTargetBlock(ItemStack stack, Block block, int meta) {
		ItemNBTHelper.setString(stack, TAG_TARGET_BLOCK_NAME, Block.blockRegistry.getNameForObject(block).toString());
		ItemNBTHelper.setInt(stack, TAG_TARGET_BLOCK_META, meta);
		return true;
	}

	public static String getTargetBlockName(ItemStack stack) {
		return ItemNBTHelper.getString(stack, TAG_TARGET_BLOCK_NAME, "");
	}

	public static Block getTargetBlock(ItemStack stack) {
		Block block = Block.getBlockFromName(getTargetBlockName(stack));
		return block;
	}

	public static int getTargetBlockMeta(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_TARGET_BLOCK_META, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BlockPos> getWireframesToDraw(EntityPlayer player, ItemStack stack) {
		ItemStack holding = player.getHeldItemMainhand();
		if(holding != stack || !canExchange(stack))
			return ImmutableList.of();

		Block block = getBlock(stack);
		int meta = getBlockMeta(stack);

		RayTraceResult pos = Minecraft.getMinecraft().objectMouseOver;
		if(pos != null && pos.getBlockPos() != null) {
			BlockPos bPos = pos.getBlockPos();
			Block targetBlock = null;
			int targetMeta = 0;
			if(ItemNBTHelper.getBoolean(stack, TAG_SWAPPING, false)) {
				bPos = new BlockPos(
					ItemNBTHelper.getInt(stack, TAG_SELECT_X, 0),
					ItemNBTHelper.getInt(stack, TAG_SELECT_Y, 0),
					ItemNBTHelper.getInt(stack, TAG_SELECT_Z, 0)
				);
				targetBlock = getTargetBlock(stack);
				targetMeta = getTargetBlockMeta(stack);
			}

			if(!player.worldObj.isAirBlock(bPos)) {
				List<BlockPos> coordsList = getBlocksToSwap(player.worldObj, stack, block.getStateFromMeta(meta), bPos, targetBlock == null ? null : targetBlock.getStateFromMeta(targetMeta));
				for(BlockPos coords : coordsList)
					if(coords.equals(bPos)) {
						coordsList.remove(coords);
						break;
					}
				return coordsList;
			}

		}
		return ImmutableList.of();
	}

}
