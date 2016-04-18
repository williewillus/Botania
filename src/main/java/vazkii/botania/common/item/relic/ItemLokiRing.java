/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 29, 2015, 10:13:32 PM (GMT)]
 */
package vazkii.botania.common.item.relic;

import baubles.api.BaubleType;
import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketSyncBauble;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.item.IExtendedWireframeCoordinateListProvider;
import vazkii.botania.api.item.ISequentialBreaker;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.common.lib.LibItemNames;

import java.util.ArrayList;
import java.util.List;

public class ItemLokiRing extends ItemRelicBauble implements IExtendedWireframeCoordinateListProvider, IManaUsingItem {

	private static final String TAG_CURSOR_LIST = "cursorList";
	private static final String TAG_CURSOR_PREFIX = "cursor";
	private static final String TAG_CURSOR_COUNT = "cursorCount";
	private static final String TAG_X_OFFSET = "xOffset";
	private static final String TAG_Y_OFFSET = "yOffset";
	private static final String TAG_Z_OFFSET = "zOffset";
	private static final String TAG_X_ORIGIN = "xOrigin";
	private static final String TAG_Y_ORIGIN = "yOrigin";
	private static final String TAG_Z_ORIGIN = "zOrigin";

	public ItemLokiRing() {
		super(LibItemNames.LOKI_RING);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
		EntityPlayer player = event.getEntityPlayer();
		ItemStack lokiRing = getLokiRing(player);
		if(lokiRing == null || player.worldObj.isRemote)
			return;

		int slot = -1;
		InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);
		for(int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if(stack == lokiRing) {
				slot = i;
				break;
			}
		}

		ItemStack heldItemStack = event.getItemStack();
		BlockPos originCoords = getOriginPos(lokiRing);
		RayTraceResult lookPos = ToolCommons.raytraceFromEntity(player.worldObj, player, true, 10F);
		List<BlockPos> cursors = getCursorList(lokiRing);
		int cursorCount = cursors.size();

		int cost = Math.min(cursorCount, (int) Math.pow(Math.E, cursorCount * 0.25));

		if(heldItemStack == null && player.isSneaking()) {
			if(originCoords.getY() == -1 && lookPos != null && lookPos.getBlockPos() != null) {
				setOriginPos(lokiRing, lookPos.getBlockPos());
				setCursorList(lokiRing, null);
				if(player instanceof EntityPlayerMP)
					PacketHandler.INSTANCE.sendTo(new PacketSyncBauble(player, slot), (EntityPlayerMP) player);
			} else if(lookPos != null && lookPos.getBlockPos() != null) {
				if(originCoords.equals(lookPos.getBlockPos())) {
					setOriginPos(lokiRing, new BlockPos(0, -1, 0));
					if(player instanceof EntityPlayerMP)
						PacketHandler.INSTANCE.sendTo(new PacketSyncBauble(player, slot), (EntityPlayerMP) player);
				} else {
					addCursor : {
					BlockPos relPos = lookPos.getBlockPos().add(new BlockPos(-originCoords.getX(), -originCoords.getY(), -originCoords.getZ()));

					for(BlockPos cursor : cursors)
						if(cursor.equals(relPos)) {
							cursors.remove(cursor);
							setCursorList(lokiRing, cursors);
							if(player instanceof EntityPlayerMP)
								PacketHandler.INSTANCE.sendTo(new PacketSyncBauble(player, slot), (EntityPlayerMP) player);
							break addCursor;
						}

					addCursor(lokiRing, relPos);
					if(player instanceof EntityPlayerMP)
						PacketHandler.INSTANCE.sendTo(new PacketSyncBauble(player, slot), (EntityPlayerMP) player);
				}
				}
			}
		} else if(heldItemStack != null && lookPos != null && lookPos.getBlockPos() != null && player.isSneaking()) {
			for(BlockPos cursor : cursors) {
				BlockPos pos = lookPos.getBlockPos().add(cursor);
				Item item = heldItemStack.getItem();
				if(!player.worldObj.isAirBlock(pos) && ManaItemHandler.requestManaExact(lokiRing, player, cost, true)) {
					item.onItemUse(player.capabilities.isCreativeMode ? heldItemStack.copy() : heldItemStack, player, player.worldObj, pos, event.getHand(), lookPos.sideHit, (float) lookPos.hitVec.xCoord - pos.getX(), (float) lookPos.hitVec.yCoord - pos.getY(), (float) lookPos.hitVec.zCoord - pos.getZ());
					if(heldItemStack.stackSize == 0) {
						event.setCanceled(true);
						return;
					}
				}
			}
		}
	}

	public static void breakOnAllCursors(EntityPlayer player, Item item, ItemStack stack, BlockPos pos, EnumFacing side) {
		ItemStack lokiRing = getLokiRing(player);
		if(lokiRing == null || player.worldObj.isRemote || !(item instanceof ISequentialBreaker))
			return;

		List<BlockPos> cursors = getCursorList(lokiRing);
		ISequentialBreaker breaker = (ISequentialBreaker) item;
		World world = player.worldObj;
		boolean silk = EnchantmentHelper.getEnchantmentLevel(Enchantments.silkTouch, stack) > 0;
		int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.fortune, stack);
		boolean dispose = breaker.disposeOfTrashBlocks(stack);

		for(int i = 0; i < cursors.size(); i++) {
			BlockPos coords = cursors.get(i);
			IBlockState state = world.getBlockState(coords);
			breaker.breakOtherBlock(player, stack, coords, coords, side);
			ToolCommons.removeBlockWithDrops(player, stack, player.worldObj, coords, pos, state.getBlock(), new Material[] { state.getMaterial() }, silk, fortune, state.getBlockHardness(world, coords), dispose);
		}
	}

	@Override
	public BaubleType getBaubleType(ItemStack arg0) {
		return BaubleType.RING;
	}

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase player) {
		setCursorList(stack, null);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BlockPos> getWireframesToDraw(EntityPlayer player, ItemStack stack) {
		if(getLokiRing(player) != stack)
			return ImmutableList.of();

		RayTraceResult lookPos = Minecraft.getMinecraft().objectMouseOver;

		if(lookPos != null && lookPos.getBlockPos() != null && !player.worldObj.isAirBlock(lookPos.getBlockPos()) && lookPos.entityHit == null) {
			List<BlockPos> list = getCursorList(stack);
			BlockPos origin = getOriginPos(stack);

			for (int i = 0; i < list.size(); i++) {
				if (origin.getY() != -1) {
					list.set(i, list.get(i).add(origin));
				} else {
					list.set(i, list.get(i).add(lookPos.getBlockPos()));
				}
			}

			return list;
		}

		return ImmutableList.of();
	}

	@Override
	public BlockPos getSourceWireframe(EntityPlayer player, ItemStack stack) {
		return getLokiRing(player) == stack ? getOriginPos(stack) : null;
	}

	private static ItemStack getLokiRing(EntityPlayer player) {
		InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(player);
		ItemStack stack1 = baubles.getStackInSlot(1);
		ItemStack stack2 = baubles.getStackInSlot(2);
		return isLokiRing(stack1) ? stack1 : isLokiRing(stack2) ? stack2 : null;
	}

	private static boolean isLokiRing(ItemStack stack) {
		return stack != null && (stack.getItem() == ModItems.lokiRing || stack.getItem() == ModItems.aesirRing);
	}

	private static BlockPos getOriginPos(ItemStack stack) {
		int x = ItemNBTHelper.getInt(stack, TAG_X_ORIGIN, 0);
		int y = ItemNBTHelper.getInt(stack, TAG_Y_ORIGIN, -1);
		int z = ItemNBTHelper.getInt(stack, TAG_Z_ORIGIN, 0);
		return new BlockPos(x, y, z);
	}

	private static void setOriginPos(ItemStack stack, BlockPos pos) {
		ItemNBTHelper.setInt(stack, TAG_X_ORIGIN, pos.getX());
		ItemNBTHelper.setInt(stack, TAG_Y_ORIGIN, pos.getY());
		ItemNBTHelper.setInt(stack, TAG_Z_ORIGIN, pos.getZ());
	}

	private static List<BlockPos> getCursorList(ItemStack stack) {
		NBTTagCompound cmp = ItemNBTHelper.getCompound(stack, TAG_CURSOR_LIST, false);
		List<BlockPos> cursors = new ArrayList<>();

		int count = cmp.getInteger(TAG_CURSOR_COUNT);
		for(int i = 0; i < count; i++) {
			NBTTagCompound cursorCmp = cmp.getCompoundTag(TAG_CURSOR_PREFIX + i);
			int x = cursorCmp.getInteger(TAG_X_OFFSET);
			int y = cursorCmp.getInteger(TAG_Y_OFFSET);
			int z = cursorCmp.getInteger(TAG_Z_OFFSET);
			cursors.add(new BlockPos(x, y, z));
		}

		return cursors;
	}

	private static void setCursorList(ItemStack stack, List<BlockPos> cursors) {
		NBTTagCompound cmp = new NBTTagCompound();
		if(cursors != null) {
			int i = 0;
			for(BlockPos cursor : cursors) {
				NBTTagCompound cursorCmp = cursorToCmp(cursor);
				cmp.setTag(TAG_CURSOR_PREFIX + i, cursorCmp);
				i++;
			}
			cmp.setInteger(TAG_CURSOR_COUNT, i);
		}

		ItemNBTHelper.setCompound(stack, TAG_CURSOR_LIST, cmp);
	}

	private static NBTTagCompound cursorToCmp(BlockPos pos) {
		NBTTagCompound cmp = new NBTTagCompound();
		cmp.setInteger(TAG_X_OFFSET, pos.getX());
		cmp.setInteger(TAG_Y_OFFSET, pos.getY());
		cmp.setInteger(TAG_Z_OFFSET, pos.getZ());
		return cmp;
	}

	private static void addCursor(ItemStack stack, BlockPos pos) {
		NBTTagCompound cmp = ItemNBTHelper.getCompound(stack, TAG_CURSOR_LIST, false);
		int count = cmp.getInteger(TAG_CURSOR_COUNT);
		cmp.setTag(TAG_CURSOR_PREFIX + count, cursorToCmp(pos));
		cmp.setInteger(TAG_CURSOR_COUNT, count + 1);
		ItemNBTHelper.setCompound(stack, TAG_CURSOR_LIST, cmp);
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

}

