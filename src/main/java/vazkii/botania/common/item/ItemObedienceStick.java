/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jul 20, 2015, 7:26:14 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.subtile.ISubTileContainer;
import vazkii.botania.api.subtile.SubTileEntity;
import vazkii.botania.api.subtile.SubTileFunctional;
import vazkii.botania.api.subtile.SubTileGenerating;
import vazkii.botania.common.core.helper.MathHelper;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.lib.LibItemNames;

public class ItemObedienceStick extends ItemMod {

	public ItemObedienceStick() {
		super(LibItemNames.OBEDIENCE_STICK);
		setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xs, float ys, float zs) {
		TileEntity tileAt = world.getTileEntity(pos);
		if(tileAt != null && (tileAt instanceof IManaPool || tileAt instanceof IManaCollector)) {
			boolean pool = tileAt instanceof IManaPool;
			Actuator act = pool ? Actuator.functionalActuator : Actuator.generatingActuator;
			int range = pool ? SubTileFunctional.RANGE : SubTileGenerating.RANGE;

			for(int i = -range; i < range + 1; i++)
				for(int j = -range; j < range + 1; j++)
					for(int k = -range; k < range + 1; k++) {
						BlockPos pos_ = pos.add(i, j, k);
						if(MathHelper.pointDistanceSpace(pos_, pos) > range)
							continue;

						TileEntity tile = world.getTileEntity(pos_);
						if(tile instanceof ISubTileContainer) {
							SubTileEntity subtile = ((ISubTileContainer) tile).getSubTile();
							if(act.actuate(subtile, tileAt)) {
								Vector3 orig = new Vector3(pos_.getX() + 0.5, pos_.getY() + 0.5, pos_.getZ() + 0.5);
								Vector3 end = new Vector3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
								ItemTwigWand.doParticleBeam(world, orig, end);
							}
						}
					}

			if(player.worldObj.isRemote)
				player.swingArm(hand);
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}

	public static abstract class Actuator {
		public static final Actuator generatingActuator = new Actuator() {

			@Override
			public boolean actuate(SubTileEntity flower, TileEntity tile) {
				if(flower instanceof SubTileGenerating) {
					((SubTileGenerating) flower).linkToForcefully(tile);
					return true;
				}
				return false;
			}

		};

		public static final Actuator functionalActuator = new Actuator() {

			@Override
			public boolean actuate(SubTileEntity flower, TileEntity tile) {
				if(flower instanceof SubTileFunctional) {
					((SubTileFunctional) flower).linkToForcefully(tile);
					return true;
				}
				return false;
			}

		};

		public abstract boolean actuate(SubTileEntity flower, TileEntity tile);

	}

}
