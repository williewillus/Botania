/**
 * This class was created by <williewillus>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p/>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei.puredaisy;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import vazkii.botania.common.item.block.ItemBlockSpecialFlower;
import vazkii.botania.common.lib.LibBlockNames;

import javax.annotation.Nonnull;
import java.util.Collection;

public class PureDaisyRecipeCategory implements IRecipeCategory {

	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;

	public PureDaisyRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(168, 64);
		localizedName = I18n.translateToLocal("botania.nei.pureDaisy");
		overlay = guiHelper.createDrawable(new ResourceLocation("botania", "textures/gui/pureDaisyOverlay.png"),
				0, 0, 64, 46);
	}

	@Nonnull
	@Override
	public String getUid() {
		return "botania.pureDaisy";
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(@Nonnull Minecraft minecraft) {
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		overlay.draw(minecraft, 48, 0);
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
	}

	@Override
	public void drawAnimations(@Nonnull Minecraft minecraft) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		if(!(recipeWrapper instanceof PureDaisyRecipeWrapper))
			return;

		PureDaisyRecipeWrapper wrapper = ((PureDaisyRecipeWrapper) recipeWrapper);
		boolean inputFluid = wrapper.getInputs().isEmpty();
		boolean outputFluid = wrapper.getOutputs().isEmpty();


		if(inputFluid) {
			recipeLayout.getFluidStacks().init(0, true, 40, 12, 16, 16, 1000, false, null);
			recipeLayout.getFluidStacks().set(0, wrapper.getFluidInputs().get(0));
		} else {
			recipeLayout.getItemStacks().init(0, true, 40, 12);
			if(wrapper.getInputs().get(0) instanceof Collection) {
				recipeLayout.getItemStacks().set(0, ((Collection<ItemStack>) wrapper.getInputs().get(0)));
			} else {
				recipeLayout.getItemStacks().set(0, ((ItemStack) wrapper.getInputs().get(0)));
			}
		}

		recipeLayout.getItemStacks().init(1, true, 70, 12);
		recipeLayout.getItemStacks().set(1, ItemBlockSpecialFlower.ofType(LibBlockNames.SUBTILE_PUREDAISY));

		if(outputFluid) {
			recipeLayout.getFluidStacks().init(2, false, 99, 12, 16, 16, 1000, false, null);
			recipeLayout.getFluidStacks().set(2, wrapper.getFluidOutputs().get(0));
		} else {
			recipeLayout.getItemStacks().init(2, false, 99, 12);
			if(wrapper.getOutputs().get(0) instanceof Collection) {
				recipeLayout.getItemStacks().set(2, ((Collection<ItemStack>) wrapper.getOutputs().get(0)));
			} else {
				recipeLayout.getItemStacks().set(2, ((ItemStack) wrapper.getOutputs().get(0)));
			}
		}
	}

}
