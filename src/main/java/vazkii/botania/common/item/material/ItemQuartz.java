/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jun 11, 2014, 2:16:47 AM (GMT)]
 */
package vazkii.botania.common.item.material;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import vazkii.botania.api.recipe.IElvenItem;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.item.ItemMod;
import vazkii.botania.common.lib.LibItemNames;

import java.util.List;

public class ItemQuartz extends ItemMod implements IElvenItem {

	private static final int SUBTYPES = 7;

	public ItemQuartz() {
		super(LibItemNames.QUARTZ);
		setHasSubtypes(true);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		for(int i = ConfigHandler.darkQuartzEnabled ? 0 : 1; i < SUBTYPES; i++)
			list.add(new ItemStack(item, 1, i));
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		return getUnlocalizedNameLazy(par1ItemStack) + par1ItemStack.getItemDamage();
	}

	String getUnlocalizedNameLazy(ItemStack par1ItemStack) {
		return super.getUnlocalizedName(par1ItemStack);
	}

	@Override
	public boolean isElvenItem(ItemStack stack) {
		return stack.getItemDamage() == 5;
	}
}
