package vazkii.botania.common.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipeElvenTrade;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lib.LibOreDict;

import java.util.ArrayList;
import java.util.List;

public class ModElvenTradeRecipes {

	public static RecipeElvenTrade dreamwoodRecipe;
	public static List<RecipeElvenTrade> elementiumRecipes;
	public static RecipeElvenTrade pixieDustRecipe;
	public static List<RecipeElvenTrade> dragonstoneRecipes;
	public static RecipeElvenTrade elvenQuartzRecipe;
	public static RecipeElvenTrade alfglassRecipe;

	public static void init() {
		dreamwoodRecipe = BotaniaAPI.registerElvenTradeRecipe(new ItemStack(ModBlocks.dreamwood), LibOreDict.LIVING_WOOD);

		elementiumRecipes = new ArrayList<>();
		elementiumRecipes.add(BotaniaAPI.registerElvenTradeRecipe(new ItemStack(ModItems.manaResource, 1, 7), LibOreDict.MANA_STEEL, LibOreDict.MANA_STEEL));
		elementiumRecipes.add(BotaniaAPI.registerElvenTradeRecipe(new ItemStack(ModBlocks.storage, 1, 2), new ItemStack(ModBlocks.storage), new ItemStack(ModBlocks.storage)));

		pixieDustRecipe = BotaniaAPI.registerElvenTradeRecipe(new ItemStack(ModItems.manaResource, 1, 8), LibOreDict.MANA_PEARL);
		dragonstoneRecipes = new ArrayList<>();
		dragonstoneRecipes.add(BotaniaAPI.registerElvenTradeRecipe(new ItemStack(ModItems.manaResource, 1, 9), LibOreDict.MANA_DIAMOND));
		dragonstoneRecipes.add(BotaniaAPI.registerElvenTradeRecipe(new ItemStack(ModBlocks.storage, 1, 4), new ItemStack(ModBlocks.storage, 1, 3)));

		elvenQuartzRecipe = BotaniaAPI.registerElvenTradeRecipe(new ItemStack(ModItems.quartz, 1, 5), new ItemStack(Items.quartz));
		alfglassRecipe = BotaniaAPI.registerElvenTradeRecipe(new ItemStack(ModBlocks.elfGlass), new ItemStack(ModBlocks.manaGlass));

		BotaniaAPI.registerElvenTradeRecipe(new ItemStack(Items.iron_ingot), new ItemStack(Items.iron_ingot));
		BotaniaAPI.registerElvenTradeRecipe(new ItemStack(Blocks.iron_block), new ItemStack(Blocks.iron_block));
		BotaniaAPI.registerElvenTradeRecipe(new ItemStack(Items.ender_pearl), new ItemStack(Items.ender_pearl));
		BotaniaAPI.registerElvenTradeRecipe(new ItemStack(Items.diamond), new ItemStack(Items.diamond));
		BotaniaAPI.registerElvenTradeRecipe(new ItemStack(Blocks.diamond_block), new ItemStack(Blocks.diamond_block));
	}

}
