/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 26, 2014, 5:09:12 PM (GMT)]
 */
package vazkii.botania.common.entity;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IClientManaHandler;
import vazkii.botania.api.mana.ILensEffect;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaCollisionGhost;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.IManaSpreader;
import vazkii.botania.api.mana.IManaTrigger;
import vazkii.botania.api.mana.IPingable;
import vazkii.botania.api.mana.IThrottledPacket;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.item.equipment.bauble.ItemTinyPlanet;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class EntityManaBurst extends EntityThrowable implements IManaBurst {

	private static final String TAG_TICKS_EXISTED = "ticksExisted";
	private static final String TAG_COLOR = "color";
	private static final String TAG_MANA = "mana";
	private static final String TAG_STARTING_MANA = "startingMana";
	private static final String TAG_MIN_MANA_LOSS = "minManaLoss";
	private static final String TAG_TICK_MANA_LOSS = "manaLossTick";
	private static final String TAG_SPREADER_X = "spreaderX";
	private static final String TAG_SPREADER_Y = "spreaderY";
	private static final String TAG_SPREADER_Z = "spreaderZ";
	private static final String TAG_GRAVITY = "gravity";
	private static final String TAG_LENS_STACK = "lensStack";
	private static final String TAG_LAST_MOTION_X = "lastMotionX";
	private static final String TAG_LAST_MOTION_Y = "lastMotionY";
	private static final String TAG_LAST_MOTION_Z = "lastMotionZ";
	private static final String TAG_HAS_SHOOTER = "hasShooter";
	private static final String TAG_SHOOTER_UUID_MOST = "shooterUUIDMost";
	private static final String TAG_SHOOTER_UUID_LEAST = "shooterUUIDLeast";

	private static final DataParameter<Integer> COLOR = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> MANA = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> START_MANA = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> MIN_MANA_LOSS = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.VARINT);
	private static final DataParameter<Float> MANA_LOSS_PER_TICK = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> GRAVITY = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.FLOAT);
	private static final DataParameter<BlockPos> SOURCE_COORDS = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.BLOCK_POS);
	private static final DataParameter<Optional<ItemStack>> SOURCE_LENS = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.OPTIONAL_ITEM_STACK);

	float accumulatedManaLoss = 0;
	boolean fake = false;
	Set<BlockPos> alreadyCollidedAt = new HashSet<>();
	boolean fullManaLastTick = true;
	UUID shooterIdentity = null;
	int _ticksExisted = 0;
	boolean scanBeam = false;
	public List<PositionProperties> propsList = new ArrayList<>();

	public EntityManaBurst(World world) {
		super(world);
		setSize(0F, 0F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(COLOR, 0);
		dataManager.register(MANA, 0);
		dataManager.register(START_MANA, 0);
		dataManager.register(MIN_MANA_LOSS, 0);
		dataManager.register(MANA_LOSS_PER_TICK, 0F);
		dataManager.register(GRAVITY, 0F);
		dataManager.register(SOURCE_COORDS, BlockPos.ORIGIN);
		dataManager.register(SOURCE_LENS, Optional.absent());
	}

	public EntityManaBurst(IManaSpreader spreader, boolean fake) {
		this(((TileEntity)spreader).getWorld());

		TileEntity tile = (TileEntity) spreader;

		this.fake = fake;

		setBurstSourceCoords(tile.getPos());
		setLocationAndAngles(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5, 0, 0);
		rotationYaw = -(spreader.getRotationX() + 90F);
		rotationPitch = spreader.getRotationY();

		float f = 0.4F;
		double mx = MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f / 2D;
		double mz = -(MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f) / 2D;
		double my = MathHelper.sin((rotationPitch) / 180.0F * (float) Math.PI) * f / 2D;
		setMotion(mx, my, mz);
	}

	public EntityManaBurst(EntityPlayer player) {
		this(player.worldObj);

		setBurstSourceCoords(new BlockPos(0, -1, 0));
		setLocationAndAngles(player.posX, player.posY + player.getEyeHeight(), player.posZ, player.rotationYaw + 180, -player.rotationPitch);

		posX -= MathHelper.cos((rotationYaw + 180) / 180.0F * (float) Math.PI) * 0.16F;
		posY -= 0.10000000149011612D;
		posZ -= MathHelper.sin((rotationYaw + 180) / 180.0F * (float) Math.PI) * 0.16F;

		setPosition(posX, posY, posZ);
		float f = 0.4F;
		double mx = MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f / 2D;
		double mz = -(MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f) / 2D;
		double my = MathHelper.sin((rotationPitch) / 180.0F * (float) Math.PI) * f / 2D;
		setMotion(mx, my, mz);
	}

	@Override
	public void onUpdate() {
		setTicksExisted(getTicksExisted() + 1);
		super.onUpdate();
		// superUpdate(); todo 1.9 removed a ton of what appears to just be copied superclass code
		// todo 1.9 if shit breaks revert this :P

		if(!fake && !isDead)
			ping();

		ILensEffect lens = getLensInstance();
		if(lens != null)
			lens.updateBurst(this, getSourceLens());

		int mana = getMana();
		if(getTicksExisted() >= getMinManaLoss()) {
			accumulatedManaLoss += getManaLossPerTick();
			int loss = (int) accumulatedManaLoss;
			setMana(mana - loss);
			accumulatedManaLoss -= loss;

			if(getMana() <= 0)
				setDead();
		}

		particles();

		setMotion(motionX, motionY, motionZ);

		fullManaLastTick = getMana() == getStartingMana();

		if(scanBeam) {
			PositionProperties props = new PositionProperties(this);
			if(propsList.isEmpty())
				propsList.add(props);
			else {
				PositionProperties lastProps = propsList.get(propsList.size() - 1);
				if(!props.coordsEqual(lastProps))
					propsList.add(props);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_, boolean p_70056_10) {
		setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
		setRotation(p_70056_7_, p_70056_8_);
	}

	@Override
	public boolean handleWaterMovement() {
		return false;
	}

	private TileEntity collidedTile = null;
	private boolean noParticles = false;

	public TileEntity getCollidedTile(boolean noParticles) {
		this.noParticles = noParticles;

		while(!isDead)
			onUpdate();

		if(fake)
			incrementFakeParticleTick();

		return collidedTile;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeEntityToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setInteger(TAG_TICKS_EXISTED, getTicksExisted());
		par1nbtTagCompound.setInteger(TAG_COLOR, getColor());
		par1nbtTagCompound.setInteger(TAG_MANA, getMana());
		par1nbtTagCompound.setInteger(TAG_STARTING_MANA, getStartingMana());
		par1nbtTagCompound.setInteger(TAG_MIN_MANA_LOSS, getMinManaLoss());
		par1nbtTagCompound.setFloat(TAG_TICK_MANA_LOSS, getManaLossPerTick());
		par1nbtTagCompound.setFloat(TAG_GRAVITY, getGravity());

		ItemStack stack = getSourceLens();
		NBTTagCompound lensCmp = new NBTTagCompound();
		if(stack != null)
			stack.writeToNBT(lensCmp);
		par1nbtTagCompound.setTag(TAG_LENS_STACK, lensCmp);

		BlockPos coords = getBurstSourceBlockPos();
		par1nbtTagCompound.setInteger(TAG_SPREADER_X, coords.getX());
		par1nbtTagCompound.setInteger(TAG_SPREADER_Y, coords.getY());
		par1nbtTagCompound.setInteger(TAG_SPREADER_Z, coords.getZ());

		par1nbtTagCompound.setDouble(TAG_LAST_MOTION_X, motionX);
		par1nbtTagCompound.setDouble(TAG_LAST_MOTION_Y, motionY);
		par1nbtTagCompound.setDouble(TAG_LAST_MOTION_Z, motionZ);

		UUID identity = getShooterUUID();
		boolean hasShooter = identity != null;
		par1nbtTagCompound.setBoolean(TAG_HAS_SHOOTER, hasShooter);
		if(hasShooter) {
			par1nbtTagCompound.setLong(TAG_SHOOTER_UUID_MOST, identity.getMostSignificantBits());
			par1nbtTagCompound.setLong(TAG_SHOOTER_UUID_LEAST, identity.getLeastSignificantBits());
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readEntityFromNBT(par1nbtTagCompound);
		setTicksExisted(par1nbtTagCompound.getInteger(TAG_TICKS_EXISTED));
		setColor(par1nbtTagCompound.getInteger(TAG_COLOR));
		setMana(par1nbtTagCompound.getInteger(TAG_MANA));
		setStartingMana(par1nbtTagCompound.getInteger(TAG_STARTING_MANA));
		setMinManaLoss(par1nbtTagCompound.getInteger(TAG_MIN_MANA_LOSS));
		setManaLossPerTick(par1nbtTagCompound.getFloat(TAG_TICK_MANA_LOSS));
		setGravity(par1nbtTagCompound.getFloat(TAG_GRAVITY));

		NBTTagCompound lensCmp = par1nbtTagCompound.getCompoundTag(TAG_LENS_STACK);
		ItemStack stack = ItemStack.loadItemStackFromNBT(lensCmp);
		if(stack != null)
			setSourceLens(stack);
		else setSourceLens(new ItemStack(Blocks.stone, 0, 0));

		int x = par1nbtTagCompound.getInteger(TAG_SPREADER_X);
		int y = par1nbtTagCompound.getInteger(TAG_SPREADER_Y);
		int z = par1nbtTagCompound.getInteger(TAG_SPREADER_Z);

		setBurstSourceCoords(new BlockPos(x, y, z));

		double lastMotionX = par1nbtTagCompound.getDouble(TAG_LAST_MOTION_X);
		double lastMotionY = par1nbtTagCompound.getDouble(TAG_LAST_MOTION_Y);
		double lastMotionZ = par1nbtTagCompound.getDouble(TAG_LAST_MOTION_Z);

		setMotion(lastMotionX, lastMotionY, lastMotionZ);

		boolean hasShooter = par1nbtTagCompound.getBoolean(TAG_HAS_SHOOTER);
		if(hasShooter) {
			long most = par1nbtTagCompound.getLong(TAG_SHOOTER_UUID_MOST);
			long least = par1nbtTagCompound.getLong(TAG_SHOOTER_UUID_LEAST);
			UUID identity = getShooterUUID();
			if(identity == null || most != identity.getMostSignificantBits() || least != identity.getLeastSignificantBits())
				shooterIdentity = new UUID(most, least);
		}
	}

	public void particles() {
		if(isDead || !worldObj.isRemote)
			return;

		ILensEffect lens = getLensInstance();
		if(lens != null && !lens.doParticles(this, getSourceLens()))
			return;

		Color color = new Color(getColor());
		float r = color.getRed() / 255F;
		float g = color.getGreen() / 255F;
		float b = color.getBlue() / 255F;

		int mana = getMana();
		int maxMana = getStartingMana();
		float osize = (float) mana / (float) maxMana;
		float size = osize;

		if(fake) {
			if(getMana() == getStartingMana())
				size = 2F;
			else if(fullManaLastTick)
				size = 4F;

			if(!noParticles && shouldDoFakeParticles())
				Botania.proxy.sparkleFX(worldObj, posX, posY, posZ, r, g, b, 0.4F * size, 1, true);
		} else {
			boolean monocle = Botania.proxy.isClientPlayerWearingMonocle();
			if(monocle)
				Botania.proxy.setWispFXDepthTest(false);

			if(ConfigHandler.subtlePowerSystem)
				Botania.proxy.wispFX(worldObj, posX, posY, posZ, r, g, b, 0.1F * size, (float) (Math.random() - 0.5F) * 0.02F, (float) (Math.random() - 0.5F) * 0.02F, (float) (Math.random() - 0.5F) * 0.01F);
			else {
				float or = r;
				float og = g;
				float ob = b;

				double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b; // Standard relative luminance calculation

				double savedPosX = posX;
				double savedPosY = posY;
				double savedPosZ = posZ;

				Vector3 currentPos = Vector3.fromEntity(this);
				Vector3 oldPos = new Vector3(prevPosX, prevPosY, prevPosZ);
				Vector3 diffVec = oldPos.copy().sub(currentPos);
				Vector3 diffVecNorm = diffVec.copy().normalize();

				double distance = 0.095;

				do {
					if (luminance < 0.1) {
						r = or + (float) Math.random() * 0.125F;
						g = og + (float) Math.random() * 0.125F;
						b = ob + (float) Math.random() * 0.125F;
					}
					size = osize + ((float) Math.random() - 0.5F) * 0.065F + (float) Math.sin(new Random(entityUniqueID.getMostSignificantBits()).nextInt(9001)) * 0.4F;
					Botania.proxy.wispFX(worldObj, posX, posY, posZ, r, g, b, 0.2F * size, (float) -motionX * 0.01F, (float) -motionY * 0.01F, (float) -motionZ * 0.01F);

					posX += diffVecNorm.x * distance;
					posY += diffVecNorm.y * distance;
					posZ += diffVecNorm.z * distance;

					currentPos = Vector3.fromEntity(this);
					diffVec = oldPos.copy().sub(currentPos);
					if(getEntityData().hasKey(ItemTinyPlanet.TAG_ORBIT))
						break;
				} while(Math.abs(diffVec.mag()) > distance);

				Botania.proxy.wispFX(worldObj, posX, posY, posZ, or, og, ob, 0.1F * size, (float) (Math.random() - 0.5F) * 0.06F, (float) (Math.random() - 0.5F) * 0.06F, (float) (Math.random() - 0.5F) * 0.06F);

				posX = savedPosX;
				posY = savedPosY;
				posZ = savedPosZ;
			}

			if(monocle)
				Botania.proxy.setWispFXDepthTest(true);
		}
	}

	@Override
	protected void onImpact(RayTraceResult RayTraceResult) {
		boolean collided = false;
		boolean dead = false;

		if(RayTraceResult.entityHit == null) {
			TileEntity tile = worldObj.getTileEntity(RayTraceResult.getBlockPos());
			Block block = worldObj.getBlockState(RayTraceResult.getBlockPos()).getBlock();

			if(tile instanceof IManaCollisionGhost && ((IManaCollisionGhost) tile).isGhost() && !(block instanceof IManaTrigger) || block instanceof BlockBush || block instanceof BlockLeaves)
				return;

			if(BotaniaAPI.internalHandler.isBuildcraftPipe(tile))
				return;

			BlockPos coords = getBurstSourceBlockPos();
			if(tile != null && !tile.getPos().equals(coords))
				collidedTile = tile;

			if(tile == null || !tile.getPos().equals(coords)) {
				if(!fake && !noParticles && (!worldObj.isRemote || tile instanceof IClientManaHandler) && tile != null && tile instanceof IManaReceiver && ((IManaReceiver) tile).canRecieveManaFromBursts())
					onRecieverImpact((IManaReceiver) tile, tile.getPos());

				if(block instanceof IManaTrigger)
					((IManaTrigger) block).onBurstCollision(this, worldObj, RayTraceResult.getBlockPos());

				boolean ghost = tile instanceof IManaCollisionGhost;
				dead = !ghost;
				if(ghost)
					return;
			}

			collided = true;
		}

		ILensEffect lens = getLensInstance();
		if(lens != null)
			dead = lens.collideBurst(this, RayTraceResult, collidedTile != null && collidedTile instanceof IManaReceiver && ((IManaReceiver) collidedTile).canRecieveManaFromBursts(), dead, getSourceLens());

		if(collided && !hasAlreadyCollidedAt(RayTraceResult.getBlockPos()))
			alreadyCollidedAt.add(RayTraceResult.getBlockPos());

		if(dead && !isDead) {
			if(!fake) {
				Color color = new Color(getColor());
				float r = color.getRed() / 255F;
				float g = color.getGreen() / 255F;
				float b = color.getBlue() / 255F;

				int mana = getMana();
				int maxMana = getStartingMana();
				float size = (float) mana / (float) maxMana;

				if(!ConfigHandler.subtlePowerSystem)
					for(int i = 0; i < 4; i++)
						Botania.proxy.wispFX(worldObj, posX, posY, posZ, r, g, b, 0.15F * size, (float) (Math.random() - 0.5F) * 0.04F, (float) (Math.random() - 0.5F) * 0.04F, (float) (Math.random() - 0.5F) * 0.04F);
				Botania.proxy.sparkleFX(worldObj, (float) posX, (float) posY, (float) posZ, r, g, b, 4, 2);
			}

			setDead();
		}
	}

	private void onRecieverImpact(IManaReceiver tile, BlockPos pos) {
		int mana = getMana();
		if(tile instanceof IManaCollector)
			mana *= ((IManaCollector) tile).getManaYieldMultiplier(this);

		tile.recieveMana(mana);
		if(tile instanceof IThrottledPacket)
			((IThrottledPacket) tile).markDispatchable();
		else VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldObj, pos);
	}

	@Override
	public void setDead() {
		super.setDead();

		if(!fake) {
			TileEntity tile = getShooter();
			if(tile != null && tile instanceof IManaSpreader)
				((IManaSpreader) tile).setCanShoot(true);
		} else setDeathTicksForFakeParticle();
	}

	private TileEntity getShooter() {
		return worldObj.getTileEntity(getBurstSourceBlockPos());
	}

	@Override
	protected float getGravityVelocity() {
		return getGravity();
	}

	@Override
	public boolean isFake() {
		return fake;
	}

	@Override
	public void setFake(boolean fake) {
		this.fake = fake;
	}

	public void setScanBeam() {
		scanBeam = true;
	}

	@Override
	public int getColor() {
		return dataManager.get(COLOR);
	}

	@Override
	public void setColor(int color) {
		dataManager.set(COLOR, color);
	}

	@Override
	public int getMana() {
		return dataManager.get(MANA);
	}

	@Override
	public void setMana(int mana) {
		dataManager.set(MANA, mana);
	}

	@Override
	public int getStartingMana() {
		return dataManager.get(START_MANA);
	}

	@Override
	public void setStartingMana(int mana) {
		dataManager.set(START_MANA, mana);
	}

	@Override
	public int getMinManaLoss() {
		return dataManager.get(MIN_MANA_LOSS);
	}

	@Override
	public void setMinManaLoss(int minManaLoss) {
		dataManager.set(MIN_MANA_LOSS, minManaLoss);
	}

	@Override
	public float getManaLossPerTick() {
		return dataManager.get(MANA_LOSS_PER_TICK);
	}

	@Override
	public void setManaLossPerTick(float mana) {
		dataManager.set(MANA_LOSS_PER_TICK, mana);
	}

	@Override
	public float getGravity() {
		return dataManager.get(GRAVITY);
	}

	@Override
	public void setGravity(float gravity) {
		dataManager.set(GRAVITY, gravity);
	}

	@Override
	public BlockPos getBurstSourceBlockPos() {
		return dataManager.get(SOURCE_COORDS);
	}

	@Override
	public void setBurstSourceCoords(BlockPos pos) {
		dataManager.set(SOURCE_COORDS, pos);
	}

	@Override
	public ItemStack getSourceLens() {
		Optional<ItemStack> stack = dataManager.get(SOURCE_LENS);
		if(!stack.isPresent())
			return new ItemStack(Blocks.stone, 0, 0); // todo 1.9 refact later
		else return stack.get();
	}

	@Override
	public void setSourceLens(ItemStack lens) {
		dataManager.set(SOURCE_LENS, lens == null ? Optional.absent() : Optional.of(lens));
	}

	@Override
	public int getTicksExisted() {
		return _ticksExisted;
	}

	public void setTicksExisted(int ticks) {
		_ticksExisted = ticks;
	}

	private ILensEffect getLensInstance() {
		ItemStack lens = getSourceLens();
		if(lens != null && lens.getItem() instanceof ILensEffect)
			return (ILensEffect) lens.getItem();

		return null;
	}

	@Override
	public void setMotion(double x, double y, double z) {
		motionX = x;
		motionY = y;
		motionZ = z;
	}

	@Override
	public boolean hasAlreadyCollidedAt(BlockPos pos) {
		return alreadyCollidedAt.contains(pos);
	}

	@Override
	public void setCollidedAt(BlockPos pos) {
		if(!hasAlreadyCollidedAt(pos))
			alreadyCollidedAt.add(pos.toImmutable());
	}

	@Override
	public void setShooterUUID(UUID uuid) {
		shooterIdentity = uuid;
	}

	@Override
	public UUID getShooterUUID() {
		return shooterIdentity;
	}

	@Override
	public void ping() {
		TileEntity tile = getShooter();
		if(tile != null && tile instanceof IPingable)
			((IPingable) tile).pingback(this, getShooterUUID());
	}

	protected boolean shouldDoFakeParticles() {
		if (ConfigHandler.staticWandBeam)
			return true;

		TileEntity tile = getShooter();
		return tile != null
				&& tile instanceof IManaSpreader
				&& (getMana() != getStartingMana() && fullManaLastTick
					|| Math.abs(((IManaSpreader) tile).getBurstParticleTick() - getTicksExisted()) < 4);
	}

	private void incrementFakeParticleTick() {
		TileEntity tile = getShooter();
		if(tile != null && tile instanceof IManaSpreader) {
			IManaSpreader spreader = (IManaSpreader) tile;
			spreader.setBurstParticleTick(spreader.getBurstParticleTick()+2);
			if(spreader.getLastBurstDeathTick() != -1 && spreader.getBurstParticleTick() > spreader.getLastBurstDeathTick())
				spreader.setBurstParticleTick(0);
		}
	}

	private void setDeathTicksForFakeParticle() {
		BlockPos coords = getBurstSourceBlockPos();
		TileEntity tile = worldObj.getTileEntity(coords);
		if(tile != null && tile instanceof IManaSpreader)
			((IManaSpreader) tile).setLastBurstDeathTick(getTicksExisted());
	}

	public static class PositionProperties {

		public final BlockPos coords;
		public final IBlockState state;

		public boolean invalid = false;

		public PositionProperties(Entity entity) {
			int x = MathHelper.floor_double(entity.posX);
			int y = MathHelper.floor_double(entity.posY);
			int z = MathHelper.floor_double(entity.posZ);
			coords = new BlockPos(x, y, z);
			state = entity.worldObj.getBlockState(coords);
		}

		public boolean coordsEqual(PositionProperties props) {
			return coords.equals(props.coords);
		}

		public boolean contentsEqual(World world) {
			if(!world.isBlockLoaded(coords)) {
				invalid = true;
				return false;
			}

			return world.getBlockState(coords) == this.state;
		}

		@Override
		public int hashCode() {
			return Objects.hash(coords, state);
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof PositionProperties
					&& ((PositionProperties) o).state == state
					&& ((PositionProperties) o).coords.equals(coords);
		}
	}

}
