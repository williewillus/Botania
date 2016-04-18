/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [May 20, 2014, 10:56:14 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.tool.terrasteel;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.ISequentialBreaker;
import vazkii.botania.api.mana.IManaGivingItem;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.sound.BotaniaSoundEvents;
import vazkii.botania.common.achievement.ModAchievements;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.crafting.recipe.TerraPickTippingRecipe;
import vazkii.botania.common.item.IColorable;
import vazkii.botania.common.item.ItemTemperanceStone;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelPick;
import vazkii.botania.common.item.relic.ItemLokiRing;
import vazkii.botania.common.item.relic.ItemThorRing;
import vazkii.botania.common.lib.LibItemNames;

import java.awt.*;
import java.util.List;

public class ItemTerraPick extends ItemManasteelPick implements IManaItem, ISequentialBreaker, IColorable {

	private static final String TAG_ENABLED = "enabled";
	private static final String TAG_MANA = "mana";
	private static final String TAG_TIPPED = "tipped";

	private static final int MAX_MANA = Integer.MAX_VALUE;
	private static final int MANA_PER_DAMAGE = 100;

	private static final Material[] MATERIALS = new Material[] { Material.rock, Material.iron, Material.ice, Material.glass, Material.piston, Material.anvil, Material.grass, Material.ground, Material.sand, Material.snow, Material.craftedSnow, Material.clay };

	public static final int[] LEVELS = new int[] {
		0, 10000, 1000000, 10000000, 100000000, 1000000000
	};

	private static final int[] CREATIVE_MANA = new int[] {
		10000 - 1, 1000000 - 1, 10000000 - 1, 100000000 - 1, 1000000000 - 1, MAX_MANA - 1
	};

	public ItemTerraPick() {
		super(BotaniaAPI.terrasteelToolMaterial, LibItemNames.TERRA_PICK);
		GameRegistry.addRecipe(new TerraPickTippingRecipe());
		RecipeSorter.register("botania:terraPickTipping", TerraPickTippingRecipe.class, Category.SHAPELESS, "");
		addPropertyOverride(new ResourceLocation("botania", "tipped"), new IItemPropertyGetter() {
			@Override
			public float apply(ItemStack itemStack, World world, EntityLivingBase entityLivingBase) {
				return isTipped(itemStack) ? 1 : 0;
			}
		});
		addPropertyOverride(new ResourceLocation("botania", "enabled"), new IItemPropertyGetter() {
			@Override
			public float apply(ItemStack itemStack, World world, EntityLivingBase entityLivingBase) {
				return isEnabled(itemStack) ? 1 : 0;
			}
		});
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		for(int mana : CREATIVE_MANA) {
			ItemStack stack = new ItemStack(item);
			setMana(stack, mana);
			list.add(stack);
		}
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4) {
		String rankFormat = I18n.translateToLocal("botaniamisc.toolRank");
		String rank = I18n.translateToLocal("botania.rank" + getLevel(par1ItemStack));
		par3List.add(String.format(rankFormat, rank).replaceAll("&", "\u00a7"));
		if(getMana(par1ItemStack) == Integer.MAX_VALUE)
			par3List.add(TextFormatting.RED + I18n.translateToLocal("botaniamisc.getALife"));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, EnumHand hand) {
		getMana(par1ItemStack);
		int level = getLevel(par1ItemStack);

		if(level != 0) {
			setEnabled(par1ItemStack, !isEnabled(par1ItemStack));
			if(!par2World.isRemote)
				par2World.playSound(null, par3EntityPlayer.posX, par3EntityPlayer.posY, par3EntityPlayer.posZ, BotaniaSoundEvents.terraPickMode, SoundCategory.PLAYERS, 0.5F, 0.4F);
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, par1ItemStack);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float sx, float sy, float sz) {
		return player.isSneaking() ? super.onItemUse(stack, player, world, pos, hand, side, sx, sy, sz)
				: EnumActionResult.PASS;
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		if(isEnabled(par1ItemStack)) {
			int level = getLevel(par1ItemStack);

			if(level == 0)
				setEnabled(par1ItemStack, false);
			else if(par3Entity instanceof EntityPlayer && !((EntityPlayer) par3Entity).isSwingInProgress)
				addMana(par1ItemStack, -level);
		}
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
		RayTraceResult raycast = ToolCommons.raytraceFromEntity(player.worldObj, player, true, 10);
		if(!player.worldObj.isRemote && raycast != null) {
			breakOtherBlock(player, stack, pos, pos, raycast.sideHit);
			ItemLokiRing.breakOnAllCursors(player, this, stack, pos, raycast.sideHit);
			// ^ Doable with API access through the IInternalMethodHandler.
		}

		return false;
	}

	@Override
	public int getManaPerDmg() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public void breakOtherBlock(EntityPlayer player, ItemStack stack, BlockPos pos, BlockPos originPos, EnumFacing side) {
		if(!isEnabled(stack))
			return;

		World world = player.worldObj;
		Material mat = world.getBlockState(pos).getMaterial();
		if(!ToolCommons.isRightMaterial(mat, MATERIALS))
			return;

		if(world.isAirBlock(pos))
			return;

		int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.fortune, player.getHeldItemMainhand());
		boolean silk = EnchantmentHelper.getEnchantmentLevel(Enchantments.silkTouch, player.getHeldItemMainhand()) > 0;
		boolean thor = ItemThorRing.getThorRing(player) != null;
		boolean doX = thor || side.getFrontOffsetX() == 0;
		boolean doY = thor || side.getFrontOffsetY() == 0;
		boolean doZ = thor || side.getFrontOffsetZ() == 0;

		int origLevel = getLevel(stack);
		int level = origLevel + (thor ? 1 : 0);
		if(ItemTemperanceStone.hasTemperanceActive(player) && level > 2)
			level = 2;

		int range = level - 1;
		int rangeY = Math.max(1, range);

		if(range == 0 && level != 1)
			return;

		BlockPos beginDiff = new BlockPos(doX ? -range : 0, doY ? -1 : 0, doZ ? -range : 0);
		BlockPos endDiff = new BlockPos(doX ? range : 0, doY ? rangeY * 2 - 1 : 0, doZ ? range : 0);

		ToolCommons.removeBlocksInIteration(player, stack, world, pos, beginDiff, endDiff, null, MATERIALS, silk, fortune, isTipped(stack));
		if(origLevel == 5)
			player.addStat(ModAchievements.rankSSPick, 1);
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		return Integer.MAX_VALUE;
	}

	public static boolean isTipped(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_TIPPED, false);
	}

	public static void setTipped(ItemStack stack) {
		ItemNBTHelper.setBoolean(stack, TAG_TIPPED, true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
		if(par2 == 0 || !isEnabled(par1ItemStack))
			return 0xFFFFFF;

		return Color.HSBtoRGB(0.375F, (float) Math.min(1F, Math.sin(System.currentTimeMillis() / 200D) * 0.5 + 1F), 1F);
	}

	public static boolean isEnabled(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_ENABLED, false);
	}

	void setEnabled(ItemStack stack, boolean enabled) {
		ItemNBTHelper.setBoolean(stack, TAG_ENABLED, enabled);
	}

	public static void setMana(ItemStack stack, int mana) {
		ItemNBTHelper.setInt(stack, TAG_MANA, mana);
	}

	@Override
	public int getMana(ItemStack stack) {
		return getMana_(stack);
	}

	public static int getMana_(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_MANA, 0);
	}

	public static int getLevel(ItemStack stack) {
		int mana = getMana_(stack);
		for(int i = LEVELS.length - 1; i > 0; i--)
			if(mana >= LEVELS[i])
				return i;

		return 0;
	}

	@Override
	public int getMaxMana(ItemStack stack) {
		return MAX_MANA;
	}

	@Override
	public void addMana(ItemStack stack, int mana) {
		setMana(stack, Math.min(getMana(stack) + mana, MAX_MANA));
	}

	@Override
	public boolean canReceiveManaFromPool(ItemStack stack, TileEntity pool) {
		return true;
	}

	@Override
	public boolean canReceiveManaFromItem(ItemStack stack, ItemStack otherStack) {
		return !(otherStack.getItem() instanceof IManaGivingItem);
	}

	@Override
	public boolean canExportManaToPool(ItemStack stack, TileEntity pool) {
		return false;
	}

	@Override
	public boolean canExportManaToItem(ItemStack stack, ItemStack otherStack) {
		return false;
	}

	@Override
	public boolean isNoExport(ItemStack stack) {
		return true;
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
		return par2ItemStack.getItem() == ModItems.manaResource && par2ItemStack.getItemDamage() == 4 ? true : super.getIsRepairable(par1ItemStack, par2ItemStack);
	}

	@Override
	public boolean disposeOfTrashBlocks(ItemStack stack) {
		return isTipped(stack);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack before, ItemStack after, boolean slotChanged) {
		return after.getItem() != this || isEnabled(before) != isEnabled(after);
	}

}
