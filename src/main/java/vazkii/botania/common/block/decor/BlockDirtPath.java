/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 18, 2015, 1:47:03 AM (GMT)]
 */
package vazkii.botania.common.block.decor;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.common.block.BlockMod;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibBlockNames;

import java.util.UUID;

public class BlockDirtPath extends BlockMod implements ILexiconable {

	private static final UUID speedBoostUuid = UUID.fromString("c5f17cca-c89f-4f12-81da-8f04b1f27679");
	private static final AttributeModifier speedBoost = new AttributeModifier(speedBoostUuid, "Trodden dirt speed boost", 0.55, 2).setSaved(false);

	public BlockDirtPath() {
		super(Material.ground, LibBlockNames.DIRT_PATH);
		setLightOpacity(255);
		setHardness(0.6F);
		setSoundType(SoundType.GROUND);
		useNeighborBrightness = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean isToolEffective(String type, IBlockState state) {
		return type.equals("shovel");
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity) {
		if(!world.isRemote && entity instanceof EntityPlayerMP) {
			EntityLivingBase living = ((EntityLivingBase) entity);
			IAttributeInstance attr = living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
			if(!attr.hasModifier(speedBoost)) {
				attr.applyModifier(speedBoost);
			}
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		IBlockState stateAbove = world.getBlockState(pos.up());
		Block blockAbove = stateAbove.getBlock();
		if(!blockAbove.isAir(stateAbove, world, pos.up()))
			return FULL_BLOCK_AABB;
		else return new AxisAlignedBB(0F, 0F, 0F, 1F, 15F / 16F, 1F);
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.DOWN;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, World world, BlockPos pos) {
		return new AxisAlignedBB(pos, pos.add(1, 1, 1));
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
		return plantable.getPlantType(world, pos.down()) == EnumPlantType.Plains;
	}

	@Override
	public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
		return LexiconData.dirtPath;
	}

	@SubscribeEvent
	public void onTickEnd(TickEvent.WorldTickEvent event) {
		if(event.world.isRemote || event.phase != TickEvent.Phase.END)
			return;
		for(EntityPlayerMP player : event.world.getPlayers(EntityPlayerMP.class, p -> p.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).hasModifier(speedBoost))) {
			if (event.world.getBlockState(new BlockPos(player).down()).getBlock() != this) {
				player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(speedBoost);
			}
		} // todo 1.8 there's probably a better way to do this.
	}

}
