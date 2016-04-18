/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 11, 2014, 5:40:55 PM (GMT)]
 */
package vazkii.botania.common.block.subtile.functional;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.sound.BotaniaSoundEvents;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.SubTileFunctional;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.lexicon.LexiconData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubTileOrechid extends SubTileFunctional {

	private static final int COST = 17500;
	private static final int COST_GOG = 700;
	private static final int DELAY = 100;
	private static final int DELAY_GOG = 2;
	private static final int RANGE = 5;
	private static final int RANGE_Y = 3;

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(redstoneSignal > 0 || !canOperate())
			return;

		int cost = getCost();
		if(!supertile.getWorld().isRemote && mana >= cost && ticksExisted % getDelay() == 0) {
			BlockPos coords = getCoordsToPut();
			if(coords != null) {
				ItemStack stack = getOreToPut();
				if(stack != null) {
					Block block = Block.getBlockFromItem(stack.getItem());
					int meta = stack.getItemDamage();
					supertile.getWorld().setBlockState(coords, block.getStateFromMeta(meta), 1 | 2);
					if(ConfigHandler.blockBreakParticles)
						supertile.getWorld().playAuxSFX(2001, coords, Block.getIdFromBlock(block) + (meta << 12));
					supertile.getWorld().playSound(null, supertile.getPos(), BotaniaSoundEvents.orechid, SoundCategory.BLOCKS, 2F, 1F);

					mana -= cost;
					sync();
				}
			}
		}
	}

	public ItemStack getOreToPut() {
		List<WeightedRandom.Item> values = new ArrayList<>();
		Map<String, Integer> map = getOreMap();
		for(String s : map.keySet())
			values.add(new StringRandomItem(map.get(s), s));

		String ore = ((StringRandomItem) WeightedRandom.getRandomItem(supertile.getWorld().rand, values)).s;

		List<ItemStack> ores = OreDictionary.getOres(ore);

		for(ItemStack stack : ores) {
			Item item = stack.getItem();
			String clname = item.getClass().getName();

			// This poem is dedicated to Greg
			//
			// Greg.
			// I get what you do when
			// others say it's a grind.
			// But take your TE ores
			// and stick them in your behind.
			if(clname.startsWith("gregtech") || clname.startsWith("gregapi"))
				continue;

			return stack;
		}

		return getOreToPut();
	}

	public BlockPos getCoordsToPut() {
		List<BlockPos> possibleCoords = new ArrayList<>();

		Block source = getSourceBlock();
		for(int i = -RANGE; i < RANGE + 1; i++)
			for(int j = -RANGE_Y; j < RANGE_Y; j++)
				for(int k = -RANGE; k < RANGE + 1; k++) {
					BlockPos pos = supertile.getPos().add(i, j, k);
					IBlockState state = supertile.getWorld().getBlockState(pos);
					if(state.getBlock().isReplaceableOreGen(state, supertile.getWorld(), pos, BlockStateMatcher.forBlock(source)))
						possibleCoords.add(pos);
				}

		if(possibleCoords.isEmpty())
			return null;
		return possibleCoords.get(supertile.getWorld().rand.nextInt(possibleCoords.size()));
	}

	public boolean canOperate() {
		return true;
	}

	public Map<String, Integer> getOreMap() {
		return BotaniaAPI.oreWeights;
	}

	public Block getSourceBlock() {
		return Blocks.stone;
	}

	public int getCost() {
		return Botania.gardenOfGlassLoaded ? COST_GOG : COST;
	}

	public int getDelay() {
		return Botania.gardenOfGlassLoaded ? DELAY_GOG : DELAY;
	}

	@Override
	public RadiusDescriptor getRadius() {
		return new RadiusDescriptor.Square(toBlockPos(), RANGE);
	}

	@Override
	public boolean acceptsRedstone() {
		return true;
	}

	@Override
	public int getColor() {
		return 0x818181;
	}

	@Override
	public int getMaxMana() {
		return getCost();
	}

	@Override
	public LexiconEntry getEntry() {
		return LexiconData.orechid;
	}

	private static class StringRandomItem extends WeightedRandom.Item {

		public String s;

		public StringRandomItem(int par1, String s) {
			super(par1);
			this.s = s;
		}

	}
}
