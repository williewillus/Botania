/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jul 31, 2014, 6:09:17 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.common.lib.LibItemNames;
import vazkii.botania.common.lib.LibObfuscation;
import baubles.api.BaubleType;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSuperLavaPendant extends ItemBauble implements IBaubleRender {

	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite gemIcon;

	public ItemSuperLavaPendant() {
		super(LibItemNames.SUPER_LAVA_PENDANT);
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		setImmunity(player, true);
	}

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase player) {
		setImmunity(player, false);
	}

	private void setImmunity(Entity entity, boolean immune) {
		ReflectionHelper.setPrivateValue(Entity.class, entity, immune, LibObfuscation.IS_IMMUNE_TO_FIRE);
	}

	@Override
	public BaubleType getBaubleType(ItemStack itemstack) {
		return BaubleType.AMULET;
	}

	@Override
	public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, float partialTicks, RenderType type) {
		if(type == RenderType.BODY) {
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			Helper.rotateIfSneaking(player);
			boolean armor = player.getCurrentArmor(2) != null;
			GlStateManager.rotate(180F, 1F, 0F, 0F);
			GlStateManager.translate(-0.36F, -0.24F, armor ? 0.2F : 0.15F);
			GlStateManager.rotate(-45F, 0F, 0F, 1F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);

			float f = gemIcon.getMinU();
			float f1 = gemIcon.getMaxU();
			float f2 = gemIcon.getMinV();
			float f3 = gemIcon.getMaxV();
			IconHelper.renderIconIn3D(Tessellator.getInstance(), f1, f2, f, f3, gemIcon.getIconWidth(), gemIcon.getIconHeight(), 1F / 32F);
		}
	}
}
