/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Aug 27, 2014, 8:55:00 PM (GMT)]
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.api.item.IBaubleRender.RenderType;
import vazkii.botania.api.item.ICosmeticAttachable;
import vazkii.botania.api.item.IPhantomInkable;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.armor.terrasteel.ItemTerrasteelHelm;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;

public final class BaubleRenderHandler implements LayerRenderer<EntityPlayer> {

    @Override
    public void doRenderLayer(EntityPlayer player, float f0, float f1, float partialTicks, float f3, float f4, float f5, float scale) {
        if (!ConfigHandler.renderBaubles || player.getActivePotionEffect(Potion.invisibility) != null)
            return;
        InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);
        dispatchRenders(inv, player, partialTicks, RenderType.BODY);
        if (inv.getStackInSlot(3) != null)
            renderManaTablet(player);

        float yaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
        float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.rotate(yawOffset, 0, -1, 0);
        GlStateManager.rotate(yaw - 270, 0, 1, 0);
        GlStateManager.rotate(pitch, 0, 0, 1);
        dispatchRenders(inv, player, partialTicks, RenderType.HEAD);
        ItemStack helm = player.inventory.armorItemInSlot(3);
        if (helm != null && helm.getItem() instanceof ItemTerrasteelHelm)
            ItemTerrasteelHelm.renderOnPlayer(helm, player);

        GlStateManager.popMatrix();
    }

    private void dispatchRenders(InventoryBaubles inv, EntityPlayer player, float partialTicks, RenderType type) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null) {
                Item item = stack.getItem();

                if(item instanceof IPhantomInkable) {
                    IPhantomInkable inkable = (IPhantomInkable) item;
                    if(inkable.hasPhantomInk(stack))
                        continue;
                }

                if (item instanceof ICosmeticAttachable) {
                    ICosmeticAttachable attachable = (ICosmeticAttachable) item;
                    ItemStack cosmetic = attachable.getCosmeticItem(stack);
                    if (cosmetic != null) {
                        GlStateManager.pushMatrix();
                        GlStateManager.color(1F, 1F, 1F, 1F);
                        ((IBaubleRender) cosmetic.getItem()).onPlayerBaubleRender(cosmetic, player, partialTicks, type);
                        GlStateManager.popMatrix();
                        continue;
                    }
                }

                if (item instanceof IBaubleRender) {
                    GlStateManager.pushMatrix();
                    GlStateManager.color(1F, 1F, 1F, 1F);
                    ((IBaubleRender) stack.getItem()).onPlayerBaubleRender(stack, player, partialTicks, type);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    private void renderManaTablet(EntityPlayer player) {
        boolean renderedOne = false;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == ModItems.manaTablet) {
                Item item = stack.getItem();
                GlStateManager.pushMatrix();
                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                IBaubleRender.Helper.rotateIfSneaking(player);
                boolean armor = player.getCurrentArmor(1) != null;
                GlStateManager.rotate(180F, 1F, 0F, 0F);
                GlStateManager.rotate(90F, 0F, 1F, 0F);
                GlStateManager.rotate(90, 1f, 0f, 0f);
                GlStateManager.translate(0.025, renderedOne ? armor ? 0.2F : 0.16F : armor ? -0.385F : -0.35F, 1f);
                GlStateManager.scale(0.75, 0.75F, 0.75F);

                GlStateManager.color(1F, 1F, 1F);
                int light = 15728880;
                int lightmapX = light % 65536;
                int lightmapY = light / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapX, lightmapY);
                Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.THIRD_PERSON);
                GlStateManager.popMatrix();

                if (renderedOne)
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
