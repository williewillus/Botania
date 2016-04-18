/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 24, 2015, 4:44:01 PM (GMT)]
 */
package vazkii.botania.common.item.lens;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.common.Botania;

import java.util.ArrayList;
import java.util.List;

public class LensPaint extends Lens {

	@Override
	public boolean collideBurst(IManaBurst burst, EntityThrowable entity, RayTraceResult pos, boolean isManaBlock, boolean dead, ItemStack stack) {
		int storedColor = ItemLens.getStoredColor(stack);
		if(!entity.worldObj.isRemote && !burst.isFake() && storedColor > -1 && storedColor < 17) {
			if(pos.entityHit != null && pos.entityHit instanceof EntitySheep) {
				int r = 20;
				EnumDyeColor sheepColor = ((EntitySheep) pos.entityHit).getFleeceColor();
				List<EntitySheep> sheepList = entity.worldObj.getEntitiesWithinAABB(EntitySheep.class, new AxisAlignedBB(pos.entityHit.posX - r, pos.entityHit.posY - r, pos.entityHit.posZ - r, pos.entityHit.posX + r, pos.entityHit.posY + r, pos.entityHit.posZ + r));
				for(EntitySheep sheep : sheepList) {
					if(sheep.getFleeceColor() == sheepColor)
						sheep.setFleeceColor(EnumDyeColor.byMetadata(storedColor == 16 ? sheep.worldObj.rand.nextInt(16) : storedColor));
				}
				dead = true;
			} else if (pos.getBlockPos() != null) {
				Block block = entity.worldObj.getBlockState(pos.getBlockPos()).getBlock();
				if(BotaniaAPI.paintableBlocks.containsKey(block)) {
					IBlockState state = entity.worldObj.getBlockState(pos.getBlockPos());
					List<BlockPos> coordsToPaint = new ArrayList<>();
					List<BlockPos> coordsFound = new ArrayList<>();

					BlockPos theseCoords = pos.getBlockPos();
					coordsFound.add(theseCoords);

					do {
						List<BlockPos> iterCoords = new ArrayList<>(coordsFound);
						for(BlockPos coords : iterCoords) {
							coordsFound.remove(coords);
							coordsToPaint.add(coords);

							for(EnumFacing dir : EnumFacing.VALUES) {
								IBlockState state_ = entity.worldObj.getBlockState(coords.offset(dir));
								BlockPos coords_ = new BlockPos(coords.offset(dir));
								if(state_ == state && !coordsFound.contains(coords_) && !coordsToPaint.contains(coords_))
									coordsFound.add(coords_);
							}
						}
					} while(!coordsFound.isEmpty() && coordsToPaint.size() < 1000);

					for(BlockPos coords : coordsToPaint) {
						EnumDyeColor placeColor = EnumDyeColor.byMetadata(storedColor == 16 ? entity.worldObj.rand.nextInt(16) : storedColor);
						IBlockState stateThere = entity.worldObj.getBlockState(coords);

						if(stateThere.getValue(BotaniaAPI.paintableBlocks.get(block)) != placeColor
								&& BotaniaAPI.paintableBlocks.get(block).getAllowedValues().contains(placeColor)) {
							entity.worldObj.setBlockState(coords, stateThere.withProperty(BotaniaAPI.paintableBlocks.get(block), placeColor), 2);
							int hex = placeColor.getMapColor().colorValue;
							int r = (hex & 0xFF0000) >> 16;
							int g = (hex & 0xFF00) >> 8;
							int b = (hex & 0xFF);
							for(int i = 0; i < 4; i++)
								Botania.proxy.sparkleFX(entity.worldObj, coords.getX() + (float) Math.random(), coords.getY() + (float) Math.random(), coords.getZ() + (float) Math.random(), r / 255F, g / 255F, b / 255F, 0.6F + (float) Math.random() * 0.3F, 5); // todo 1.8 noop right now due to serversiding

						}
					}
				}
			}
		}

		return dead;
	}

}
