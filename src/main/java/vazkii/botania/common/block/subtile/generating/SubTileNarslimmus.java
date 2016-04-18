/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 29, 2015, 10:43:54 PM (GMT)]
 */
package vazkii.botania.common.block.subtile.generating;

import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.SubTileGenerating;
import vazkii.botania.common.lexicon.LexiconData;

import java.util.List;

public class SubTileNarslimmus extends SubTileGenerating {

	public static final String TAG_WORLD_SPAWNED = "Botania:WorldSpawned";

	private static final int RANGE = 2;

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(ticksExisted % 5 == 0) {
			List<EntitySlime> slimes = supertile.getWorld().getEntitiesWithinAABB(EntitySlime.class, new AxisAlignedBB(supertile.getPos().add(-RANGE, -RANGE, -RANGE), supertile.getPos().add(RANGE + 1, RANGE + 1, RANGE + 1)));
			for(EntitySlime slime : slimes) {
				if(slime.getEntityData().getBoolean(TAG_WORLD_SPAWNED) && !slime.isDead) {
					int size = slime.getSlimeSize();
					int mul = (int) Math.pow(2, size);
					int mana = 820 * mul;
					if(!slime.worldObj.isRemote) {
						slime.setDead();
						slime.playSound(size > 1 ? SoundEvents.entity_slime_squish : SoundEvents.entity_small_slime_squish, 1, 0.02F);
						this.mana = Math.min(getMaxMana(), this.mana + mana);
						sync();
					}

					for (int j = 0; j < mul * 8; ++j) {
						float f = slime.worldObj.rand.nextFloat() * (float)Math.PI * 2.0F;
						float f1 = slime.worldObj.rand.nextFloat() * 0.5F + 0.5F;
						float f2 = MathHelper.sin(f) * size * 0.5F * f1;
						float f3 = MathHelper.cos(f) * size * 0.5F * f1;
						float f4 = slime.worldObj.rand.nextFloat() * size * 0.5F * f1;
						slime.worldObj.spawnParticle(EnumParticleTypes.SLIME, slime.posX + f2, slime.getEntityBoundingBox().minY + f4, slime.posZ + f3, 0.0D, 0.0D, 0.0D);
					}
					break;
				}
			}
		}
	}

	@Override
	public RadiusDescriptor getRadius() {
		return new RadiusDescriptor.Square(toBlockPos(), RANGE);
	}

	@Override
	public int getMaxMana() {
		return 8000;
	}

	@Override
	public int getColor() {
		return 0x71C373;
	}

	@Override
	public LexiconEntry getEntry() {
		return LexiconData.narslimmus;
	}

	public static class SpawnIntercepter {

		@SubscribeEvent
		public void onSpawn(LivingSpawnEvent.CheckSpawn event) {
			if(event.getEntityLiving() instanceof EntitySlime && event.getResult() != Result.DENY && isSlimeChunk(event.getEntityLiving().worldObj, MathHelper.floor_double(event.getX()), MathHelper.floor_double(event.getZ())))
				event.getEntityLiving().getEntityData().setBoolean(TAG_WORLD_SPAWNED, true);
		}

		public static boolean isSlimeChunk(World world, int x, int z) {
			Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(x, 0, z));
			return chunk.getRandomWithSeed(987234911L).nextInt(10) == 0;
		}

	}

}
