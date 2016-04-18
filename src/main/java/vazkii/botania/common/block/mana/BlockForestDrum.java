/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [May 16, 2014, 7:34:37 PM (GMT)]
 */
package vazkii.botania.common.block.mana;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.common.registry.GameRegistry;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.mana.IManaTrigger;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.DrumVariant;
import vazkii.botania.common.block.BlockMod;
import vazkii.botania.common.item.ItemGrassHorn;
import vazkii.botania.common.item.block.ItemBlockWithMetadataAndName;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibBlockNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockForestDrum extends BlockMod implements IManaTrigger, ILexiconable {

	private static final AxisAlignedBB AABB = new AxisAlignedBB(3/16.0, 0, 3/16.0, 13/16.0, 14/16.0, 13/16.0);

	public BlockForestDrum() {
		super(Material.wood, LibBlockNames.FOREST_DRUM);
		setHardness(2.0F);
		setSoundType(SoundType.WOOD);
		setDefaultState(blockState.getBaseState().withProperty(BotaniaStateProps.DRUM_VARIANT, DrumVariant.WILD));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB;
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BotaniaStateProps.DRUM_VARIANT);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BotaniaStateProps.DRUM_VARIANT).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta >= DrumVariant.values().length) {
			meta = 0;
		}
		return getDefaultState().withProperty(BotaniaStateProps.DRUM_VARIANT, DrumVariant.values()[meta]);
	}

	@Override
	public void registerItemForm() {
		GameRegistry.register(new ItemBlockWithMetadataAndName(this), getRegistryName());
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getBlock().getMetaFromState(state);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
		for(int i = 0; i < 3; i++)
			list.add(new ItemStack(item, 1, i));
	}

	@Override
	public void onBurstCollision(IManaBurst burst, World world, BlockPos pos) {
		if(burst.isFake())
			return;
		DrumVariant variant = world.getBlockState(pos).getValue(BotaniaStateProps.DRUM_VARIANT);
		if(variant == DrumVariant.WILD)
			ItemGrassHorn.breakGrass(world, null, 0, pos);
		else if(variant == DrumVariant.CANOPY)
			ItemGrassHorn.breakGrass(world, null, 1, pos);
		else if(!world.isRemote) {
			int range = 10;
			List<EntityLiving> entities = world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(pos.add(-range, -range, -range), pos.add(range + 1, range + 1, range + 1)));
			List<EntityLiving> shearables = new ArrayList<>();
			ItemStack stack = new ItemStack(this, 1, 1);

			for(EntityLiving entity : entities) {
				if(entity instanceof IShearable && ((IShearable) entity).isShearable(stack, world, new BlockPos(entity))) {
					shearables.add(entity);
				} else if(entity instanceof EntityCow) {
					List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(entity.posX, entity.posY, entity.posZ, entity.posX + entity.width, entity.posY + entity.height, entity.posZ + entity.width));
					for(EntityItem item : items) {
						ItemStack itemstack = item.getEntityItem();
						if(itemstack != null && itemstack.getItem() == Items.bucket && !world.isRemote) {
							while(itemstack.stackSize > 0) {
								EntityItem ent = entity.entityDropItem(new ItemStack(Items.milk_bucket), 1.0F);
								ent.motionY += world.rand.nextFloat() * 0.05F;
								ent.motionX += (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
								ent.motionZ += (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
								itemstack.stackSize--;
							}
							item.setDead();
						}
					}
				}
			}

			Collections.shuffle(shearables);
			int sheared = 0;

			for(EntityLiving entity : shearables) {
				if(sheared > 4)
					break;

				List<ItemStack> stacks = ((IShearable) entity).onSheared(stack, world, new BlockPos(entity), 0);
				if(stacks != null)
					for(ItemStack wool : stacks) {
						EntityItem ent = entity.entityDropItem(wool, 1.0F);
						ent.motionY += world.rand.nextFloat() * 0.05F;
						ent.motionX += (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
						ent.motionZ += (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
					}
				++sheared;
			}
		}

		if(!world.isRemote)
			for(int i = 0; i < 10; i++)
				world.playSound(null, pos, SoundEvents.block_note_basedrum, SoundCategory.BLOCKS, 1F, 1F);
		else world.spawnParticle(EnumParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5D, 1.0 / 24.0, 0, 0);

	}

	@Override
	public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
		DrumVariant variant = world.getBlockState(pos).getValue(BotaniaStateProps.DRUM_VARIANT);

		switch(variant) {
		case GATHERING:
			return LexiconData.gatherDrum;
		case CANOPY:
			return LexiconData.canopyDrum;
		case WILD:
		default:
			return LexiconData.forestDrum;
		}
	}

}
