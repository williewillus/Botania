/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 19, 2014, 7:00:34 PM (GMT)]
 */
package vazkii.botania.common.item.material;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import vazkii.botania.common.crafting.recipe.FlowerCrushRecipe;
import vazkii.botania.common.crafting.recipe.ManaGunClipRecipe;
import vazkii.botania.common.item.ItemMod;
import vazkii.botania.common.lib.LibItemNames;

public class ItemPestleAndMortar extends ItemMod {

	public ItemPestleAndMortar() {
		super(LibItemNames.PESTLE_AND_MORTAR);
		setMaxStackSize(1);
		setContainerItem(this);

		GameRegistry.addRecipe(new FlowerCrushRecipe());
		RecipeSorter.register("botania:flowerCrush", FlowerCrushRecipe.class, RecipeSorter.Category.SHAPELESS, "");
	}

}
