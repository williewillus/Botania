/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Apr 26, 2014, 2:06:17 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.common.lib.LibItemNames;
import baubles.api.BaubleType;

public class ItemIcePendant extends ItemBauble implements IBaubleRender {

	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite gemIcon;
	public static Map<String, List<IceRemover>> playerIceBlocks = new HashMap();

	public ItemIcePendant() {
		super(LibItemNames.ICE_PENDANT);
	}

	@Override
	public BaubleType getBaubleType(ItemStack itemstack) {
		return BaubleType.AMULET;
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase entity) {
		super.onWornTick(stack, entity);

		if(entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;

			if(!player.worldObj.isRemote)
				tickIceRemovers(player);

			if(!player.isSneaking() && !player.isInsideOfMaterial(Material.water) && !player.worldObj.isRemote) {
				int x = MathHelper.floor_double(player.posX);
				int y = MathHelper.floor_double(player.posY - (player.isInWater() ? 0 : 1));
				int z = MathHelper.floor_double(player.posZ);

				int range = 3;
				for(int i = -range; i < range + 1; i++)
					for(int j = -range; j < range + 1; j++) {
						int x1 = x + i;
						int z1 = z + j;

						addIceBlock(player, new BlockPos(x1, y, z1));
					}
			}
		}
	}

	private void addIceBlock(EntityPlayer player, BlockPos coords) {
		String user = player.getName();
		if(!playerIceBlocks.containsKey(user))
			playerIceBlocks.put(user, new ArrayList());

		List<IceRemover> ice = playerIceBlocks.get(user);
		if(player.worldObj.getBlockState(coords).getBlock() == Blocks.water && player.worldObj.getBlockState(coords).getValue(BlockLiquid.LEVEL) == 0) {
			player.worldObj.setBlockState(coords, Blocks.ice.getDefaultState());

			if(!player.worldObj.isRemote)
				ice.add(new IceRemover(coords));
		}
	}

	private void tickIceRemovers(EntityPlayer player) {
		String user = player.getName();
		if(!playerIceBlocks.containsKey(user))
			return;

		List<IceRemover> removers = playerIceBlocks.get(user);
		for(IceRemover ice : new ArrayList<IceRemover>(removers))
			ice.tick(player.worldObj, removers);
	}

	@Override
	public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, float partialTicks, RenderType type) {
		if(type == RenderType.BODY) {
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			Helper.rotateIfSneaking(player);
			boolean armor = player.getCurrentArmor(2) != null;
			GlStateManager.rotate(180F, 1F, 0F, 0F);
			GlStateManager.translate(-0.36F, -0.3F, armor ? 0.2F : 0.15F);
			GlStateManager.rotate(-45F, 0F, 0F, 1F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);

			float f = gemIcon.getMinU();
			float f1 = gemIcon.getMaxU();
			float f2 = gemIcon.getMinV();
			float f3 = gemIcon.getMaxV();
			IconHelper.renderIconIn3D(Tessellator.getInstance(), f1, f2, f, f3, gemIcon.getIconWidth(), gemIcon.getIconHeight(), 1F / 32F);
		}
	}

	class IceRemover {

		int time = 30;
		final BlockPos coords;

		public IceRemover(BlockPos coords) {
			this.coords = coords;
		}

		public void tick(World world, List<IceRemover> list) {
			if(world.getBlockState(coords).getBlock() == Blocks.ice) {
				if(time-- == 0)
					world.setBlockState(coords, Blocks.water.getDefaultState(), 1 | 2);
				else return;
				list.remove(this);
			}
		}
	}
}
