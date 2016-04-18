/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 30, 2014, 6:10:48 PM (GMT)]
 */
package vazkii.botania.common.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipeManaInfusion;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public final class ModManaInfusionRecipes {

	public static List<RecipeManaInfusion> manasteelRecipes;
	public static RecipeManaInfusion manaPearlRecipe;
	public static List<RecipeManaInfusion> manaDiamondRecipes;
	public static List<RecipeManaInfusion> manaPowderRecipes;
	public static RecipeManaInfusion pistonRelayRecipe;
	public static RecipeManaInfusion manaCookieRecipe;
	public static RecipeManaInfusion grassSeedsRecipe;
	public static RecipeManaInfusion podzolSeedsRecipe;
	public static List<RecipeManaInfusion> mycelSeedsRecipes;
	public static RecipeManaInfusion manaQuartzRecipe;
	public static RecipeManaInfusion tinyPotatoRecipe;
	public static RecipeManaInfusion manaInkwellRecipe;
	public static RecipeManaInfusion managlassRecipe;
	public static RecipeManaInfusion manaStringRecipe;

	public static RecipeManaInfusion sugarCaneRecipe;

	public static void init() {
		manasteelRecipes = new ArrayList<>();
		manasteelRecipes.add(BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaResource, 1, 0), "ingotIron", 3000));
		manasteelRecipes.add(BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModBlocks.storage, 1, 0), new ItemStack(Blocks.iron_block), 27000));

		manaPearlRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaResource, 1, 1), new ItemStack(Items.ender_pearl), 6000);

		manaDiamondRecipes = new ArrayList<>();
		manaDiamondRecipes.add(BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaResource, 1, 2), "gemDiamond", 10000));
		manaDiamondRecipes.add(BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModBlocks.storage, 1, 3), new ItemStack(Blocks.diamond_block), 90000));

		manaPowderRecipes = new ArrayList<>();
		manaPowderRecipes.add(BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaResource, 1, 23), new ItemStack(Items.gunpowder), 500));
		manaPowderRecipes.add(BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaResource, 1, 23), new ItemStack(Items.redstone), 500));
		manaPowderRecipes.add(BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaResource, 1, 23), new ItemStack(Items.glowstone_dust), 500));
		manaPowderRecipes.add(BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaResource, 1, 23), new ItemStack(Items.sugar), 500));
		for(int i = 0; i < 16; i++)
			manaPowderRecipes.add(BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaResource, 1, 23), new ItemStack(ModItems.dye, 1, i), 400));

		pistonRelayRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModBlocks.pistonRelay), new ItemStack(Blocks.piston), 15000);
		manaCookieRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaCookie), new ItemStack(Items.cookie), 20000);
		grassSeedsRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.grassSeeds), new ItemStack(Blocks.tallgrass, 1, 1), 2500);
		podzolSeedsRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.grassSeeds, 1, 1), new ItemStack(Blocks.deadbush), 2500);

		mycelSeedsRecipes = new ArrayList<>();
		mycelSeedsRecipes.add(BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.grassSeeds, 1, 2), new ItemStack(Blocks.red_mushroom), 6500));
		mycelSeedsRecipes.add(BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.grassSeeds, 1, 2), new ItemStack(Blocks.brown_mushroom), 6500));

		manaQuartzRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.quartz, 1, 1), new ItemStack(Items.quartz), 250);
		tinyPotatoRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModBlocks.tinyPotato), new ItemStack(Items.potato), 1337);

		if(Botania.thaumcraftLoaded) {
			Item inkwell = Item.itemRegistry.getObject(new ResourceLocation("thaumcraft", "scribing_tools"));
			manaInkwellRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaInkwell, 1, ModItems.manaInkwell.getMaxDamage()), new ItemStack(inkwell), 35000);
		}

		managlassRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModBlocks.manaGlass), new ItemStack(Blocks.glass), 150);
		manaStringRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaResource, 1, 16), new ItemStack(Items.string), 5000);

		if(Botania.gardenOfGlassLoaded)
			sugarCaneRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(Items.reeds), new ItemStack(Blocks.hay_block), 2000);

		BotaniaAPI.registerManaInfusionRecipe(new ItemStack(ModItems.manaBottle), new ItemStack(Items.glass_bottle), 5000);
	}

}
