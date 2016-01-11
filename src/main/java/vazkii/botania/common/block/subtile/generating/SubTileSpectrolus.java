/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 30, 2015, 1:37:26 PM (GMT)]
 */
package vazkii.botania.common.block.subtile.generating;

import java.awt.Color;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.GL11;

import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.SubTileGenerating;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibObfuscation;

public class SubTileSpectrolus extends SubTileGenerating {

	private static final String TAG_NEXT_COLOR = "nextColor";

	private static final int RANGE = 1;

	int nextColor;

	@Override
	public void onUpdate() {
		super.onUpdate();

		boolean remote = supertile.getWorld().isRemote;
		Item wool = Item.getItemFromBlock(Blocks.wool);

		List<EntityItem> items = supertile.getWorld().getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(supertile.getPos().add(-RANGE, -RANGE, -RANGE), supertile.getPos().add(RANGE + 1, RANGE + 1, RANGE + 1)));
		int slowdown = getSlowdownFactor();

		for(EntityItem item : items) {
			ItemStack stack = item.getEntityItem();
			if(stack != null && stack.getItem() == wool && !item.isDead && ((Integer) ObfuscationReflectionHelper.getPrivateValue(EntityItem.class, item, LibObfuscation.AGE)) >= slowdown) {
				int meta = stack.getItemDamage();
				if(meta == nextColor) {
					if(!remote) {
						mana = Math.min(getMaxMana(), mana + 300);
						nextColor = nextColor == 15 ? 0 : nextColor + 1;
						sync();
					}
					
					for(int i = 0; i < 10; i++) {
						float m = 0.2F;
						float mx = (float) (Math.random() - 0.5) * m;
						float my = (float) (Math.random() - 0.5) * m;
						float mz = (float) (Math.random() - 0.5) * m;
						// todo 1.8.8 verify. old: supertile.getWorld().spawnParticle("blockcrack_" + Item.getIdFromItem(stack.getItem()) + "_" + meta, item.posX, item.posY, item.posZ, mx, my, mz);
						supertile.getWorld().spawnParticle(EnumParticleTypes.ITEM_CRACK, item.posX, item.posY, item.posZ, mx, my, mz, Item.getIdFromItem(stack.getItem()), stack.getItemDamage());
					}
				}
				
				if(!remote)
					item.setDead();
			}
		}
	}

	@Override
	public RadiusDescriptor getRadius() {
		return new RadiusDescriptor.Square(toBlockPos(), RANGE);
	}

	@Override
	public int getMaxMana() {
		return 8000;
	}

	@Override
	public int getColor() {
		return Color.HSBtoRGB(ticksExisted / 100F, 1F, 1F);
	}

	@Override
	public LexiconEntry getEntry() {
		return LexiconData.spectrolus;
	}

	@Override
	public void renderHUD(Minecraft mc, ScaledResolution res) {
		super.renderHUD(mc, res);

		ItemStack stack = new ItemStack(Blocks.wool, 1, nextColor);
		int color = getColor();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		if(stack != null && stack.getItem() != null) {
			String stackName = stack.getDisplayName();
			int width = 16 + mc.fontRendererObj.getStringWidth(stackName) / 2;
			int x = res.getScaledWidth() / 2 - width;
			int y = res.getScaledHeight() / 2 + 30;

			mc.fontRendererObj.drawStringWithShadow(stackName, x + 20, y + 5, color);
			RenderHelper.enableGUIStandardItemLighting();
			mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
			RenderHelper.disableStandardItemLighting();
		}

		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
	}

	@Override
	public void writeToPacketNBT(NBTTagCompound cmp) {
		super.writeToPacketNBT(cmp);
		cmp.setInteger(TAG_NEXT_COLOR, nextColor);
	}

	@Override
	public void readFromPacketNBT(NBTTagCompound cmp) {
		super.readFromPacketNBT(cmp);
		nextColor = cmp.getInteger(TAG_NEXT_COLOR);
	}

}
