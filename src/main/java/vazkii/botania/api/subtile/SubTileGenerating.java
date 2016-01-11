/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 24, 2014, 8:03:36 PM (GMT)]
 */
package vazkii.botania.api.subtile;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.IManaNetwork;
import vazkii.botania.api.mana.IManaCollector;

/**
 * The basic class for a Generating Flower.
 */
public class SubTileGenerating extends SubTileEntity {

	public static final int RANGE = 6;

	private static final String TAG_MANA = "mana";

	private static final String TAG_COLLECTOR_X = "collectorX";
	private static final String TAG_COLLECTOR_Y = "collectorY";
	private static final String TAG_COLLECTOR_Z = "collectorZ";
	private static final String TAG_PASSIVE_DECAY_TICKS = "passiveDecayTicks";

	protected int mana;

	public int redstoneSignal = 0;

	int sizeLastCheck = -1;
	protected TileEntity linkedCollector = null;
	public int knownMana = -1;
	public int passiveDecayTicks;

	BlockPos cachedCollectorCoordinates = null;

	/**
	 * If set to true, redstoneSignal will be updated every tick.
	 */
	public boolean acceptsRedstone() {
		return false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		linkCollector();

		if(canGeneratePassively()) {
			int delay = getDelayBetweenPassiveGeneration();
			if(delay > 0 && ticksExisted % delay == 0 && !supertile.getWorld().isRemote) {
				if(shouldSyncPassiveGeneration())
					sync();
				addMana(getValueForPassiveGeneration());
			}
		}
		emptyManaIntoCollector();

		if(acceptsRedstone()) {
			redstoneSignal = 0;
			for(EnumFacing dir : EnumFacing.VALUES) {
				int redstoneSide = supertile.getWorld().getRedstonePower(supertile.getPos().offset(dir), dir);
				redstoneSignal = Math.max(redstoneSignal, redstoneSide);
			}
		}

		if(supertile.getWorld().isRemote) {
			double particleChance = 1F - (double) mana / (double) getMaxMana() / 3.5F;
			Color color = new Color(getColor());
			if(Math.random() > particleChance)
				BotaniaAPI.internalHandler.sparkleFX(supertile.getWorld(), supertile.getPos().getX() + 0.3 + Math.random() * 0.5, supertile.getPos().getY() + 0.5 + Math.random()  * 0.5, supertile.getPos().getZ() + 0.3 + Math.random() * 0.5, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, (float) Math.random(), 5);
		}

		boolean passive = isPassiveFlower();
		if(!supertile.getWorld().isRemote) {
			int muhBalance = BotaniaAPI.internalHandler.getPassiveFlowerDecay();

			if(passive && muhBalance > 0 && passiveDecayTicks > muhBalance) {
				supertile.getWorld().playAuxSFX(2001, supertile.getPos(), Block.getIdFromBlock(supertile.getBlockType()));
				if(supertile.getWorld().getBlockState(supertile.getPos().down()).getBlock().isSideSolid(supertile.getWorld(), supertile.getPos().down(), EnumFacing.DOWN))
					supertile.getWorld().setBlockState(supertile.getPos(), Blocks.deadbush.getDefaultState());
				else supertile.getWorld().setBlockToAir(supertile.getPos());
			}
		}

		if(!overgrowth && passive)
			passiveDecayTicks++;
	}

	public void linkCollector() {
		boolean needsNew = false;
		if(linkedCollector == null) {
			needsNew = true;

			if(cachedCollectorCoordinates != null) {
				needsNew = false;
				if(supertile.getWorld().isBlockLoaded(cachedCollectorCoordinates)) {
					needsNew = true;
					TileEntity tileAt = supertile.getWorld().getTileEntity(cachedCollectorCoordinates);
					if(tileAt != null && tileAt instanceof IManaCollector && !tileAt.isInvalid()) {
						linkedCollector = tileAt;
						needsNew = false;
					}
					cachedCollectorCoordinates = null;
				}
			}
		} else {
			TileEntity tileAt = supertile.getWorld().getTileEntity(linkedCollector.getPos());
			if(tileAt != null && tileAt instanceof IManaCollector)
				linkedCollector = tileAt;
		}

		if(needsNew && ticksExisted == 1) { // New flowers only
			IManaNetwork network = BotaniaAPI.internalHandler.getManaNetworkInstance();
			int size = network.getAllCollectorsInWorld(supertile.getWorld()).size();
			if(BotaniaAPI.internalHandler.shouldForceCheck() || size != sizeLastCheck) {
				linkedCollector = network.getClosestCollector(supertile.getPos(), supertile.getWorld(), RANGE);
				sizeLastCheck = size;
			}
		}
	}

	public void linkToForcefully(TileEntity collector) {
		linkedCollector = collector;
	}

	public void addMana(int mana) {
		this.mana = Math.min(getMaxMana(), this.mana + mana);
	}

	public void emptyManaIntoCollector() {
		if(linkedCollector != null && isValidBinding()) {
			IManaCollector collector = (IManaCollector) linkedCollector;
			if(!collector.isFull() && mana > 0) {
				int manaval = Math.min(mana, collector.getMaxMana() - collector.getCurrentMana());
				mana -= manaval;
				collector.recieveMana(manaval);
			}
		}
	}

	public boolean isPassiveFlower() {
		return false;
	}

	public boolean shouldSyncPassiveGeneration() {
		return false;
	}

	public boolean canGeneratePassively() {
		return false;
	}

	public int getDelayBetweenPassiveGeneration() {
		return 20;
	}

	public int getValueForPassiveGeneration() {
		return 1;
	}

	@Override
	public List<ItemStack> getDrops(List<ItemStack> list) {
		List<ItemStack> drops = super.getDrops(list);
		populateDropStackNBTs(drops);
		return drops;
	}

	public void populateDropStackNBTs(List<ItemStack> drops) {
		if(isPassiveFlower() && ticksExisted > 0 && BotaniaAPI.internalHandler.getPassiveFlowerDecay() > 0) {
			ItemStack drop = drops.get(0);
			if(drop != null) {
				if(!drop.hasTagCompound())
					drop.setTagCompound(new NBTTagCompound());
				NBTTagCompound cmp = drop.getTagCompound();
				cmp.setInteger(TAG_PASSIVE_DECAY_TICKS, passiveDecayTicks);
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, entity, stack);
		if(isPassiveFlower()) {
			NBTTagCompound cmp = stack.getTagCompound();
			passiveDecayTicks = cmp.getInteger(TAG_PASSIVE_DECAY_TICKS);
		}
	}

	@Override
	public boolean onWanded(EntityPlayer player, ItemStack wand) {
		if(player == null)
			return false;

		if(!player.worldObj.isRemote)
			sync();

		knownMana = mana;
		player.worldObj.playSoundAtEntity(player, "botania:ding", 0.1F, 1F);

		return super.onWanded(player, wand);
	}

	public int getMaxMana() {
		return 20;
	}

	public int getColor() {
		return 0xFFFFFF;
	}

	@Override
	public void readFromPacketNBT(NBTTagCompound cmp) {
		mana = cmp.getInteger(TAG_MANA);
		passiveDecayTicks = cmp.getInteger(TAG_PASSIVE_DECAY_TICKS);

		int x = cmp.getInteger(TAG_COLLECTOR_X);
		int y = cmp.getInteger(TAG_COLLECTOR_Y);
		int z = cmp.getInteger(TAG_COLLECTOR_Z);

		cachedCollectorCoordinates = y < 0 ? null : new BlockPos(x, y, z);
	}

	@Override
	public void writeToPacketNBT(NBTTagCompound cmp) {
		cmp.setInteger(TAG_MANA, mana);
		cmp.setInteger(TAG_TICKS_EXISTED, ticksExisted);
		cmp.setInteger(TAG_PASSIVE_DECAY_TICKS, passiveDecayTicks);

		if(cachedCollectorCoordinates != null) {
			cmp.setInteger(TAG_COLLECTOR_X, cachedCollectorCoordinates.getX());
			cmp.setInteger(TAG_COLLECTOR_Y, cachedCollectorCoordinates.getY());
			cmp.setInteger(TAG_COLLECTOR_Z, cachedCollectorCoordinates.getZ());
		} else {
			int x = linkedCollector == null ? 0 : linkedCollector.getPos().getX();
			int y = linkedCollector == null ? -1 : linkedCollector.getPos().getY();
			int z = linkedCollector == null ? 0 : linkedCollector.getPos().getZ();

			cmp.setInteger(TAG_COLLECTOR_X, x);
			cmp.setInteger(TAG_COLLECTOR_Y, y);
			cmp.setInteger(TAG_COLLECTOR_Z, z);
		}
	}

	@Override
	public BlockPos getBinding() {
		if(linkedCollector == null)
			return null;
		return linkedCollector.getPos();
	}

	@Override
	public boolean canSelect(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public boolean bindTo(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side) {
		int range = 6;
		range *= range;

		double dist = pos.distanceSq(supertile.getPos());
		if(range >= dist) {
			TileEntity tile = player.worldObj.getTileEntity(pos);
			if(tile instanceof IManaCollector) {
				linkedCollector = tile;
				return true;
			}
		}

		return false;
	}


	public boolean isValidBinding() {
		return linkedCollector != null && !linkedCollector.isInvalid() && supertile.getWorld().getTileEntity(linkedCollector.getPos()) == linkedCollector;
	}

	@Override
	public void renderHUD(Minecraft mc, ScaledResolution res) {
		String name = StatCollector.translateToLocal("tile.botania:flower." + getUnlocalizedName() + ".name");
		int color = getColor();
		BotaniaAPI.internalHandler.drawComplexManaHUD(color, knownMana, getMaxMana(), name, res, BotaniaAPI.internalHandler.getBindDisplayForFlowerType(this), isValidBinding());
	}

	@Override
	public boolean isOvergrowthAffected() {
		return !isPassiveFlower();
	}

}
