/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jun 29, 2015, 4:16:09 PM (GMT)]
 */
package vazkii.botania.client.gui.lexicon.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.gui.lexicon.GuiLexicon;

import java.util.ArrayList;
import java.util.List;

public class GuiButtonChallenges extends GuiButtonLexicon {

	public GuiButtonChallenges(int id, int x, int y) {
		super(id, x, y, 11, 11, "");
	}

	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3) {
		hovered = par2 >= xPosition && par3 >= yPosition && par2 < xPosition + width && par3 < yPosition + height;
		int k = getHoverState(hovered);

		par1Minecraft.renderEngine.bindTexture(GuiLexicon.texture);
		GlStateManager.color(1F, 1F, 1F, 1F);
		drawTexturedModalRect(xPosition, yPosition, k == 2 ? 131 : 120, 180, 11, 11);

		List<String> tooltip = new ArrayList();
		tooltip.add(TextFormatting.YELLOW + I18n.translateToLocal("botaniamisc.challenges"));

		int tooltipY = (tooltip.size() - 1) * 10;
		if(k == 2)
			RenderHelper.renderTooltip(par2, par3 + tooltipY, tooltip);
	}

}