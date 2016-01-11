/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Feb 18, 2014, 10:15:50 PM (GMT)]
 */
package vazkii.botania.common.block.tile;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ITickable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.AlfPortalState;
import vazkii.botania.api.state.enums.PylonVariant;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.helper.Vector3;

public class TilePylon extends TileEntity implements ITickable {

	boolean activated = false;
	BlockPos centerPos;
	int ticks = 0;



	@Override
	public void update() {
		++ticks;

		if (worldObj.getBlockState(getPos()) != ModBlocks.pylon)
			return;

		PylonVariant variant = worldObj.getBlockState(getPos()).getValue(BotaniaStateProps.PYLON_VARIANT);

		if(activated && worldObj.isRemote) {
			if(worldObj.getBlockState(centerPos).getBlock() != getBlockForMeta() || variant != PylonVariant.MANA && portalOff()) { // todo 1.8 clarify
				activated = false;
				return;
			}

			Vector3 centerBlock = new Vector3(centerPos.getX() + 0.5, centerPos.getY() + 0.75 + (Math.random() - 0.5 * 0.25), centerPos.getZ() + 0.5);

			if(variant == PylonVariant.NATURA) {
				if(ConfigHandler.elfPortalParticlesEnabled) {
					double worldTime = ticks;
					worldTime += new Random(pos.hashCode()).nextInt(1000);
					worldTime /= 5;

					float r = 0.75F + (float) Math.random() * 0.05F;
					double x = pos.getX() + 0.5 + Math.cos(worldTime) * r;
					double z = pos.getZ() + 0.5 + Math.sin(worldTime) * r;

					Vector3 ourCoords = new Vector3(x, pos.getY() + 0.25, z);
					centerBlock.sub(new Vector3(0, 0.5, 0));
					Vector3 movementVector = centerBlock.sub(ourCoords).normalize().multiply(0.2);

					Botania.proxy.wispFX(worldObj, x, pos.getY() + 0.25, z, (float) Math.random() * 0.25F, 0.75F + (float) Math.random() * 0.25F, (float) Math.random() * 0.25F, 0.25F + (float) Math.random() * 0.1F, -0.075F - (float) Math.random() * 0.015F);
					if(worldObj.rand.nextInt(3) == 0)
						Botania.proxy.wispFX(worldObj, x, pos.getY() + 0.25, z, (float) Math.random() * 0.25F, 0.75F + (float) Math.random() * 0.25F, (float) Math.random() * 0.25F, 0.25F + (float) Math.random() * 0.1F, (float) movementVector.x, (float) movementVector.y, (float) movementVector.z);
				}
			} else {
				Vector3 ourCoords = Vector3.fromTileEntityCenter(this).add(0, 1 + (Math.random() - 0.5 * 0.25), 0);
				Vector3 movementVector = centerBlock.sub(ourCoords).normalize().multiply(0.2);

				Block block = worldObj.getBlockState(pos.down()).getBlock();
				if(block == ModBlocks.flower || block == ModBlocks.shinyFlower) {
					int hex = worldObj.getBlockState(pos.down()).getValue(BotaniaStateProps.COLOR).getMapColor().colorValue;
					int r = (hex & 0xFF0000) >> 16;
					int g = (hex & 0xFF00) >> 8;
					int b = (hex & 0xFF);

					if(worldObj.rand.nextInt(4) == 0)
						Botania.proxy.sparkleFX(worldObj, centerBlock.x + (Math.random() - 0.5) * 0.5, centerBlock.y, centerBlock.z + (Math.random() - 0.5) * 0.5, r / 255F, g / 255F, b / 255F, (float) Math.random(), 8);

					Botania.proxy.wispFX(worldObj, pos.getX() + 0.5 + (Math.random() - 0.5) * 0.25, pos.getY() - 0.5, pos.getZ() + 0.5 + (Math.random() - 0.5) * 0.25, r / 255F, g / 255F, b / 255F, (float) Math.random() / 3F, -0.04F);
					Botania.proxy.wispFX(worldObj, pos.getX() + 0.5 + (Math.random() - 0.5) * 0.125, pos.getY() + 1.5, pos.getZ() + 0.5 + (Math.random() - 0.5) * 0.125, r / 255F, g / 255F, b / 255F, (float) Math.random() / 5F, -0.001F);
					Botania.proxy.wispFX(worldObj, pos.getX() + 0.5 + (Math.random() - 0.5) * 0.25, pos.getY() + 1.5, pos.getZ() + 0.5 + (Math.random() - 0.5) * 0.25, r / 255F, g / 255F, b / 255F, (float) Math.random() / 8F, (float) movementVector.x, (float) movementVector.y, (float) movementVector.z);
				}
			}
		}

		if(worldObj.rand.nextBoolean() && worldObj.isRemote)
			Botania.proxy.sparkleFX(worldObj, pos.getX() + Math.random(), pos.getY() + Math.random() * 1.5, pos.getZ() + Math.random(), variant == PylonVariant.GAIA ? 1F : 0.5F, variant == PylonVariant.NATURA ? 1F : 0.5F, variant == PylonVariant.NATURA ? 0.5F : 1F, (float) Math.random(), 2);
	}

	private Block getBlockForMeta() {
		return worldObj.getBlockState(pos).getValue(BotaniaStateProps.PYLON_VARIANT) == PylonVariant.MANA ? ModBlocks.enchanter : ModBlocks.alfPortal;
	}

	private boolean portalOff() {
		return worldObj.getBlockState(centerPos).getBlock() != ModBlocks.alfPortal
				|| worldObj.getBlockState(centerPos).getValue(BotaniaStateProps.ALFPORTAL_STATE) == AlfPortalState.OFF;
	}

}