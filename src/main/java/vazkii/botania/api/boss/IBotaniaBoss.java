/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Oct 29, 2014, 6:09:48 PM (GMT)]
 */
package vazkii.botania.api.boss;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.UUID;

/**
 * A Botania boss, that is subject to special rendering.
 */
public interface IBotaniaBoss {

	/**
	 * The ResourceLocation to bind for this boss's boss bar.
	 * You can use BotaniaAPI.internalMethodHandler.getDefaultBossBarTexture() to get
	 * the one used by botania bosses.
	 */
	public ResourceLocation getBossBarTexture();

	/**
	 * A Rectangle instance delimiting the uv, width and height of this boss's
	 * boss bar texture. This is for the background, not the bar that shows
	 * the HP.
	 */
	public Rectangle getBossBarTextureRect();

	/**
	 * A Rectangle instance delimiting the uv, width and height of this boss's
	 * boss bar HP texture. This is for the foreground that shows how much
	 * HP the boss has. The width of the rectangle will be multiplied by the
	 * faction of the boss's current HP by max HP.
	 */
	public Rectangle getBossBarHPTextureRect();

	/**
	 * A callback for when this boss's boss bar renders, you can do additional rendering
	 * here if needed.
	 * @return How tall your auxiliary renders were
	 */
	@SideOnly(Side.CLIENT)
	public int bossBarRenderCallback(ScaledResolution res, int x, int y);

	/**
	 * Get the serverside UUID of the {@link net.minecraft.world.BossInfoServer} instance tracking this boss
	 * Note that this is NOT the entity's UUID, nor is it the clientside UUID of the BossInfoServer instance!
	 * You will most likely need to sync this yourself using the datawatcher
	 * @return The uuid.
     */
	public UUID getBossInfoUuid();

}
