/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [May 10, 2014, 5:50:01 PM (GMT)]
 */
package vazkii.botania.common.block.tile;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.ITickable;
import net.minecraft.util.AxisAlignedBB;

public class TileForestEye extends TileMod {

	public int entities = 0;

	@Override
	public void updateEntity() {
		int range = 6;
		int entityCount = worldObj.getEntitiesWithinAABB(EntityAnimal.class, new AxisAlignedBB(pos.add(-range, -range, -range), pos.add(range + 1, range + 1, range + 1))).size();
		if(entityCount != entities) {
			entities = entityCount;
			worldObj.updateComparatorOutputLevel(pos, worldObj.getBlockState(pos).getBlock());
		}
	}

}
