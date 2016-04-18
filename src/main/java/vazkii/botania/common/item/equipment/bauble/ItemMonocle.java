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

import baubles.api.BaubleType;
import baubles.common.lib.PlayerHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.item.IBurstViewerBauble;
import vazkii.botania.api.item.ICosmeticAttachable;
import vazkii.botania.api.item.ICosmeticBauble;
import vazkii.botania.common.lib.LibItemNames;

public class ItemMonocle extends ItemBauble implements IBurstViewerBauble, ICosmeticBauble {

	public ItemMonocle() {
		super(LibItemNames.MONOCLE);
	}

	@Override
	public BaubleType getBaubleType(ItemStack arg0) {
		return BaubleType.AMULET;
	}

	@Override
	public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, RenderType type, float partialTicks) {
		if(type == RenderType.HEAD) {
			boolean armor = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null;
			Helper.translateToHeadLevel(player);
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.rotate(90, 0, 0, 1); // todo 1.8 could use some further refinement
			GlStateManager.translate(2.88F, armor ? -0.75F : -0.63F, 0.62F);
			Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(this), ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND); // todo 1.9
		}
	}

	@SideOnly(Side.CLIENT)
	public static void renderHUD(ScaledResolution resolution, EntityPlayer player) {
		Minecraft mc = Minecraft.getMinecraft();
		RayTraceResult pos = mc.objectMouseOver;
		if(pos == null || pos.getBlockPos() == null)
			return;
		IBlockState state = player.worldObj.getBlockState(pos.getBlockPos());
		Block block = state.getBlock();
		player.worldObj.getTileEntity(pos.getBlockPos());

		ItemStack dispStack = null;
		String text = "";

		if(block == Blocks.redstone_wire) {
			dispStack = new ItemStack(Items.redstone);
			text = TextFormatting.RED + "" + state.getValue(BlockRedstoneWire.POWER);
		} else if(block == Blocks.unpowered_repeater || block == Blocks.powered_repeater) {
			dispStack = new ItemStack(Items.repeater);
			text = "" + state.getValue(BlockRedstoneRepeater.DELAY);
		} else if(block == Blocks.unpowered_comparator || block == Blocks.powered_comparator) {
			dispStack = new ItemStack(Items.comparator);
			text = state.getValue(BlockRedstoneComparator.MODE) == BlockRedstoneComparator.Mode.SUBTRACT ? "-" : "+";
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
