/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jul 30, 2014, 1:10:34 PM (GMT)]
 */
package vazkii.botania.common.block.tile;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

public class TileEnderEye extends TileMod {

	@Override
	public void updateEntity() {
		boolean wasLooking = worldObj.getBlockState(getPos()).getValue(BotaniaStateProps.POWERED);
		int range = 80;
		List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos.add(-range, -range, -range), pos.add(range, range, range)));

		boolean looking = false;
		for(EntityPlayer player : players) {
			ItemStack helm = player.getCurrentArmor(3);
			if(helm != null && helm.getItem() == Item.getItemFromBlock(Blocks.pumpkin))
				continue;

			MovingObjectPosition pos = ToolCommons.raytraceFromEntity(worldObj, player, true, 64);
			if(pos != null && pos.getBlockPos().equals(getPos())) {
				looking = true;
				break;
			}
		}

		if(looking != wasLooking && !worldObj.isRemote)
			worldObj.setBlockState(getPos(), worldObj.getBlockState(getPos()).withProperty(BotaniaStateProps.POWERED, looking), 1 | 2);
		
		if(looking) {
			double x = getPos().getX() - 0.1 + Math.random() * 1.2;
			double y = getPos().getY() - 0.1 + Math.random() * 1.2;
			double z = getPos().getZ() - 0.1 + Math.random() * 1.2;
			
			worldObj.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 1, 0, 0);
		}
	}

}
