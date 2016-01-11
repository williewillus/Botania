/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 10, 2014, 7:55:12 PM (GMT)]
 */
package vazkii.botania.common.block.tile.mana;

import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.util.ITickable;
import net.minecraft.util.AxisAlignedBB;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.mana.IManaCollisionGhost;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.TileMod;

public class TileManaDetector extends TileMod implements IManaCollisionGhost {

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			boolean state = worldObj.getBlockState(getPos()).getValue(BotaniaStateProps.POWERED);
			boolean expectedState = worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)), Predicates.instanceOf(IManaBurst.class)).size() != 0;
			if(state != expectedState)
				worldObj.setBlockState(getPos(), worldObj.getBlockState(getPos()).withProperty(BotaniaStateProps.POWERED, expectedState), 1 | 2);

			if(expectedState)
				for(int i = 0; i < 4; i++)
					Botania.proxy.sparkleFX(getWorld(), pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 1F, 0.2F, 0.2F, 0.7F + 0.5F * (float) Math.random(), 5);
		}
	}

	@Override
	public boolean isGhost() {
		return true;
	}

}
