/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Apr 30, 2015, 4:10:14 PM (GMT)]
 */
package vazkii.botania.client.render.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.model.ModelCrystalCube;
import vazkii.botania.common.block.tile.corporea.TileCorporeaCrystalCube;
import vazkii.botania.common.core.handler.MethodHandles;

public class RenderTileCorporeaCrystalCube extends TileEntitySpecialRenderer<TileCorporeaCrystalCube> {

	ModelCrystalCube model = new ModelCrystalCube();
	EntityItem entity = null;

	@Override
	public void renderTileEntityAt(TileCorporeaCrystalCube cube, double d0, double d1, double d2, float f, int digProgress) {
		ItemStack stack = null;
		if (cube != null) {
			if(entity == null)
                entity = new EntityItem(cube.getWorld(), cube.getPos().getX(), cube.getPos().getY(), cube.getPos().getZ(), new ItemStack(Blocks.stone));

			try {
				MethodHandles.itemAge_setter.invokeExact(entity, ClientTickHandler.ticksInGame);
			} catch (Throwable ignored) {}

			stack = cube.getRequestTarget();
			entity.setEntityItemStack(stack);
		}

		double time = ClientTickHandler.ticksInGame + f;
		double worldTicks = cube == null || cube.getWorld() == null ? 0 : time;

		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.translate(d0, d1, d2);
		GlStateManager.translate(0.5F, 1.5F, 0.5F);
		GlStateManager.scale(1F, -1F, -1F);
		GlStateManager.translate(0F, (float) Math.sin(worldTicks / 20.0 * 1.55) * 0.025F, 0F);
		if(stack != null) {
			GlStateManager.pushMatrix();
			float s = stack.getItem() instanceof ItemBlock ? 0.7F : 0.5F;
			GlStateManager.translate(0F, 0.8F, 0F);
			GlStateManager.scale(s, s, s);
			GlStateManager.rotate(180F, 0F, 0F, 1F);
			((Render) mc.getRenderManager().entityRenderMap.get(EntityItem.class)).doRender(entity, 0, 0, 0, 1F, f);
			GlStateManager.popMatrix();
		}

		GlStateManager.color(1F, 1F, 1F);

		if(stack != null) {
			int count = cube.getItemCount();
			String countStr = "" + count;
			int color = 0xFFFFFF;
			if(count > 9999) {
				countStr = count / 1000 + "K";
				color = 0xFFFF00;
				if(count > 9999999) {
					countStr = count / 10000000 + "M";
					color = 0x00FF00;
				}
			}
			color |= 0xA0 << 24;
			int colorShade = (color & 16579836) >> 2 | color & -16777216;

			float s = 1F / 64F;
			GlStateManager.scale(s, s, s);
			GlStateManager.disableLighting();
			int l = mc.fontRendererObj.getStringWidth(countStr);

			GlStateManager.translate(0F, 55F, 0F);
			float tr = -16.5F;
			for(int i = 0; i < 4; i++) {
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				GlStateManager.translate(0F, 0F, tr);
				mc.fontRendererObj.drawString(countStr, -l / 2, 0, color);
				GlStateManager.translate(0F, 0F, 0.1F);
				mc.fontRendererObj.drawString(countStr, -l / 2 + 1, 1, colorShade);
				GlStateManager.translate(0F, 0F, -tr - 0.1F);
			}
			GlStateManager.enableLighting();
		}

		GlStateManager.scale(1F, -1F, -1F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.popMatrix();

		renderAnimatedModel(cube, d0, d1, d2, f);
	}

	// Copied from AnimationTESR
	protected static BlockRendererDispatcher blockRenderer;

	private void renderAnimatedModel(TileCorporeaCrystalCube te, double x, double y, double z, float partialTick) {
		// From FastTESR.renderTileEntityAt
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldRenderer = tessellator.getBuffer();
		this.bindTexture(TextureMap.locationBlocksTexture);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();

		if (Minecraft.isAmbientOcclusionEnabled())
		{
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		}
		else
		{
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		// Inlined AnimationTESR.renderTileEntityFast
		if(blockRenderer == null) blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos pos = te.getPos();
		IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
		IBlockState state = world.getBlockState(pos);
		if(state.getPropertyNames().contains(Properties.StaticProperty))
		{
			state = state.withProperty(Properties.StaticProperty, false);
		}
		if(state instanceof IExtendedBlockState)
		{
			IExtendedBlockState exState = (IExtendedBlockState)state;
			if(exState.getUnlistedNames().contains(Properties.AnimationProperty))
			{
				float time = Animation.getWorldTime(getWorld(), partialTick);
				// todo 1.9
				if (true) return;
				Pair<IModelState, Iterable<Event>> pair = null;
				// handleEvents(te, time, pair.getRight());

				IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(exState.getClean());
				exState = exState.withProperty(Properties.AnimationProperty, pair.getLeft());

				worldRenderer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());

				blockRenderer.getBlockModelRenderer().renderModel(world, model, exState, pos, worldRenderer, false);
			}
		}
		// End inline AnimationTESR.renderTileEntityFast

		worldRenderer.setTranslation(0, 0, 0);

		tessellator.draw();

		RenderHelper.enableStandardItemLighting();
	}

}
