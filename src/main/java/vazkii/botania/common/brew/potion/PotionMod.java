/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Nov 2, 2014, 10:12:45 PM (GMT)]
 */
package vazkii.botania.common.brew.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.lib.LibMisc;

public class PotionMod extends Potion {

	private static final ResourceLocation resource = new ResourceLocation(LibResources.GUI_POTIONS);

	public PotionMod(String name, boolean badEffect, int color, int iconIndex) {
		super(badEffect, color);
		GameRegistry.register(this, new ResourceLocation(LibMisc.MOD_ID, name));
		setPotionName("botania.potion." + name);
		setIconIndex(iconIndex % 8, iconIndex / 8);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getStatusIconIndex() {
		Minecraft.getMinecraft().renderEngine.bindTexture(resource);

		return super.getStatusIconIndex();
	}

	public boolean hasEffect(EntityLivingBase entity) {
		return hasEffect(entity, this);
	}

	public boolean hasEffect(EntityLivingBase entity, Potion potion) {
		return entity.getActivePotionEffect(potion) != null;
	}

}
