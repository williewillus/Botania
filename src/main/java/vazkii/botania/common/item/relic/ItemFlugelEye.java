/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 29, 2015, 10:13:26 PM (GMT)]
 */
package vazkii.botania.common.item.relic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.api.wand.ICoordBoundItem;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.core.helper.MathHelper;
import vazkii.botania.common.lib.LibItemNames;

public class ItemFlugelEye extends ItemRelic implements ICoordBoundItem, IManaUsingItem {

	public ItemFlugelEye() {
		super(LibItemNames.FLUGEL_EYE);
	}

	private static final String TAG_X = "x";
	private static final String TAG_Y = "y";
	private static final String TAG_Z = "z";
	private static final String TAG_DIMENSION = "dim";

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(player.isSneaking()) {
			if(world.isRemote) {
				player.swingArm(hand);
				for(int i = 0; i < 10; i++) {
					float x1 = (float) (pos.getX() + Math.random());
					float y1 = pos.getY() + 1;
					float z1 = (float) (pos.getZ() + Math.random());
					Botania.proxy.wispFX(player.worldObj, x1, y1, z1, (float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random() * 0.5F, -0.05F + (float) Math.random() * 0.05F);
				}
			} else {
				ItemNBTHelper.setInt(stack, TAG_X, pos.getX());
				ItemNBTHelper.setInt(stack, TAG_Y, pos.getY());
				ItemNBTHelper.setInt(stack, TAG_Z, pos.getZ());
				ItemNBTHelper.setInt(stack, TAG_DIMENSION, world.provider.getDimension());
				world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.entity_endermen_teleport, SoundCategory.PLAYERS, 1F, 5F);
			}
		}

		return EnumActionResult.SUCCESS;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
		float x = (float) (living.posX - Math.random() * living.width);
		float y = (float) (living.posY - 1.6 + Math.random());
		float z = (float) (living.posZ - Math.random() * living.width);
		Botania.proxy.wispFX(living.worldObj, x, y, z, (float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random() * 0.7F, -0.05F - (float) Math.random() * 0.05F);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, EnumHand hand) {
		par3EntityPlayer.setActiveHand(hand);
		return ActionResult.newResult(EnumActionResult.SUCCESS, par1ItemStack);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase living) {
		if(!(living instanceof EntityPlayer))
			return stack;

		EntityPlayer player = ((EntityPlayer) living);
		int x = ItemNBTHelper.getInt(stack, TAG_X, 0);
		int y = ItemNBTHelper.getInt(stack, TAG_Y, -1);
		int z = ItemNBTHelper.getInt(stack, TAG_Z, 0);
		int dim = ItemNBTHelper.getInt(stack, TAG_DIMENSION, 0);

		int cost = (int) (MathHelper.pointDistanceSpace(x + 0.5, y + 0.5, z + 0.5, player.posX, player.posY, player.posZ) * 10);

		if(y > -1 && dim == world.provider.getDimension() && ManaItemHandler.requestManaExact(stack, player, cost, true)) {
			moveParticlesAndSound(player);
			if(player instanceof EntityPlayerMP && !world.isRemote)
				((EntityPlayerMP) player).playerNetServerHandler.setPlayerLocation(x + 0.5, y + 1.6, z + 0.5, player.rotationYaw, player.rotationPitch);
			moveParticlesAndSound(player);
		}

		return stack;
	}

	private static void moveParticlesAndSound(Entity entity) {
		for(int i = 0; i < 15; i++) {
			float x = (float) (entity.posX + Math.random());
			float y = (float) (entity.posY - 1.6 + Math.random());
			float z = (float) (entity.posZ + Math.random());
			Botania.proxy.wispFX(entity.worldObj, x, y, z, (float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random(), -0.3F + (float) Math.random() * 0.2F);
		}
		if(!entity.worldObj.isRemote)
			entity.worldObj.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.entity_endermen_teleport, SoundCategory.PLAYERS, 1F, 1F);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack) {
		return EnumAction.BLOCK;
	}

	@Override
	public BlockPos getBinding(ItemStack stack) {
		int x = ItemNBTHelper.getInt(stack, TAG_X, 0);
		int y = ItemNBTHelper.getInt(stack, TAG_Y, -1);
		int z = ItemNBTHelper.getInt(stack, TAG_Z, 0);
		return y == -1 ? null : new BlockPos(x, y, z);
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

}
