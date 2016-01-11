/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 14, 2014, 8:34:01 PM (GMT)]
 */
package vazkii.botania.client.gui.lexicon.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.PersistentVariableHelper;
import vazkii.botania.client.core.helper.FontHelper;
import vazkii.botania.client.gui.lexicon.GuiLexiconIndex;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.lib.LibMisc;

public class GuiButtonInvisible extends GuiButtonLexicon {

	private static final ResourceLocation dogResource = new ResourceLocation(LibResources.GUI_DOG);
	
	GuiLexiconIndex gui;
	public ItemStack displayStack = null;
	public boolean dog = false;
	float timeHover = 0;
	
	boolean enableDog = false;
	double dogPos = 0;

	public GuiButtonInvisible(GuiLexiconIndex gui, int par1, int par2, int par3, int par4, int par5, String par6Str) {
		super(par1, par2, par3, par4, par5, par6Str);
		this.gui = gui;
	}
	
	public void click() {
		enableDog = true;
		PersistentVariableHelper.dog = true;
		PersistentVariableHelper.saveSafe();
	}

	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3) {
		if(enableDog) {
			dogPos += ClientTickHandler.delta * 10;
			
			par1Minecraft.renderEngine.bindTexture(dogResource);
			float f = 1F / 64F;
			GL11.glTranslated(dogPos, 0, 0);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			vazkii.botania.client.core.helper.RenderHelper.drawTexturedModalRect(0, yPosition, zLevel + 10, (dogPos % 100 < 50) ? 23 : 0, 0, 23, 19, f, f);
			xPosition = (int) Math.max(xPosition, dogPos + 10);
			
			GL11.glTranslated(-dogPos, 0, 0);
		}
		
		hovered = par2 >= xPosition && par3 >= yPosition && par2 < xPosition + width && par3 < yPosition + height;
		int k = getHoverState(hovered);
		boolean showStack = displayStack != null && !displayString.isEmpty();

		if(!displayString.isEmpty() && k == 2) {
			timeHover = Math.min(5, timeHover + gui.timeDelta);
			gui.setHoveredButton(this);
		} else timeHover = Math.max(0, timeHover - gui.timeDelta);

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GlStateManager.disableAlpha();
		int color = 0;
		String format = FontHelper.getFormatFromString(displayString);
		if(format.length() > 1) {
			char key = format.charAt(format.length() - 1);
			if(key == 'o' && format.length() > 3)
				key = format.charAt(1);

			for(EnumChatFormatting ecf : EnumChatFormatting.class.getEnumConstants())
				if(ecf.toString().indexOf(ecf.toString().length() - 1) == key) { // todo 1.8 recheck (want last char of code)
					if(ecf.ordinal() > 15)
						ecf = EnumChatFormatting.BLACK;
					color = LibMisc.CONTROL_CODE_COLORS[ecf.ordinal()];
					break;
				}
		}

		int maxalpha = 0x22;
		int alpha = Math.min(maxalpha, (int) (timeHover / 4 * maxalpha));
		drawRect(xPosition - 5, yPosition, (int) (xPosition - 5 + timeHover * 24), yPosition + height, alpha << 24 | color);
		GlStateManager.enableAlpha();

		boolean unicode = par1Minecraft.fontRendererObj.getUnicodeFlag();
		par1Minecraft.fontRendererObj.setUnicodeFlag(true);
		par1Minecraft.fontRendererObj.drawString(displayString, xPosition + (showStack ? 7 : 0), yPosition + (height - 8) / 2, 0);
		par1Minecraft.fontRendererObj.setUnicodeFlag(unicode);

		if(showStack) {
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.enableRescaleNormal();
			par1Minecraft.getRenderItem().renderItemIntoGUI(displayStack, xPosition * 2 - 6, yPosition * 2 + 4);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.enableBlend();
		}
		GlStateManager.popMatrix();
	}

}
