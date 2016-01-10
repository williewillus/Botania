/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 24, 2015, 3:03:18 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;

import vazkii.botania.api.item.IBurstViewerBauble;
import vazkii.botania.api.item.ICosmeticAttachable;
import vazkii.botania.api.item.ICosmeticBauble;
import vazkii.botania.common.lib.LibItemNames;
import baubles.api.BaubleType;
import baubles.common.lib.PlayerHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMonocle extends ItemBauble implements IBurstViewerBauble, ICosmeticBauble {

	public ItemMonocle() {
		super(LibItemNames.MONOCLE);
	}

	@Override
	public BaubleType getBaubleType(ItemStack arg0) {
		return BaubleType.AMULET;
	}

	@Override
	public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, float partialTicks, RenderType type) {
		if(type == RenderType.HEAD) {
			boolean armor = player.getCurrentArmor(3) != null;
			Helper.translateToHeadLevel(player);
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			GlStateManager.rotate(90F, 0F, 1F, 0F);
			GlStateManager.rotate(180F, 1F, 0F, 0F);
			GlStateManager.translate(-0.15F, -1.425F, armor ? -0.3F : -0.25F);
			GlStateManager.scale(-0.35F, 0.35F, 0.35F);
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void renderHUD(ScaledResolution resolution, EntityPlayer player) {
		Minecraft mc = Minecraft.getMinecraft();
		MovingObjectPosition pos = mc.objectMouseOver;
		if(pos == null)
			return;
		Block block = player.worldObj.getBlockState(pos.getBlockPos()).getBlock();
		int meta = block.getMetaFromState(player.worldObj.getBlockState(pos.getBlockPos())); // todo 1.8 use states
		player.worldObj.getTileEntity(pos.getBlockPos());

		ItemStack dispStack = null;
		String text = "";

		if(block == Blocks.redstone_wire) {
			dispStack = new ItemStack(Items.redstone);
			text = EnumChatFormatting.RED + "" + meta;
		} else if(block == Blocks.unpowered_repeater || block == Blocks.powered_repeater) {
			dispStack = new ItemStack(Items.repeater);
			text = "" + (((meta & 12) >> 2) + 1);
		} else if(block == Blocks.unpowered_comparator || block == Blocks.powered_comparator) {
			dispStack = new ItemStack(Items.comparator);
			text = (meta & 4) == 4 ? "-" : "+";
		}

		if(dispStack == null)
			return;

		int x = resolution.getScaledWidth() / 2 + 15;
		int y = resolution.getScaledHeight() / 2 - 8;

		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		mc.getRenderItem().renderItemAndEffectIntoGUI(dispStack, x, y);
		net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();

		mc.fontRendererObj.drawStringWithShadow(text, x + 20, y + 4, 0xFFFFFF);
	}

	public static boolean hasMonocle(EntityPlayer player) {
		for(int i = 0; i < 4; i++) {
			ItemStack stack = PlayerHandler.getPlayerBaubles(player).getStackInSlot(i);
			if(stack != null) {
				Item item = stack.getItem();
				if(item instanceof IBurstViewerBauble)
					return true;

				if(item instanceof ICosmeticAttachable) {
					ICosmeticAttachable attach = (ICosmeticAttachable) item;
					ItemStack cosmetic = attach.getCosmeticItem(stack);
					if(cosmetic != null && cosmetic.getItem() instanceof IBurstViewerBauble)
						return true;
				}
			}
		}

		return false;
	}

}
