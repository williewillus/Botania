/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Apr 5, 2016, 6:29:33 PM (EST)]
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nullable;
import java.util.List;

public class FlowerCrushRecipe implements IRecipe {

    private static boolean doingFlowerRecipe = false;

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        if (doingFlowerRecipe) return false;
        doingFlowerRecipe = true;

        boolean foundMortar = false;
        boolean foundFlower = false;

        for (int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if (stack != null) {
                if (stack.getItem() == ModItems.pestleAndMortar) {
                    if (foundMortar) return false;
                    foundMortar = true;
                } else {
                    if (foundFlower || dyeOutput(stack) == null) return false;
                    foundFlower = true;
                }
            }
        }
        doingFlowerRecipe = false;
        return foundMortar && foundFlower;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        ItemStack flower = null;
        for (int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if (stack != null && stack.getItem() != ModItems.pestleAndMortar)
                flower = stack;
        }
        if (flower == null) return null;

        return dyeOutput(flower);
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
    public ItemStack[] getRemainingItems(InventoryCrafting inventoryCrafting) {
        return ForgeHooks.defaultRecipeGetRemainingItems(inventoryCrafting);
    }


    private InventoryCrafting withStack(ItemStack stack) {
        InventoryCrafting craft = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer player) {
                return false;
            }
        }, 3, 3);
        craft.setInventorySlotContents(4, stack);
        return craft;
    }

    private static final String[] dyeNames = new String[] {"White", "Orange", "Magenta", "LightBlue", "Yellow", "Lime", "Pink", "Gray", "LightGray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"};
    private static final String[] dyes = new String[dyeNames.length];

    static {
        for (int i = 0; i < dyeNames.length; i++) {
            dyes[i] = "dye" + dyeNames[i];
        }
    }

    private @Nullable ItemStack dyeOutput(ItemStack stack) {
        ItemStack result = CraftingManager.getInstance().findMatchingRecipe(withStack(stack.copy()), null);
        if (result.getItem() == ModItems.dye)
            return null;
        for (int i = 0; i < dyes.length; i++) {
            if (checkStack(result, dyes[i])) {
                return new ItemStack(ModItems.dye, result.stackSize, i);
            }
        }
        return null;
    }

    private static boolean checkStack(ItemStack stack, String key) {
        List<ItemStack> ores = OreDictionary.getOres(key, false);
        for (ItemStack ore : ores) {
            if (OreDictionary.itemMatches(stack, ore, false))
                return true;
        }
        return false;
    }
}
