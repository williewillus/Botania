/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jun 29, 2015, 7:42:52 PM (GMT)]
 */
package vazkii.botania.client.gui.lexicon.button;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.gui.lexicon.GuiLexicon;

import java.util.ArrayList;
import java.util.List;

public class GuiButtonChallengeInfo extends GuiButtonLexicon {

	GuiLexicon gui;

	public GuiButtonChallengeInfo(int par1, int par2, int par3, String str, GuiLexicon gui) {
		super(par1, par2, par3, gui.bookmarkWidth(str) + 5, 11, str);
		this.gui = gui;
	}

	@Override
	public void drawButton(Minecraft mc, int par2, int par3) {
		gui.drawBookmark(xPosition, yPosition, displayString, false);
		hovered = par2 >= xPosition && par3 >= yPosition && par2 < xPosition + width && par3 < yPosition + height;
		int k = getHoverState(hovered);

		List<String> tooltip = new ArrayList<>();
		tooltip.add(I18n.translateToLocal("botaniamisc.challengeInfo"));

		int tooltipY = (tooltip.size() + 1) * 5;
		if(k == 2)
			RenderHelper.renderTooltip(par2, par3 + tooltipY, tooltip);
	}

}