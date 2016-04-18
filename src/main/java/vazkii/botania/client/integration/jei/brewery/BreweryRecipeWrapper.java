/**
 * This class was created by <williewillus>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p/>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei.brewery;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.recipe.RecipeBrew;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nonnull;
import java.util.List;

public class BreweryRecipeWrapper implements IRecipeWrapper {

	private final List input;
	private final List<ItemStack> output;

	@SuppressWarnings("unchecked")
	public BreweryRecipeWrapper(RecipeBrew recipeBrew) {
		ImmutableList.Builder builder = ImmutableList.builder();
		builder.add(ImmutableList.of(new ItemStack(ModItems.vial, 1, 0), new ItemStack(ModItems.vial, 1, 1)));
		for(Object o : recipeBrew.getInputs()) {
			if(o instanceof ItemStack) {
				builder.add(o);
			}
			if(o instanceof String) {
				builder.add(OreDictionary.getOres(((String) o)));
			}
		}

		input = builder.build();
		output = ImmutableList.of(recipeBrew.getOutput(new ItemStack(ModItems.vial)).copy(), recipeBrew.getOutput(new ItemStack(ModItems.vial, 1, 1)).copy());
	}

	@Override
	public List getInputs() {
		return input;
	}

	@Override
	public List<ItemStack> getOutputs() {
		return output;
	}

	@Override
	public List<FluidStack> getFluidInputs() {
		return ImmutableList.of();
	}

	@Override
	public List<FluidStack> getFluidOutputs() {
		return ImmutableList.of();
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
	}

	@Override
	public void drawAnimations(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		return ImmutableList.of();
	}

	@Override
	public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
		return false;
	}

}
