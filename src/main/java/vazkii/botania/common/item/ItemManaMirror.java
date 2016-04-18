/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Apr 13, 2014, 5:39:24 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaTooltipDisplay;
import vazkii.botania.api.sound.BotaniaSoundEvents;
import vazkii.botania.api.wand.ICoordBoundItem;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.lib.LibItemNames;

import java.awt.*;

public class ItemManaMirror extends ItemMod implements IManaItem, ICoordBoundItem, IManaTooltipDisplay, IColorable {

	private static final String TAG_MANA = "mana";
	private static final String TAG_MANA_BACKLOG = "manaBacklog";

	private static final String TAG_POS_X = "posX";
	private static final String TAG_POS_Y = "posY";
	private static final String TAG_POS_Z = "posZ";
	private static final String TAG_DIM = "dim";

	private static final DummyPool fallbackPool = new DummyPool();

	public ItemManaMirror() {
		super(LibItemNames.MANA_MIRROR);
		setMaxStackSize(1);
		setMaxDamage(1000);
		setNoRepair();
	}

	@Override
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
		float mana = getMana(par1ItemStack);
		return par2 == 1 ? Color.HSBtoRGB(0.528F,  mana / TilePool.MAX_MANA, 1F) : 0xFFFFFF;
	}

	@Override
	public int getDamage(ItemStack stack) {
		float mana = getMana(stack);
		return 1000 - (int) (mana / TilePool.MAX_MANA * 1000);
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
		if(par2World.isRemote)
			return;

		IManaPool pool = getManaPool(par1ItemStack);
		if(!(pool instanceof DummyPool)) {
			if(pool == null)
				setMana(par1ItemStack, 0);
			else {
				pool.recieveMana(getManaBacklog(par1ItemStack));
				setManaBacklog(par1ItemStack, 0);
				setMana(par1ItemStack, pool.getCurrentMana());
			}
		}
	}

	@Override
	public EnumActionResult onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, BlockPos pos, EnumHand hand, EnumFacing side, float par8, float par9, float par10) {
		if(par2EntityPlayer.isSneaking() && !par3World.isRemote) {
			TileEntity tile = par3World.getTileEntity(pos);
			if(tile != null && tile instanceof IManaPool) {
				bindPool(par1ItemStack, tile);
				par3World.playSound(null, par2EntityPlayer.posX, par2EntityPlayer.posY, par2EntityPlayer.posZ, BotaniaSoundEvents.ding, SoundCategory.PLAYERS, 1F, 1F);
				return EnumActionResult.SUCCESS;
			}
		}

		return EnumActionResult.PASS;
	}

	/*public int getMana(ItemStack stack) {
		IManaPool pool = getManaPool(stack);
		return pool == null ? 0 : pool.getCurrentMana();
	}*/

	@Override
	public int getMana(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_MANA, 0);
	}

	public void setMana(ItemStack stack, int mana) {
		ItemNBTHelper.setInt(stack, TAG_MANA, Math.max(0, mana));
	}

	public int getManaBacklog(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_MANA_BACKLOG, 0);
	}

	public void setManaBacklog(ItemStack stack, int backlog) {
		ItemNBTHelper.setInt(stack, TAG_MANA_BACKLOG, backlog);
	}

	@Override
	public int getMaxMana(ItemStack stack) {
		return TilePool.MAX_MANA;
	}

	@Override
	public void addMana(ItemStack stack, int mana) {
		setMana(stack, getMana(stack) + mana);
		setManaBacklog(stack, getManaBacklog(stack) + mana);
	}

	/*public void addMana(ItemStack stack, int mana) {
		IManaPool pool = getManaPool(stack);
		if(pool != null) {
			pool.recieveMana(mana);
			TileEntity tile = (TileEntity) pool;
			tile.getWorld().func_147453_f(tile.xCoord, tile.yCoord, tile.zCoord, tile.getWorld().getBlock(tile.xCoord, tile.yCoord, tile.zCoord));
		}
	}*/

	public void bindPool(ItemStack stack, TileEntity pool) {
		ItemNBTHelper.setInt(stack, TAG_POS_X, pool == null ? 0 : pool.getPos().getX());
		ItemNBTHelper.setInt(stack, TAG_POS_Y, pool == null ? -1 : pool.getPos().getY());
		ItemNBTHelper.setInt(stack, TAG_POS_Z, pool == null ? 0 : pool.getPos().getZ());
		ItemNBTHelper.setInt(stack, TAG_DIM, pool == null ? 0 : pool.getWorld().provider.getDimension());
	}

	public BlockPos getPoolCoords(ItemStack stack) {
		int x = ItemNBTHelper.getInt(stack, TAG_POS_X, 0);
		int y = ItemNBTHelper.getInt(stack, TAG_POS_Y, -1);
		int z = ItemNBTHelper.getInt(stack, TAG_POS_Z, 0);
		return new BlockPos(x, y, z);
	}

	public int getDimension(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_DIM, 0);
	}

	public IManaPool getManaPool(ItemStack stack) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server == null)
			return fallbackPool;

		BlockPos coords = getPoolCoords(stack);
		if(coords.getY() == -1)
			return null;

		int dim = getDimension(stack);
		World world = null;
		for(World w : server.worldServers)
			if(w.provider.getDimension() == dim) {
				world = w;
				break;
			}

		if(world != null) {
			TileEntity tile = world.getTileEntity(coords);
			if(tile != null && tile instanceof IManaPool)
				return (IManaPool) tile;
		}

		return null;
	}

	@Override
	public boolean canReceiveManaFromPool(ItemStack stack, TileEntity pool) {
		return false;
	}

	@Override
	public boolean canReceiveManaFromItem(ItemStack stack, ItemStack otherStack) {
		return false;
	}

	@Override
	public boolean canExportManaToPool(ItemStack stack, TileEntity pool) {
		return false;
	}

	@Override
	public boolean canExportManaToItem(ItemStack stack, ItemStack otherStack) {
		return true;
	}

	private static class DummyPool implements IManaPool {

		@Override
		public boolean isFull() {
			return false;
		}

		@Override
		public void recieveMana(int mana) {
			// NO-OP
		}

		@Override
		public boolean canRecieveManaFromBursts() {
			return false;
		}

		@Override
		public int getCurrentMana() {
			return 0;
		}

		@Override
		public boolean isOutputtingPower() {
			return false;
		}

	}

	@Override
	public boolean isNoExport(ItemStack stack) {
		return false;
	}

	@Override
	public BlockPos getBinding(ItemStack stack) {
		IManaPool pool = getManaPool(stack);

		return pool == null || pool instanceof DummyPool ? null : getPoolCoords(stack);
	}

	@Override
	public float getManaFractionForDisplay(ItemStack stack) {
		return (float) getMana(stack) / (float) getMaxMana(stack);
	}

}
