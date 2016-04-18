/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Feb 8, 2014, 1:11:35 PM (GMT)]
 */
package vazkii.botania.common.lexicon.page;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.internal.IGuiLexiconEntry;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.lexicon.LexiconRecipeMappings;
import vazkii.botania.api.recipe.RecipePetals;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.core.handler.ConfigHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PagePetalRecipe<T extends RecipePetals> extends PageRecipe {

	private static final ResourceLocation petalOverlay = new ResourceLocation(LibResources.GUI_PETAL_OVERLAY);

	List<T> recipes;
	int ticksElapsed = 0;
	int recipeAt = 0;

	public PagePetalRecipe(String unlocalizedName, List<T> recipes) {
		super(unlocalizedName);
		this.recipes = recipes;
	}

	public PagePetalRecipe(String unlocalizedName, T recipe) {
		this(unlocalizedName, Collections.singletonList(recipe));
	}

	@Override
	public void onPageAdded(LexiconEntry entry, int index) {
		for(T recipe : recipes)
			LexiconRecipeMappings.map(recipe.getOutput(), entry, index);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderRecipe(IGuiLexiconEntry gui, int mx, int my) {
		T recipe = recipes.get(recipeAt);
		TextureManager render = Minecraft.getMinecraft().renderEngine;

		renderItemAtGridPos(gui, 3, 0, recipe.getOutput(), false);
		renderItemAtGridPos(gui, 2, 1, getMiddleStack(), false);

		List<Object> inputs = recipe.getInputs();
		int degreePerInput = (int) (360F / inputs.size());
		float currentDegree = ConfigHandler.lexiconRotatingItems ? GuiScreen.isShiftKeyDown() ? ticksElapsed : ticksElapsed + ClientTickHandler.partialTicks : 0;

		for(Object obj : inputs) {
			Object input = obj;
			if(input instanceof String)
				input = OreDictionary.getOres((String) input).get(0);

			renderItemAtAngle(gui, currentDegree, (ItemStack) input);

			currentDegree += degreePerInput;
		}

		renderManaBar(gui, recipe, mx, my);

		render.bindTexture(petalOverlay);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1F, 1F, 1F, 1F);
		((GuiScreen) gui).drawTexturedModalRect(gui.getLeft(), gui.getTop(), 0, 0, gui.getWidth(), gui.getHeight());
		GlStateManager.disableBlend();
	}

	ItemStack getMiddleStack() {
		return new ItemStack(ModBlocks.altar);
	}

	@SideOnly(Side.CLIENT)
	public void renderManaBar(IGuiLexiconEntry gui, T recipe, int mx, int my) {
		FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		int x = gui.getLeft() + gui.getWidth() / 2 - 50;
		int y = gui.getTop() + 120;

		boolean unicode = font.getUnicodeFlag();
		font.setUnicodeFlag(true);
		String stopStr = I18n.translateToLocal("botaniamisc.shiftToStopSpin");
		font.drawString(stopStr, x + 50 - font.getStringWidth(stopStr) / 2, y + 15, 0x99000000);
		font.setUnicodeFlag(unicode);

		GL11.glDisable(GL11.GL_BLEND);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateScreen() {
		if(GuiScreen.isShiftKeyDown())
			return;

		if(ticksElapsed % 20 == 0) {
			recipeAt++;

			if(recipeAt == recipes.size())
				recipeAt = 0;
		}
		++ticksElapsed;
	}

	@Override
	public List<ItemStack> getDisplayedRecipes() {
		ArrayList<ItemStack> list = new ArrayList<>();
		for(T r : recipes)
			list.add(r.getOutput());

		return list;
	}

}
