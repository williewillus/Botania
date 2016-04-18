/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Aug 27, 2014, 8:55:00 PM (GMT)]
 */
package vazkii.botania.client.core.handler;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.api.item.IBaubleRender.Helper;
import vazkii.botania.api.item.IBaubleRender.RenderType;
import vazkii.botania.api.item.ICosmeticAttachable;
import vazkii.botania.api.item.IPhantomInkable;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.armor.terrasteel.ItemTerrasteelHelm;

public final class BaubleRenderHandler implements LayerRenderer<EntityPlayer> {

	@Override
	public void doRenderLayer(EntityPlayer player, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
		if(!ConfigHandler.renderBaubles || player.getActivePotionEffect(MobEffects.invisibility) != null)
			return;

		InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);

		dispatchRenders(inv, player, RenderType.BODY, partialTicks);
		if(inv.getStackInSlot(3) != null)
			renderManaTablet(player);

		float yaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
		float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

		GlStateManager.pushMatrix();
		GlStateManager.rotate(yawOffset, 0, -1, 0);
		GlStateManager.rotate(yaw - 270, 0, 1, 0);
		GlStateManager.rotate(pitch, 0, 0, 1);
		dispatchRenders(inv, player, RenderType.HEAD, partialTicks);

		ItemStack helm = player.inventory.armorItemInSlot(3);
		if(helm != null && helm.getItem() instanceof ItemTerrasteelHelm)
			ItemTerrasteelHelm.renderOnPlayer(helm, player);

		GlStateManager.popMatrix();
	}

	private void dispatchRenders(InventoryBaubles inv, EntityPlayer player, RenderType type, float partialTicks) {
		for(int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if(stack != null) {
				Item item = stack.getItem();

				if(item instanceof IPhantomInkable) {
					IPhantomInkable inkable = (IPhantomInkable) item;
					if(inkable.hasPhantomInk(stack))
						continue;
				}

				if(item instanceof ICosmeticAttachable) {
					ICosmeticAttachable attachable = (ICosmeticAttachable) item;
					ItemStack cosmetic = attachable.getCosmeticItem(stack);
					if(cosmetic != null) {
						GlStateManager.pushMatrix();
						GL11.glColor3ub(((byte) 255), ((byte) 255), ((byte) 255)); // Some of the baubles use this so we must restore it manually as well
						GlStateManager.color(1F, 1F, 1F, 1F);
						((IBaubleRender) cosmetic.getItem()).onPlayerBaubleRender(cosmetic, player, type, partialTicks);
						GlStateManager.popMatrix();
						continue;
					}
				}

				if(item instanceof IBaubleRender) {
					GlStateManager.pushMatrix();
					GL11.glColor3ub(((byte) 255), ((byte) 255), ((byte) 255)); // Some of the baubles use this so we must restore it manually as well
					GlStateManager.color(1F, 1F, 1F, 1F);
					((IBaubleRender) stack.getItem()).onPlayerBaubleRender(stack, player, type, partialTicks);
					GlStateManager.popMatrix();
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void renderManaTablet(EntityPlayer player) {
		boolean renderedOne = false;
		for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if(stack != null && stack.getItem() == ModItems.manaTablet) {
				GlStateManager.pushMatrix();
				Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				Helper.rotateIfSneaking(player);
				boolean armor = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS) != null;
				GlStateManager.rotate(90, 0, 1, 0);
				GlStateManager.rotate(180, 0, 0, 1);
				GlStateManager.translate(0, -0.6, 0);
				GlStateManager.scale(0.55, 0.55, 0.55);

				if (renderedOne)
					GlStateManager.translate(0F, 0F, armor ? 0.55F : 0.5F);
				else
					GlStateManager.translate(0F, 0F, armor ? -0.55F : -0.5F);

				GlStateManager.scale(0.75F, 0.75F, 0.75F);

				GlStateManager.color(1F, 1F, 1F);
				int light = 15728880;
				int lightmapX = light % 65536;
				int lightmapY = light / 65536;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapX, lightmapY);
				Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
				GlStateManager.popMatrix();

				if(renderedOne)
					return;
				renderedOne = true;
			}
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
