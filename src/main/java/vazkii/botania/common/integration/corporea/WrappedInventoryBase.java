/**
 * This class was created by <Vindex>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 */
package vazkii.botania.common.integration.corporea;

import net.minecraft.item.ItemStack;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.corporea.ICorporeaSpark;
import vazkii.botania.api.corporea.IWrappedInventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class WrappedInventoryBase implements IWrappedInventory {

	protected ICorporeaSpark spark;

	@Override
	public ICorporeaSpark getSpark() {
		return spark;
	}

	protected boolean isMatchingItemStack(Object matcher, boolean checkNBT, ItemStack stackAt) {
		return matcher instanceof ItemStack ? CorporeaHelper.stacksMatch((ItemStack) matcher, stackAt, checkNBT)
				: matcher instanceof String ? CorporeaHelper.stacksMatch(stackAt, (String) matcher) : false;
	}

	protected Collection<? extends ItemStack> breakDownBigStack(ItemStack stack) {
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		int additionalStacks = stack.stackSize / stack.getMaxStackSize();
		int lastStackSize = stack.stackSize % stack.getMaxStackSize();
		if(additionalStacks > 0) {
			ItemStack fullStack = stack.copy();
			fullStack.stackSize = stack.getMaxStackSize();
			for (int i = 0; i < additionalStacks; i++) {
				stacks.add(fullStack.copy());
			}
		}
		ItemStack lastStack = stack.copy();
		lastStack.stackSize = lastStackSize;
		stacks.add(lastStack);

		return stacks;
	}
}
