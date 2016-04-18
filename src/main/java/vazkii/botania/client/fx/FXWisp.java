/**
 * This class was created by <Azanor>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [? (GMT)]
 */
package vazkii.botania.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.lib.LibObfuscation;

import java.util.ArrayDeque;
import java.util.Queue;

public class FXWisp extends EntityFX {

	public static final ResourceLocation particles = new ResourceLocation(LibResources.MISC_WISP_LARGE);

	private static Queue<FXWisp> queuedRenders = new ArrayDeque<>();
	private static Queue<FXWisp> queuedDepthIgnoringRenders = new ArrayDeque<>();

	// Queue values
	private float f;
	private float f1;
	private float f2;
	private float f3;
	private float f4;
	private float f5;

	public FXWisp(World world, double d, double d1, double d2,  float size, float red, float green, float blue, boolean distanceLimit, boolean depthTest, float maxAgeMul) {
		super(world, d, d1, d2, 0.0D, 0.0D, 0.0D);
		particleRed = red;
		particleGreen = green;
		particleBlue = blue;
		particleAlpha = 0.5F; // So MC renders us on the alpha layer, value not actually used
		particleGravity = 0;
		xSpeed = ySpeed = zSpeed = 0;
		particleScale *= size;
		moteParticleScale = particleScale;
		particleMaxAge = (int)(28D / (Math.random() * 0.3D + 0.7D) * maxAgeMul);
		this.depthTest = depthTest;

		moteHalfLife = particleMaxAge / 2;
		setSize(0.01F, 0.01F);
		Entity renderentity = FMLClientHandler.instance().getClient().getRenderViewEntity();

		if(distanceLimit) {
			int visibleDistance = 50;
			if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics)
				visibleDistance = 25;

			if (renderentity == null || renderentity.getDistance(posX, posY, posZ) > visibleDistance)
				particleMaxAge = 0;
		}

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
	}

	public static void dispatchQueuedRenders(Tessellator tessellator) {
		ParticleRenderDispatcher.wispFxCount = 0;
		ParticleRenderDispatcher.depthIgnoringWispFxCount = 0;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F);
		Minecraft.getMinecraft().renderEngine.bindTexture(ConfigHandler.matrixMode ? ReflectionHelper.getPrivateValue(EffectRenderer.class, null, LibObfuscation.PARTICLE_TEXTURES) : particles);

		if(!queuedRenders.isEmpty()) {
			tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
			for(FXWisp wisp : queuedRenders)
				wisp.renderQueued(tessellator, true);
			tessellator.draw();
		}

		if(!queuedDepthIgnoringRenders.isEmpty()) {
			GlStateManager.disableDepth();
			tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
			for(FXWisp wisp : queuedDepthIgnoringRenders)
				wisp.renderQueued(tessellator, false);
			tessellator.draw();
			GlStateManager.enableDepth();
		}

		queuedRenders.clear();
		queuedDepthIgnoringRenders.clear();
	}

	private void renderQueued(Tessellator tessellator, boolean depthEnabled) {
		if(depthEnabled)
			ParticleRenderDispatcher.wispFxCount++;
		else ParticleRenderDispatcher.depthIgnoringWispFxCount++;

		float agescale = (float)particleAge / (float) moteHalfLife;
		if (agescale > 1F)
			agescale = 2 - agescale;

		particleScale = moteParticleScale * agescale;

		float f10 = 0.5F * particleScale;
		float f11 = (float)(prevPosX + (posX - prevPosX) * f - interpPosX);
		float f12 = (float)(prevPosY + (posY - prevPosY) * f - interpPosY);
		float f13 = (float)(prevPosZ + (posZ - prevPosZ) * f - interpPosZ);
		int combined = 15 << 20 | 15 << 4;
		int k3 = combined >> 16 & 0xFFFF;
		int l3 = combined & 0xFFFF;
		tessellator.getBuffer().pos(f11 - f1 * f10 - f4 * f10, f12 - f2 * f10, f13 - f3 * f10 - f5 * f10).tex(0, 1).lightmap(k3, l3).color(particleRed, particleGreen, particleBlue, 0.5F).endVertex();
		tessellator.getBuffer().pos(f11 - f1 * f10 + f4 * f10, f12 + f2 * f10, f13 - f3 * f10 + f5 * f10).tex(1, 1).lightmap(k3, l3).color(particleRed, particleGreen, particleBlue, 0.5F).endVertex();
		tessellator.getBuffer().pos(f11 + f1 * f10 + f4 * f10, f12 + f2 * f10, f13 + f3 * f10 + f5 * f10).tex(1, 0).lightmap(k3, l3).color(particleRed, particleGreen, particleBlue, 0.5F).endVertex();
		tessellator.getBuffer().pos(f11 + f1 * f10 - f4 * f10, f12 - f2 * f10, f13 + f3 * f10 - f5 * f10).tex(0, 0).lightmap(k3, l3).color(particleRed, particleGreen, particleBlue, 0.5F).endVertex();
	}

	@Override
	public void renderParticle(VertexBuffer wr, Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.f = f;
		this.f1 = f1;
		this.f2 = f2;
		this.f3 = f3;
		this.f4 = f4;
		this.f5 = f5;

		if(depthTest)
			queuedRenders.add(this);
		else queuedDepthIgnoringRenders.add(this);
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (particleAge++ >= particleMaxAge)
			setExpired();

		ySpeed -= 0.04D * particleGravity;
		posX += xSpeed;
		posY += ySpeed;
		posZ += zSpeed;
		xSpeed *= 0.98000001907348633D;
		ySpeed *= 0.98000001907348633D;
		zSpeed *= 0.98000001907348633D;
	}

	public void setGravity(float value) {
		particleGravity = value;
	}

	public void setSpeed(float mx, float my, float mz) {
		xSpeed = mx;
		ySpeed = my;
		zSpeed = mz;
	}

	private boolean depthTest = true;
	public boolean distanceLimit = true;
	private float moteParticleScale;
	private int moteHalfLife;
	public boolean tinkle = false;
	public int blendmode = 1;
}
