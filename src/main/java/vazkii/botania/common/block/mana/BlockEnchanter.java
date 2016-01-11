/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 15, 2014, 4:08:26 PM (GMT)]
 */
package vazkii.botania.common.block.mana;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.api.wand.IWandable;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.common.block.BlockModContainer;
import vazkii.botania.common.block.tile.TileEnchanter;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibBlockNames;

public class BlockEnchanter extends BlockModContainer implements IWandable, ILexiconable, IWandHUD {

	Random random;

	public BlockEnchanter() {
		super(Material.rock);
		setHardness(3.0F);
		setResistance(5.0F);
		setLightLevel(1.0F);
		setStepSound(soundTypeStone);
		setUnlocalizedName(LibBlockNames.ENCHANTER);
		setDefaultState(blockState.getBaseState().withProperty(BotaniaStateProps.ENCHANTER_DIRECTION, EnumFacing.Axis.X));
		random = new Random();
	}

	@Override
	public BlockState createBlockState() {
		return new BlockState(this, BotaniaStateProps.ENCHANTER_DIRECTION);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		switch (state.getValue(BotaniaStateProps.ENCHANTER_DIRECTION)) {
			case Z: return 1;
			case X:
			default: return 0;
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(BotaniaStateProps.ENCHANTER_DIRECTION, meta == 1 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);
	}

	@Override
	public boolean registerInCreative() {
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEnchanter();
	}

	@Override
	public Item getItemDropped(IBlockState state, Random p_149650_2_, int p_149650_3_) {
		return Item.getItemFromBlock(Blocks.lapis_block);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean onBlockActivated(World par1World, BlockPos pos, IBlockState state, EntityPlayer par5EntityPlayer, EnumFacing side, float par7, float par8, float par9) {
		TileEnchanter enchanter = (TileEnchanter) par1World.getTileEntity(pos);
		ItemStack stack = par5EntityPlayer.getCurrentEquippedItem();
		if(stack != null && stack.getItem() == ModItems.twigWand)
			return false;

		boolean stackEnchantable = stack != null && stack.getItem() != Items.book && stack.isItemEnchantable() && stack.stackSize == 1 && stack.getItem().getItemEnchantability(stack) > 0;

		if(enchanter.itemToEnchant == null) {
			if(stackEnchantable) {
				enchanter.itemToEnchant = stack.copy();
				par5EntityPlayer.inventory.setInventorySlotContents(par5EntityPlayer.inventory.currentItem, null);
				enchanter.sync();
			}
		} else if(enchanter.stage == 0) {
			if(par5EntityPlayer.inventory.addItemStackToInventory(enchanter.itemToEnchant.copy())) {
				enchanter.itemToEnchant = null;
				enchanter.sync();
			} else par5EntityPlayer.addChatMessage(new ChatComponentTranslation("botaniamisc.invFull"));
		}

		return true;
	}

	@Override
	public void breakBlock(World par1World, BlockPos pos, IBlockState state) {
		TileEnchanter enchanter = (TileEnchanter) par1World.getTileEntity(pos);

		ItemStack itemstack = enchanter.itemToEnchant;

		if (itemstack != null) {
			float f = random.nextFloat() * 0.8F + 0.1F;
			float f1 = random.nextFloat() * 0.8F + 0.1F;
			EntityItem entityitem;

			for (float f2 = random.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; par1World.spawnEntityInWorld(entityitem)) {
				int k1 = random.nextInt(21) + 10;

				if (k1 > itemstack.stackSize)
					k1 = itemstack.stackSize;

				itemstack.stackSize -= k1;
				entityitem = new EntityItem(par1World, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, new ItemStack(itemstack.getItem(), k1, itemstack.getItemDamage()));
				float f3 = 0.05F;
				entityitem.motionX = (float)random.nextGaussian() * f3 * 0.5;
				entityitem.motionY = (float)random.nextGaussian() * f3 + 0.2F;
				entityitem.motionZ = (float)random.nextGaussian() * f3 * 0.5;

				if (itemstack.hasTagCompound())
					entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
			}
		}

		par1World.updateComparatorOutputLevel(pos, state.getBlock());

		super.breakBlock(par1World, pos, state);
	}

	@Override
	public boolean onUsedByWand(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing side) {
		((TileEnchanter) world.getTileEntity(pos)).onWanded(player, stack);
		return true;
	}

	@Override
	public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
		return LexiconData.manaEnchanting;
	}

	@Override
	public void renderHUD(Minecraft mc, ScaledResolution res, World world, BlockPos pos) {
		((TileEnchanter) world.getTileEntity(pos)).renderHUD(mc, res);
	}

}
