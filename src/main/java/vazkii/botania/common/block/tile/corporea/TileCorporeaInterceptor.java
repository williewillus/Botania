/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Apr 19, 2015, 6:21:08 PM (GMT)]
 */
package vazkii.botania.common.block.tile.corporea;

import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.corporea.ICorporeaInterceptor;
import vazkii.botania.api.corporea.ICorporeaSpark;
import vazkii.botania.api.corporea.InvWithLocation;
import vazkii.botania.api.state.BotaniaStateProps;

import java.util.ArrayList;
import java.util.List;

public class TileCorporeaInterceptor extends TileCorporeaBase implements ICorporeaInterceptor {

	@Override
	protected SimpleItemStackHandler createItemHandler() {
		return new SimpleItemStackHandler(this, false);
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public void interceptRequest(Object request, int count, ICorporeaSpark spark, ICorporeaSpark source, List<ItemStack> stacks, List<InvWithLocation> inventories, boolean doit) {
		// NO-OP
	}

	@Override
	public void interceptRequestLast(Object request, int count, ICorporeaSpark spark, ICorporeaSpark source, List<ItemStack> stacks, List<InvWithLocation> inventories, boolean doit) {
		List<ItemStack> filter = getFilter();
		for(ItemStack stack : filter)
			if(requestMatches(request, stack)) {
				int missing = count;
				for(ItemStack stack_ : stacks)
					missing -= stack_.stackSize;

				if(missing > 0 && !worldObj.getBlockState(getPos()).getValue(BotaniaStateProps.POWERED)) {
					worldObj.setBlockState(getPos(), worldObj.getBlockState(getPos()).withProperty(BotaniaStateProps.POWERED, true), 1 | 2);
					worldObj.scheduleUpdate(getPos(), getBlockType(), 2);

					TileEntity requestor = source.getSparkInventory().world.getTileEntity(source.getSparkInventory().pos);
					for(EnumFacing dir : EnumFacing.HORIZONTALS) {
						TileEntity tile = worldObj.getTileEntity(pos.offset(dir));
						if(tile != null && tile instanceof TileCorporeaRetainer)
							((TileCorporeaRetainer) tile).setPendingRequest(requestor.getPos(), request, count);
					}

					return;
				}
			}

	}

	public boolean requestMatches(Object request, ItemStack filter) {
		if(filter == null)
			return false;

		if(request instanceof ItemStack) {
			ItemStack stack = (ItemStack) request;
			return stack != null && stack.isItemEqual(filter) && ItemStack.areItemStackTagsEqual(filter, stack);
		}

		String name = (String) request;
		return CorporeaHelper.stacksMatch(filter, name);
	}

	public List<ItemStack> getFilter() {
		List<ItemStack> filter = new ArrayList<>();

		for(EnumFacing dir : EnumFacing.HORIZONTALS) {
			List<EntityItemFrame> frames = worldObj.getEntitiesWithinAABB(EntityItemFrame.class, new AxisAlignedBB(pos.offset(dir), pos.offset(dir).add(1, 1, 1)));
			for(EntityItemFrame frame : frames) {
				EnumFacing orientation = frame.facingDirection;
				if(orientation == dir)
					filter.add(frame.getDisplayedItem());
			}
		}

		return filter;
	}


}
