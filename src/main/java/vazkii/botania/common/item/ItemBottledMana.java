/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jun 27, 2014, 2:41:19 AM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.entity.EntityPixie;
import vazkii.botania.common.entity.EntitySignalFlare;
import vazkii.botania.common.lib.LibItemNames;

import java.util.List;
import java.util.Random;

public class ItemBottledMana extends ItemMod {

	private static final String TAG_SEED = "randomSeed";

	public ItemBottledMana() {
		super(LibItemNames.MANA_BOTTLE);
		setMaxStackSize(1);
		setMaxDamage(6);
	}

	public void effect(EntityLivingBase living, int id) {
		switch(id) {
		case 0 : { // Random motion
			living.motionX = (Math.random() - 0.5) * 3;
			living.motionZ = (Math.random() - 0.5) * 3;
			break;
		}
		case 1 : { // Water
			if(!living.worldObj.isRemote && !living.worldObj.provider.doesWaterVaporize())
				living.worldObj.setBlockState(new BlockPos(living), Blocks.flowing_water.getDefaultState());
			break;
		}
		case 2 : { // Set on Fire
			if(!living.worldObj.isRemote)
				living.setFire(4);
			break;
		}
		case 3 : { // Mini Explosion
			if(!living.worldObj.isRemote)
				living.worldObj.createExplosion(null, living.posX, living.posY, living.posZ, 0.25F, false);
			break;
		}
		case 4 : { // Mega Jump
			if(!living.worldObj.provider.doesWaterVaporize()) {
				if(!living.worldObj.isRemote)
					living.addPotionEffect(new PotionEffect(MobEffects.resistance, 300, 5));
				living.motionY = 6;
			}

			break;
		}
		case 5 : { // Randomly set HP
			if(!living.worldObj.isRemote)
				living.setHealth(living.worldObj.rand.nextInt(19) + 1);
			break;
		}
		case 6 : { // Lots O' Hearts
			if(!living.worldObj.isRemote)
				living.addPotionEffect(new PotionEffect(MobEffects.absorption, 20 * 60 * 2, 9));
			break;
		}
		case 7 : { // All your inventory is belong to us
			if(!living.worldObj.isRemote && living instanceof EntityPlayer) {
				EntityPlayer player = ((EntityPlayer) living);
				for(int i = 0; i < player.inventory.getSizeInventory(); i++)
					if(i != player.inventory.currentItem) {
						ItemStack stackAt = player.inventory.getStackInSlot(i);
						if(stackAt != null)
							player.entityDropItem(stackAt, 0);
						player.inventory.setInventorySlotContents(i, null);
					}
			}

			break;
		}
		case 8 : { // Break your neck
			living.rotationPitch = (float) Math.random() * 360F;
			living.rotationYaw = (float) Math.random() * 180F;

			break;
		}
		case 9 : { // Highest Possible
			int x = MathHelper.floor_double(living.posX);
			MathHelper.floor_double(living.posY);
			int z = MathHelper.floor_double(living.posZ);
			for(int i = 256; i > 0; i--) {
				Block block = living.worldObj.getBlockState(new BlockPos(x, i, z)).getBlock();
				if(!block.isAir(living.worldObj.getBlockState(new BlockPos(x, i, z)), living.worldObj, new BlockPos(x, i, z))) {
					if(living instanceof EntityPlayerMP) {
						EntityPlayerMP mp = (EntityPlayerMP) living;
						mp.playerNetServerHandler.setPlayerLocation(living.posX, i + 1.6, living.posZ, living.rotationYaw, living.rotationPitch);
					}
					break;
				}
			}

			break;
		}
		case 10 : { // HYPERSPEEEEEED
			if(!living.worldObj.isRemote)
				living.addPotionEffect(new PotionEffect(MobEffects.moveSpeed, 60, 200));
			break;
		}
		case 11 : { // Night Vision
			if(!living.worldObj.isRemote)
				living.addPotionEffect(new PotionEffect(MobEffects.nightVision, 6000, 0));
			break;
		}
		case 12 : { // Flare
			if(!living.worldObj.isRemote) {
				EntitySignalFlare flare = new EntitySignalFlare(living.worldObj);
				flare.setPosition(living.posX, living.posY, living.posZ);
				flare.setColor(living.worldObj.rand.nextInt(16));
				flare.playSound(SoundEvents.entity_generic_explode, 40F, (1.0F + (living.worldObj.rand.nextFloat() - living.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);

				living.worldObj.spawnEntityInWorld(flare);

				int range = 5;
				List<EntityLivingBase> entities = living.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(living.posX - range, living.posY - range, living.posZ - range, living.posX + range, living.posY + range, living.posZ + range));
				for(EntityLivingBase entity : entities)
					if(entity != living && (!(entity instanceof EntityPlayer) || FMLCommonHandler.instance().getMinecraftServerInstance() == null || FMLCommonHandler.instance().getMinecraftServerInstance().isPVPEnabled()))
						entity.addPotionEffect(new PotionEffect(MobEffects.moveSlowdown, 50, 5));
			}

			break;
		}
		case 13 : { // Pixie Friend
			if(!living.worldObj.isRemote) {
				EntityPixie pixie = new EntityPixie(living.worldObj);
				pixie.setPosition(living.posX, living.posY + 1.5, living.posZ);
				living.worldObj.spawnEntityInWorld(pixie);
			}
			break;
		}
		case 14 : { // Nausea + Blindness :3
			if(!living.worldObj.isRemote) {
				living.addPotionEffect(new PotionEffect(MobEffects.confusion, 160, 3));
				living.addPotionEffect(new PotionEffect(MobEffects.blindness, 160, 0));
			}

			break;
		}
		case 15 : { // Drop own Head
			if(!living.worldObj.isRemote && living instanceof EntityPlayer) {
				living.attackEntityFrom(DamageSource.magic, living.getHealth() - 1);
				ItemStack stack = new ItemStack(Items.skull, 1, 3);
				ItemNBTHelper.setString(stack, "SkullOwner", living.getName());
				living.entityDropItem(stack, 0);
			}
			break;
		}
		}
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
		getSeed(par1ItemStack);
	}

	public void randomEffect(EntityLivingBase player, ItemStack stack) {
		effect(player, new Random(getSeed(stack)).nextInt(16));
	}

	long getSeed(ItemStack stack) {
		long seed = ItemNBTHelper.getLong(stack, TAG_SEED, -1);
		if(seed == -1)
			return randomSeed(stack);
		return seed;
	}

	long randomSeed(ItemStack stack) {
		long seed = Math.abs(itemRand.nextLong());
		ItemNBTHelper.setLong(stack, TAG_SEED, seed);
		return seed;
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4) {
		par3List.add(I18n.translateToLocal("botaniamisc.bottleTooltip"));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, EnumHand hand) {
		par3EntityPlayer.setActiveHand(hand);
		return ActionResult.newResult(EnumActionResult.SUCCESS, par1ItemStack);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack par1ItemStack, World par2World, EntityLivingBase living) {
		randomEffect(living, par1ItemStack);
		par1ItemStack.setItemDamage(par1ItemStack.getItemDamage() + 1);
		randomSeed(par1ItemStack);

		if(par1ItemStack.getItemDamage() == 6)
			return new ItemStack(Items.glass_bottle);
		return par1ItemStack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack) {
		return 20;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack) {
		return EnumAction.DRINK;
	}

}
