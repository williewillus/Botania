/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Oct 29, 2014, 6:46:10 PM (GMT)]
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.boss.IBotaniaBoss;
import vazkii.botania.api.boss.IBotaniaBossWithShader;
import vazkii.botania.api.internal.ShaderCallback;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.core.helper.ShaderHelper;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.core.helper.MathHelper;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class BossBarHandler {

	// Only access on the client thread!
	public static final Set<IBotaniaBoss> bosses = new HashSet<>();
	public static final ResourceLocation defaultBossBar = new ResourceLocation(LibResources.GUI_BOSS_BAR);
	private static final BarCallback barUniformCallback = new BarCallback();

	@SubscribeEvent
	public void onBarRender(RenderGameOverlayEvent.BossInfo evt) {
		UUID infoUuid = evt.getBossInfo().getUniqueId();
		for(IBotaniaBoss currentBoss : bosses) {
			if(currentBoss.getBossInfoUuid().equals(infoUuid)) {
				evt.setCanceled(true);

				Minecraft mc = Minecraft.getMinecraft();
				Rectangle bgRect = currentBoss.getBossBarTextureRect();
				Rectangle fgRect = currentBoss.getBossBarHPTextureRect();
				String name = evt.getBossInfo().getName().getFormattedText();
				int c = evt.getResolution().getScaledWidth() / 2;
				int x = evt.getX();
				int y = evt.getY();
				int xf = x + (bgRect.width - fgRect.width) / 2;
				int yf = y + (bgRect.height - fgRect.height) / 2;
				int fw = (int) ((double) fgRect.width * (evt.getBossInfo().getPercent()));
				int tx = c - mc.fontRendererObj.getStringWidth(name) / 2;

				GlStateManager.color(1F, 1F, 1F, 1F);
				int auxHeight = currentBoss.bossBarRenderCallback(evt.getResolution(), x, y);
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				mc.renderEngine.bindTexture(currentBoss.getBossBarTexture());
				drawBar(currentBoss, x, y, bgRect.x, bgRect.y, bgRect.width, bgRect.height, true);
				drawBar(currentBoss, xf, yf, fgRect.x, fgRect.y, fw, fgRect.height, false);
				mc.fontRendererObj.drawStringWithShadow(name, tx, y - 10, 0xA2018C);
				GlStateManager.enableBlend();
				evt.setIncrement(Math.max(bgRect.height, fgRect.height) + auxHeight + mc.fontRendererObj.FONT_HEIGHT);
			}
		}
	}

	private static void drawBar(IBotaniaBoss currentBoss, int x, int y, int u, int v, int w, int h, boolean bg) {
		boolean useShader = currentBoss instanceof IBotaniaBossWithShader;
		if(useShader) {
			IBotaniaBossWithShader shader = (IBotaniaBossWithShader) currentBoss;
			int program = shader.getBossBarShaderProgram(bg);
			ShaderCallback callback = program == 0 ? null : shader.getBossBarShaderCallback(bg, program);
			barUniformCallback.set(u, v, callback);

			ShaderHelper.useShader(program, barUniformCallback);
		}

		RenderHelper.drawTexturedModalRect(x, y, 0, u, v, w, h);

		if(useShader)
			ShaderHelper.releaseShader();
	}

	private static class BarCallback implements ShaderCallback {
		int x, y;
		ShaderCallback callback;

		@Override
		public void call(int shader) {
			int startXUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "startX");
			int startYUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "startY");


			ARBShaderObjects.glUniform1iARB(startXUniform, x);
			ARBShaderObjects.glUniform1iARB(startYUniform, y);

			if(callback != null)
				callback.call(shader);
		}

		void set(int x, int y, ShaderCallback callback) {
			this.x = x;
			this.y = y;
			this.callback = callback;
		}
	}

}
