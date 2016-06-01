/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 16, 2015, 6:43:33 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.lib.LibGuiIDs;
import vazkii.botania.common.lib.LibItemNames;

import javax.annotation.Nonnull;

public class ItemFlowerBag extends ItemMod {

	private static final String TAG_ITEMS = "InvItems";
	private static final String TAG_SLOT = "Slot";

	public ItemFlowerBag() {
		super(LibItemNames.FLOWER_BAG);
		setMaxStackSize(1);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onPickupItem(EntityItemPickupEvent event) {
		ItemStack stack = event.getItem().getEntityItem();
		if(stack.getItem() == Item.getItemFromBlock(ModBlocks.flower) && stack.stackSize > 0) {
			int color = stack.getItemDamage();
			if(color > 15)
				return;

			for(int i = 0; i < event.getEntityPlayer().inventory.getSizeInventory(); i++) {
				if(i == event.getEntityPlayer().inventory.currentItem)
					continue; // prevent item deletion

				ItemStack invStack = event.getEntityPlayer().inventory.getStackInSlot(i);
				if(invStack != null && invStack.getItem() == this) {
					ItemStack[] bagInv = loadStacks(invStack);
					ItemStack stackAt = bagInv[color];
					boolean didChange = false;
					if(stackAt == null) {
						bagInv[color] = stack.copy();
						stack.stackSize = 0;
						didChange = true;
					} else {
						int stackAtSize = stackAt.stackSize;
						int stackSize = stack.stackSize;
						int spare = 64 - stackAtSize;
						int pass = Math.min(spare, stackSize);
						if(pass > 0) {
							stackAt.stackSize += pass;
							stack.stackSize -= pass;
							didChange = true;
						}
					}

					if(didChange) {
						setStacks(invStack, bagInv);
						if (!event.getItem().isSilent()) {
							event.getItem().worldObj.playSound(null, event.getEntityPlayer().posX, event.getEntityPlayer().posY, event.getEntityPlayer().posZ,
									SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
									((event.getItem().worldObj.rand.nextFloat() - event.getItem().worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
						}
						((EntityPlayerMP) event.getEntityPlayer()).connection.sendPacket(new SPacketCollectItem(event.getItem().getEntityId(), event.getEntityPlayer().getEntityId()));
					}
				}

				if(stack.stackSize == 0)
					return;
			}
		}
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		if(hand == EnumHand.OFF_HAND) {
			return ActionResult.newResult(EnumActionResult.PASS, stack);
		}
		player.openGui(Botania.instance, LibGuiIDs.FLOWER_BAG, world, 0, 0, 0);
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xs, float ys, float zs) {
		TileEntity tile = world.getTileEntity(pos);
		if(tile != null) {
			if(!world.isRemote) {
				IItemHandler inv = null;
				if(tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
					inv = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				else if(tile instanceof IInventory)
					inv = new InvWrapper(((IInventory) tile));

				if(inv == null)
					return EnumActionResult.SUCCESS;

				ItemStack[] stacks = loadStacks(stack);
				ItemStack[] newStacks = new ItemStack[stacks.length];
				boolean putAny = false;

				int i = 0;
				for(ItemStack petal : stacks) {
					if(petal != null) {
						newStacks[i] = ItemHandlerHelper.insertItemStacked(inv, petal, false);
						int count = petal.stackSize;
						if(newStacks[i] != null)
							count = petal.stackSize - newStacks[i].stackSize;
						putAny |= count > 0;
					}

					i++;
				}

				setStacks(stack, newStacks);
				if(putAny && tile instanceof TileEntityChest) {
					player.displayGUIChest(((TileEntityChest) tile));
				}
			}

			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	public static ItemStack[] loadStacks(ItemStack stack) {
		NBTTagList var2 = ItemNBTHelper.getList(stack, TAG_ITEMS, 10, false);
		ItemStack[] inventorySlots = new ItemStack[16];
		for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
			NBTTagCompound var4 = var2.getCompoundTagAt(var3);
			byte var5 = var4.getByte(TAG_SLOT);
			if(var5 >= 0 && var5 < inventorySlots.length)
				inventorySlots[var5] = ItemStack.loadItemStackFromNBT(var4);
		}

		return inventorySlots;
	}

	public static void setStacks(ItemStack stack, ItemStack[] inventorySlots) {
		NBTTagList var2 = new NBTTagList();
		for(int var3 = 0; var3 < inventorySlots.length; ++var3)
			if(inventorySlots[var3] != null) {
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte(TAG_SLOT, (byte)var3);
				inventorySlots[var3].writeToNBT(var4);
				var2.appendTag(var4);
			}

		ItemNBTHelper.setList(stack, TAG_ITEMS, var2);
	}

}
