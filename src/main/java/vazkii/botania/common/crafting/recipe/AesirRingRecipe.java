/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 31, 2015, 10:39:58 PM (GMT)]
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.ModItems;

import java.util.UUID;

public class AesirRingRecipe implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting var1, World var2) {
		boolean foundThorRing = false;
		boolean foundOdinRing = false;
		boolean foundLokiRing = false;

		for(int i = 0; i < var1.getSizeInventory(); i++) {
			ItemStack stack = var1.getStackInSlot(i);
			if(stack != null) {
				if(stack.getItem() == ModItems.thorRing && !foundThorRing)
					foundThorRing = true;
				else if(stack.getItem() == ModItems.odinRing && !foundOdinRing)
					foundOdinRing = true;
				else if(stack.getItem() == ModItems.lokiRing && !foundLokiRing)
					foundLokiRing = true;
				else return false; // Found an invalid item, breaking the recipe
			}
		}

		return foundThorRing && foundOdinRing && foundLokiRing;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
		String soulbind = null;
		UUID soulbindUUID = null;
		boolean hasUUID = false;

		for(int i = 0; i < var1.getSizeInventory(); i++) {
			ItemStack stack = var1.getStackInSlot(i);
			if(stack != null) {
				if(stack.getItem() instanceof IRelic) {
					if(((IRelic) stack.getItem()).hasUUID(stack)) {
						hasUUID = true;
						UUID bindUUID = ((IRelic) stack.getItem()).getSoulbindUUID(stack);
						if(soulbindUUID == null)
							soulbindUUID = bindUUID;
						else if(!soulbindUUID.equals(bindUUID))
							return null;
					}
					else {
						String bind = ((IRelic) stack.getItem()).getSoulbindUsername(stack);
						if(soulbind == null)
							soulbind = bind;
						else if(!soulbind.equals(bind))
							return null;
					}
				} else return null;
			}
		}

		ItemStack stack = new ItemStack(ModItems.aesirRing);
		if(hasUUID)
			((IRelic) ModItems.aesirRing).bindToUUID(soulbindUUID, stack);
		else
			((IRelic) ModItems.aesirRing).bindToUsername(soulbind, stack);
		return stack;
	}

	@Override
	public int getRecipeSize() {
		return 10;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv) {
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}

}
