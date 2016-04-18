/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jun 20, 2014, 7:09:51 PM (GMT)]
 */
package vazkii.botania.common.item.rod;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import vazkii.botania.api.item.IAvatarTile;
import vazkii.botania.api.item.IAvatarWieldable;
import vazkii.botania.api.item.IManaProficiencyArmor;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.api.sound.BotaniaSoundEvents;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.TileBifrost;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.item.ItemMod;
import vazkii.botania.common.lib.LibItemNames;

import java.util.List;

public class ItemRainbowRod extends ItemMod implements IManaUsingItem, IAvatarWieldable {

	private static final ResourceLocation avatarOverlay = new ResourceLocation(LibResources.MODEL_AVATAR_RAINBOW);

	private static final int MANA_COST = 750;
	private static final int MANA_COST_AVATAR = 10;
	private static final int TIME = 600;

	public ItemRainbowRod() {
		super(LibItemNames.RAINBOW_ROD);
		setMaxDamage(TIME);
		setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, EnumHand hand) {
		if(!par2World.isRemote && par1ItemStack.getItemDamage() == 0 && ManaItemHandler.requestManaExactForTool(par1ItemStack, par3EntityPlayer, MANA_COST, false)) {
			Block place = ModBlocks.bifrost;
			Vector3 vector = new Vector3(par3EntityPlayer.getLookVec()).normalize();

			double x = par3EntityPlayer.posX;
			double y = par3EntityPlayer.posY;
			double z = par3EntityPlayer.posZ;

			double lx = 0;
			double ly = -1;
			double lz = 0;

			int count = 0;
			boolean prof = IManaProficiencyArmor.Helper.hasProficiency(par3EntityPlayer);
			int maxlen = prof ? 160 : 100;
			int time = prof ? (int) (TIME * 1.6) : TIME;

			BlockPos playerPos = new BlockPos((int) x, (int) y, (int) z);
			while(count < maxlen && (int) lx == (int) x && (int) ly == (int) y && (int) lz == (int) z || count < 4 || par2World.getBlockState(playerPos).getBlock().isAir(par2World.getBlockState(playerPos), par2World, playerPos) || par2World.getBlockState(playerPos).getBlock() == place) {
				if(y >= 256 || y <= 0)
					break;

				for(int i = -2; i < 1; i++)
					for(int j = -2; j < 1; j++) {
						BlockPos pos_ = new BlockPos((int) x + i, (int) y, (int) z + j);
						if(par2World.getBlockState(pos_).getBlock().isAir(par2World.getBlockState(pos_), par2World, pos_) || par2World.getBlockState(pos_).getBlock() == place) {
							par2World.setBlockState(pos_, place.getDefaultState());
							TileBifrost tile = (TileBifrost) par2World.getTileEntity(pos_);
							if(tile != null) {
								for(int k = 0; k < 4; k++)
									Botania.proxy.sparkleFX(par2World, tile.getPos().getX() + Math.random(), tile.getPos().getY() + Math.random(), tile.getPos().getZ() + Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.45F + 0.2F * (float) Math.random(), 6);
								tile.ticks = time;
							}
						}

					}

				lx = x;
				ly = y;
				lz = z;

				x += vector.x;
				y += vector.y;
				z += vector.z;
				count++;
			}

			if(count > 0) {
				par2World.playSound(null, par3EntityPlayer.posX, par3EntityPlayer.posY, par3EntityPlayer.posZ, BotaniaSoundEvents.bifrostRod, SoundCategory.PLAYERS, 0.5F, 0.25F);
				ManaItemHandler.requestManaExactForTool(par1ItemStack, par3EntityPlayer, MANA_COST, false);
				par1ItemStack.setItemDamage(TIME);
			}
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, par1ItemStack);
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack) {
		return itemStack.copy();
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return getContainerItem(stack) != null;
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
		if(par1ItemStack.isItemDamaged())
			par1ItemStack.setItemDamage(par1ItemStack.getItemDamage() - 1);
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

	@Override
	public void onAvatarUpdate(IAvatarTile tile, ItemStack stack) {
		TileEntity te = (TileEntity) tile;
		World world = te.getWorld();

		if(world.isRemote || tile.getCurrentMana() < MANA_COST_AVATAR * 25 || !tile.isEnabled())
			return;

		BlockPos tePos = te.getPos();
		int w = 1;
		int h = 1;
		int l = 20;

		AxisAlignedBB axis = null;
		switch(world.getBlockState(tePos).getValue(BotaniaStateProps.CARDINALS)) {
		case NORTH :
			axis = new AxisAlignedBB(tePos.add(-w, -h, -l), tePos.add(w + 1, h, 0));
			break;
		case SOUTH :
			axis = new AxisAlignedBB(tePos.add(-w, -h, 1), tePos.add(w + 1, h, l + 1));
			break;
		case WEST :
			axis = new AxisAlignedBB(tePos.add(-l, -h, -w), tePos.add(0, h, w + 1));
			break;
		case EAST :
			axis = new AxisAlignedBB(tePos.add(1, -h, -w), tePos.add(l + 1, h, w + 1));
		}

		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, axis);
		for(EntityPlayer p : players) {
			int px = MathHelper.floor_double(p.posX);
			int py = MathHelper.floor_double(p.posY) - 1;
			int pz = MathHelper.floor_double(p.posZ);
			int dist = 5;
			int diff = dist / 2;

			for(int i = 0; i < dist; i++)
				for(int j = 0; j < dist; j++) {
					int ex = px + i - diff;
					int ez = pz + j - diff;

					if(!axis.isVecInside(new Vec3d(ex + 0.5, py + 1, ez + 0.5)))
						continue;
					BlockPos pos = new BlockPos(ex, py, ez);
					Block block = world.getBlockState(pos).getBlock();
					if(block.isAir(world.getBlockState(pos), world, pos)) {
						world.setBlockState(pos, ModBlocks.bifrost.getDefaultState());
						TileBifrost tileBifrost = (TileBifrost) world.getTileEntity(pos);
						tileBifrost.ticks = 10;
						tile.recieveMana(-MANA_COST_AVATAR);
					} else if(block == ModBlocks.bifrost) {
						TileBifrost tileBifrost = (TileBifrost) world.getTileEntity(pos);
						if(tileBifrost.ticks < 2) {
							tileBifrost.ticks = 10;
							tile.recieveMana(-MANA_COST_AVATAR);
						}
					}
				}
		}


	}

	@Override
	public ResourceLocation getOverlayResource(IAvatarTile tile, ItemStack stack) {
		return avatarOverlay;
	}

}
