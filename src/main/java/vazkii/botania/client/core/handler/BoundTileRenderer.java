/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 24, 2014, 7:02:37 PM (GMT)]
 */
package vazkii.botania.client.core.handler;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.IExtendedWireframeCoordinateListProvider;
import vazkii.botania.api.item.IWireframeCoordinateListProvider;
import vazkii.botania.api.wand.ICoordBoundItem;
import vazkii.botania.api.wand.IWireframeAABBProvider;

import java.awt.*;
import java.util.List;

public final class BoundTileRenderer {

	@SubscribeEvent
	public void onWorldRenderLast(RenderWorldLastEvent event) {
		GlStateManager.pushMatrix();
		GL11.glPushAttrib(GL11.GL_LIGHTING);
		GlStateManager.disableDepth();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		int color = Color.HSBtoRGB(ClientTickHandler.ticksInGame % 200 / 200F, 0.6F, 1F);
		
		if(player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ICoordBoundItem) {
			BlockPos coords = ((ICoordBoundItem) player.getHeldItemMainhand().getItem()).getBinding(player.getHeldItemMainhand());
			if(coords != null)
				renderBlockOutlineAt(coords, color);
		}

		if(player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() instanceof ICoordBoundItem) {
			BlockPos coords = ((ICoordBoundItem) player.getHeldItemOffhand().getItem()).getBinding(player.getHeldItemOffhand());
			if(coords != null)
				renderBlockOutlineAt(coords, color);
		}

		IItemHandlerModifiable mainInv = ((IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP));
		IItemHandlerModifiable baublesInv = BotaniaAPI.internalHandler.getBaublesInventoryWrapped(player);
		IItemHandler joined = baublesInv != null ? new CombinedInvWrapper(mainInv, baublesInv) : mainInv;

		for(int i = 0; i < joined.getSlots(); i++) {
			ItemStack stackInSlot = joined.getStackInSlot(i);

			if(stackInSlot != null && stackInSlot.getItem() instanceof IWireframeCoordinateListProvider) {
				IWireframeCoordinateListProvider provider = (IWireframeCoordinateListProvider) stackInSlot.getItem();
				List<BlockPos> coordsList = provider.getWireframesToDraw(player, stackInSlot);
				if(coordsList != null)
					for(BlockPos coords : coordsList)
						renderBlockOutlineAt(coords, color);

				if(stackInSlot.getItem() instanceof IExtendedWireframeCoordinateListProvider) {
					BlockPos coords = ((IExtendedWireframeCoordinateListProvider) stackInSlot.getItem()).getSourceWireframe(player, stackInSlot);
					if(coords != null && coords.getY() > -1)
						renderBlockOutlineAt(coords, color, 5F);
				}
			}
		}

		GlStateManager.enableDepth();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GL11.glPopAttrib();
		GlStateManager.popMatrix();
	}

	private void renderBlockOutlineAt(BlockPos pos, int color) {
		renderBlockOutlineAt(pos, color, 1F);
	}

	private void renderBlockOutlineAt(BlockPos pos, int color, float thickness) {
		double renderPosX, renderPosY, renderPosZ;

		try {
			renderPosX = (double) ClientMethodHandles.renderPosX_getter.invokeExact(Minecraft.getMinecraft().getRenderManager());
			renderPosY = (double) ClientMethodHandles.renderPosY_getter.invokeExact(Minecraft.getMinecraft().getRenderManager());
			renderPosZ = (double) ClientMethodHandles.renderPosZ_getter.invokeExact(Minecraft.getMinecraft().getRenderManager());
		} catch (Throwable t) {
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(pos.getX() - renderPosX, pos.getY() - renderPosY, pos.getZ() - renderPosZ + 1);
		Color colorRGB = new Color(color);
		GL11.glColor4ub((byte) colorRGB.getRed(), (byte) colorRGB.getGreen(), (byte) colorRGB.getBlue(), (byte) 255);

		World world = Minecraft.getMinecraft().theWorld;
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		drawWireframe : {
			if(block != null) {
				AxisAlignedBB axis;

				if(block instanceof IWireframeAABBProvider)
					axis = ((IWireframeAABBProvider) block).getWireframeAABB(world, pos);
				else axis = state.getSelectedBoundingBox(world, pos);

				if(axis == null)
					break drawWireframe;

				axis = axis.offset(-pos.getX(), -pos.getY(), -(pos.getZ() + 1));

				GlStateManager.scale(1F, 1F, 1F);

				GL11.glLineWidth(thickness);
				renderBlockOutline(axis);

				GL11.glLineWidth(thickness + 3F);
				GL11.glColor4ub((byte) colorRGB.getRed(), (byte) colorRGB.getGreen(), (byte) colorRGB.getBlue(), ((byte) 64));
				renderBlockOutline(axis);
			}
		}

		GL11.glColor4ub(((byte) 255), ((byte) 255), ((byte) 255), ((byte) 255));
		GlStateManager.popMatrix();
	}

	private void renderBlockOutline(AxisAlignedBB aabb) {
		Tessellator tessellator = Tessellator.getInstance();

		double ix = aabb.minX;
		double iy = aabb.minY;
		double iz = aabb.minZ;
		double ax = aabb.maxX;
		double ay = aabb.maxY;
		double az = aabb.maxZ;

		tessellator.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		tessellator.getBuffer().pos(ix, iy, iz).endVertex();
		tessellator.getBuffer().pos(ix, ay, iz).endVertex();

		tessellator.getBuffer().pos(ix, ay, iz).endVertex();
		tessellator.getBuffer().pos(ax, ay, iz).endVertex();

		tessellator.getBuffer().pos(ax, ay, iz).endVertex();
		tessellator.getBuffer().pos(ax, iy, iz).endVertex();

		tessellator.getBuffer().pos(ax, iy, iz).endVertex();
		tessellator.getBuffer().pos(ix, iy, iz).endVertex();

		tessellator.getBuffer().pos(ix, iy, az).endVertex();
		tessellator.getBuffer().pos(ix, ay, az).endVertex();

		tessellator.getBuffer().pos(ix, iy, az).endVertex();
		tessellator.getBuffer().pos(ax, iy, az).endVertex();

		tessellator.getBuffer().pos(ax, iy, az).endVertex();
		tessellator.getBuffer().pos(ax, ay, az).endVertex();

		tessellator.getBuffer().pos(ix, ay, az).endVertex();
		tessellator.getBuffer().pos(ax, ay, az).endVertex();

		tessellator.getBuffer().pos(ix, iy, iz).endVertex();
		tessellator.getBuffer().pos(ix, iy, az).endVertex();

		tessellator.getBuffer().pos(ix, ay, iz).endVertex();
		tessellator.getBuffer().pos(ix, ay, az).endVertex();

		tessellator.getBuffer().pos(ax, iy, iz).endVertex();
		tessellator.getBuffer().pos(ax, iy, az).endVertex();

		tessellator.getBuffer().pos(ax, ay, iz).endVertex();
		tessellator.getBuffer().pos(ax, ay, az).endVertex();

		tessellator.draw();
	}
}
