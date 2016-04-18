/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Feb 16, 2015, 2:18:30 PM (GMT)]
 */
package vazkii.botania.common.block.tile.corporea;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.corporea.ICorporeaRequestor;
import vazkii.botania.api.corporea.ICorporeaSpark;
import vazkii.botania.common.core.helper.InventoryHelper;

import java.util.ArrayList;
import java.util.List;

public class TileCorporeaFunnel extends TileCorporeaBase implements ICorporeaRequestor {

	public void doRequest() {
		ICorporeaSpark spark = getSpark();
		if(spark != null && spark.getMaster() != null) {
			List<ItemStack> filter = getFilter();
			if(!filter.isEmpty()) {
				ItemStack stack = filter.get(worldObj.rand.nextInt(filter.size()));

				if(stack != null)
					doCorporeaRequest(stack, stack.stackSize, spark);
			}
		}
	}

	public List<ItemStack> getFilter() {
		List<ItemStack> filter = new ArrayList<>();

		final int[] rotationToStackSize = new int[] {
				1, 2, 4, 8, 16, 32, 48, 64
		};

		for(EnumFacing dir : EnumFacing.HORIZONTALS) {
			List<EntityItemFrame> frames = worldObj.getEntitiesWithinAABB(EntityItemFrame.class, new AxisAlignedBB(pos.offset(dir), pos.offset(dir).add(1, 1, 1)));
			for(EntityItemFrame frame : frames) {
				EnumFacing orientation = frame.facingDirection;
				if(orientation == dir) {
					ItemStack stack = frame.getDisplayedItem();
					if(stack != null) {
						ItemStack copy = stack.copy();
						copy.stackSize = rotationToStackSize[frame.getRotation()];
						filter.add(copy);
					}
				}
			}
		}

		return filter;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	protected SimpleItemStackHandler createItemHandler() {
		return new SimpleItemStackHandler(this, false);
	}

	@Override
	public void doCorporeaRequest(Object request, int count, ICorporeaSpark spark) {
		if(!(request instanceof ItemStack))
			return;

		IItemHandler inv = getInv();

		List<ItemStack> stacks = CorporeaHelper.requestItem(request, count, spark, true, true);
		spark.onItemsRequested(stacks);
		for(ItemStack reqStack : stacks) {
			if(inv != null && ItemHandlerHelper.insertItemStacked(inv, reqStack, true) == null)
				ItemHandlerHelper.insertItemStacked(inv, reqStack, false);
			else {
				EntityItem item = new EntityItem(worldObj, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, reqStack);
				worldObj.spawnEntityInWorld(item);
			}
		}
	}

	private IItemHandler getInv() {
		TileEntity te = worldObj.getTileEntity(pos.down());
		IItemHandler ret = InventoryHelper.getInventory(worldObj, pos.down(), EnumFacing.UP);
		if(ret == null)
			ret = InventoryHelper.getInventory(worldObj, pos.down(), null);
		if(ret != null && !(te instanceof TileCorporeaFunnel))
			return ret;

		te = worldObj.getTileEntity(pos.down(2));
		ret = InventoryHelper.getInventory(worldObj, pos.down(2), EnumFacing.UP);
		if(ret == null)
			ret = InventoryHelper.getInventory(worldObj, pos.down(2), null);
		if(ret != null && !(te instanceof TileCorporeaFunnel))
			return ret;

		return null;
	}

}
