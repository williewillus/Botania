/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 17, 2015, 6:48:29 PM (GMT)]
 */
package vazkii.botania.common.item;

import com.mojang.authlib.GameProfile;
import mods.railcraft.api.core.items.IMinecartItem;
import net.minecraft.block.BlockRailBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import vazkii.botania.common.achievement.ICraftAchievement;
import vazkii.botania.common.achievement.ModAchievements;
import vazkii.botania.common.entity.EntityPoolMinecart;
import vazkii.botania.common.lib.LibItemNames;

@Optional.Interface(modid = "Railcraft", iface = "mods.railcraft.api.core.items.IMinecartItem", striprefs = true)
public class ItemPoolMinecart extends ItemMod implements ICraftAchievement, IMinecartItem {

	public ItemPoolMinecart() {
		super(LibItemNames.POOL_MINECART);
		setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World p_77648_3_, BlockPos pos, EnumHand hand, EnumFacing side, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
		if(BlockRailBase.isRailBlock(p_77648_3_.getBlockState(pos))) {
			if(!p_77648_3_.isRemote) {
				EntityMinecart entityminecart = new EntityPoolMinecart(p_77648_3_, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

				if(p_77648_1_.hasDisplayName())
					entityminecart.setCustomNameTag(p_77648_1_.getDisplayName());

				p_77648_3_.spawnEntityInWorld(entityminecart);
			}

			--p_77648_1_.stackSize;
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}

	@Override
	public Achievement getAchievementOnCraft(ItemStack stack, EntityPlayer player, IInventory matrix) {
		return ModAchievements.manaCartCraft;
	}

	@Override
	public boolean canBePlacedByNonPlayer(ItemStack cart) {
		return true;
	}

	@Override
	public EntityMinecart placeCart(GameProfile owner, ItemStack cart, World world, int i, int j, int k) {
		if(BlockRailBase.isRailBlock(world.getBlockState(new BlockPos(i, j, k)))) {
			if(!world.isRemote) {
				EntityMinecart entityminecart = new EntityPoolMinecart(world, i + 0.5,j + 0.5, k + 0.5);

				if(cart.hasDisplayName())
					entityminecart.setCustomNameTag(cart.getDisplayName());

				if(world.spawnEntityInWorld(entityminecart))
					return entityminecart;
			}
		}
		return null;
	}

}
