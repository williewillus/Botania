/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Feb 13, 2015, 10:25:32 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.common.entity.EntityCorporeaSpark;
import vazkii.botania.common.lib.LibItemNames;

import java.util.List;

public class ItemCorporeaSpark extends ItemMod {

	public ItemCorporeaSpark() {
		super(LibItemNames.CORPOREA_SPARK);
		setHasSubtypes(true);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		for(int i = 0; i < 2; i++)
			list.add(new ItemStack(item, 1, i));
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xv, float yv, float zv) {
		TileEntity tile = world.getTileEntity(pos);
		if(tile != null && (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP) || tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
			&& !CorporeaHelper.doesBlockHaveSpark(world, pos)) {
			stack.stackSize--;
			if(!world.isRemote) {
				EntityCorporeaSpark spark = new EntityCorporeaSpark(world);
				if(stack.getItemDamage() == 1)
					spark.setMaster(true);
				spark.setPosition(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
				world.spawnEntityInWorld(spark);
				world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 0);
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		return super.getUnlocalizedName(par1ItemStack) + par1ItemStack.getItemDamage();
	}

}
