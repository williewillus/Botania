/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Apr 27, 2014, 8:32:52 PM (GMT)]
 */
package vazkii.botania.common.item.block;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import java.util.List;

public class ItemBlockPool extends ItemBlockWithMetaNameAndColor {

	public ItemBlockPool(Block par2Block) {
		super(par2Block);
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4) {
		if(par1ItemStack.getItemDamage() == 1)
			for(int i = 0; i < 2; i++)
				par3List.add(I18n.translateToLocal("botaniamisc.creativePool" + i));
	}

}
