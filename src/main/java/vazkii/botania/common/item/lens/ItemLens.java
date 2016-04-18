/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 31, 2014, 3:02:58 PM (GMT)]
 */
package vazkii.botania.common.item.lens;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.ICompositableLens;
import vazkii.botania.api.mana.ILens;
import vazkii.botania.api.mana.ILensControl;
import vazkii.botania.api.mana.IManaSpreader;
import vazkii.botania.api.mana.ITinyPlanetExcempt;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.crafting.recipe.CompositeLensRecipe;
import vazkii.botania.common.crafting.recipe.LensDyeingRecipe;
import vazkii.botania.common.item.IColorable;
import vazkii.botania.common.item.ItemMod;
import vazkii.botania.common.lib.LibItemNames;

import java.awt.*;
import java.util.List;

public class ItemLens extends ItemMod implements ILensControl, ICompositableLens, ITinyPlanetExcempt, IColorable {

	public static final int SUBTYPES = 22;

	public static final int NORMAL = 0,
			SPEED = 1,
			POWER = 2,
			TIME = 3,
			EFFICIENCY = 4,
			BOUNCE = 5,
			GRAVITY = 6,
			MINE = 7,
			DAMAGE = 8,
			PHANTOM = 9,
			MAGNET = 10,
			EXPLOSIVE = 11,
			INFLUENCE = 12,
			WEIGHT = 13,
			PAINT = 14,
			FIRE = 15,
			PISTON = 16,
			LIGHT = 17,
			WARP = 18,
			REDIRECT = 19,
			FIREWORK = 20,
			FLARE = 21;

	public static final int STORM = 5000;

	private static final int PROP_NONE = 0,
			PROP_POWER = 1,
			PROP_ORIENTATION = 1 << 1,
			PROP_TOUCH = 1 << 2,
			PROP_INTERACTION = 1 << 3,
			PROP_DAMAGE = 1 << 4,
			PROP_CONTROL = 1 << 5;

	private static final int[] props = new int[SUBTYPES];
	private static final Lens[] lenses = new Lens[SUBTYPES];
	private static Lens fallbackLens = new Lens();
	private static Lens stormLens = new LensStorm();

	static {
		setProps(NORMAL, PROP_NONE);
		setProps(SPEED, PROP_NONE);
		setProps(POWER, PROP_POWER);
		setProps(TIME, PROP_NONE);
		setProps(EFFICIENCY, PROP_NONE);
		setProps(BOUNCE, PROP_TOUCH);
		setProps(GRAVITY, PROP_ORIENTATION);
		setProps(MINE, PROP_TOUCH | PROP_INTERACTION);
		setProps(DAMAGE, PROP_DAMAGE);
		setProps(PHANTOM, PROP_TOUCH);
		setProps(MAGNET, PROP_ORIENTATION);
		setProps(EXPLOSIVE, PROP_DAMAGE | PROP_TOUCH | PROP_INTERACTION);
		setProps(INFLUENCE, PROP_NONE);
		setProps(WEIGHT, PROP_TOUCH | PROP_INTERACTION);
		setProps(PAINT, PROP_TOUCH | PROP_INTERACTION);
		setProps(FIRE, PROP_DAMAGE | PROP_TOUCH | PROP_INTERACTION);
		setProps(PISTON, PROP_TOUCH | PROP_INTERACTION);
		setProps(LIGHT, PROP_TOUCH | PROP_INTERACTION);
		setProps(WARP, PROP_NONE);
		setProps(REDIRECT, PROP_TOUCH | PROP_INTERACTION);
		setProps(FIREWORK, PROP_TOUCH);
		setProps(FLARE, PROP_CONTROL);

		setLens(NORMAL, fallbackLens);
		setLens(SPEED, new LensSpeed());
		setLens(POWER, new LensPower());
		setLens(TIME, new LensTime());
		setLens(EFFICIENCY, new LensEfficiency());
		setLens(BOUNCE, new LensBounce());
		setLens(GRAVITY, new LensGravity());
		setLens(MINE, new LensMine());
		setLens(DAMAGE, new LensDamage());
		setLens(PHANTOM, new LensPhantom());
		setLens(MAGNET, new LensMagnet());
		setLens(EXPLOSIVE, new LensExplosive());
		setLens(INFLUENCE, new LensInfluence());
		setLens(WEIGHT, new LensWeight());
		setLens(PAINT, new LensPaint());
		setLens(FIRE, new LensFire());
		setLens(PISTON, new LensPiston());
		setLens(LIGHT, new LensLight());
		setLens(WARP, new LensWarp());
		setLens(REDIRECT, new LensRedirect());
		setLens(FIREWORK, new LensFirework());
		setLens(FLARE, new LensFlare());
	}

	private static final String TAG_COLOR = "color";
	private static final String TAG_COMPOSITE_LENS = "compositeLens";

	public ItemLens() {
		super(LibItemNames.LENS);
		setMaxStackSize(1);
		setHasSubtypes(true);

		GameRegistry.addRecipe(new CompositeLensRecipe());
		GameRegistry.addRecipe(new LensDyeingRecipe());
		RecipeSorter.register("botania:compositeLens", CompositeLensRecipe.class, Category.SHAPELESS, "");
		RecipeSorter.register("botania:lensDying", LensDyeingRecipe.class, Category.SHAPELESS, "");
	}

	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		for(int i = 0; i < SUBTYPES; i++)
			par3List.add(new ItemStack(par1, 1, i));
	}

	@Override
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
		return par2 == 0 ? getLensColor(par1ItemStack) : 0xFFFFFF;
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		return "item." + LibItemNames.LENS_NAMES[Math.min(SUBTYPES - 1, par1ItemStack.getItemDamage())];
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		int storedColor = getStoredColor(par1ItemStack);
		if(storedColor != -1)
			par3List.add(String.format(I18n.translateToLocal("botaniamisc.color"), I18n.translateToLocal("botania.color" + storedColor)));
	}


	public String getItemShortTermName(ItemStack stack) {
		return I18n.translateToLocal(stack.getUnlocalizedName().replaceAll("item.", "item.botania:") + ".short");
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		ItemStack compositeLens = getCompositeLens(stack);
		if(compositeLens == null)
			return super.getItemStackDisplayName(stack);
		return String.format(I18n.translateToLocal("item.botania:compositeLens.name"), getItemShortTermName(stack), getItemShortTermName(compositeLens));
	}

	@Override
	public void apply(ItemStack stack, BurstProperties props) {
		int storedColor = getStoredColor(stack);
		if(storedColor != -1)
			props.color = getLensColor(stack);

		getLens(stack.getItemDamage()).apply(stack, props);

		ItemStack compositeLens = getCompositeLens(stack);
		if(compositeLens != null && compositeLens.getItem() instanceof ILens)
			((ILens) compositeLens.getItem()).apply(compositeLens, props);
	}

	@Override
	public boolean collideBurst(IManaBurst burst, RayTraceResult pos, boolean isManaBlock, boolean dead, ItemStack stack) {
		EntityThrowable entity = (EntityThrowable) burst;

		dead = getLens(stack.getItemDamage()).collideBurst(burst, entity, pos, isManaBlock, dead, stack);

		ItemStack compositeLens = getCompositeLens(stack);
		if(compositeLens != null && compositeLens.getItem() instanceof ILens)
			dead = ((ILens) compositeLens.getItem()).collideBurst(burst, pos, isManaBlock, dead, compositeLens);

		return dead;
	}

	@Override
	public void updateBurst(IManaBurst burst, ItemStack stack) {
		EntityThrowable entity = (EntityThrowable) burst;
		int storedColor = getStoredColor(stack);

		if(storedColor == 16 && entity.worldObj.isRemote)
			burst.setColor(getLensColor(stack));

		getLens(stack.getItemDamage()).updateBurst(burst, entity, stack);

		ItemStack compositeLens = getCompositeLens(stack);
		if(compositeLens != null && compositeLens.getItem() instanceof ILens)
			((ILens) compositeLens.getItem()).updateBurst(burst, compositeLens);
	}

	@Override
	public int getLensColor(ItemStack stack) {
		int storedColor = getStoredColor(stack);

		if(storedColor == -1)
			return 0xFFFFFF;

		if(storedColor == 16)
			return Color.HSBtoRGB(Botania.proxy.getWorldElapsedTicks() * 2 % 360 / 360F, 1F, 1F);

		return EnumDyeColor.byMetadata(storedColor).getMapColor().colorValue;
	}

	public static int getStoredColor(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_COLOR, -1);
	}

	public static ItemStack setLensColor(ItemStack stack, int color) {
		ItemNBTHelper.setInt(stack, TAG_COLOR, color);
		return stack;
	}

	@Override
	public boolean doParticles(IManaBurst burst, ItemStack stack) {
		return true;
	}

	public static void setProps(int lens, int props_) {
		props[lens] = props_;
	}

	public static void setLens(int index, Lens lens) {
		lenses[index] = lens;
	}

	public static boolean isBlacklisted(ItemStack lens1, ItemStack lens2) {
		ICompositableLens item1 = (ICompositableLens) lens1.getItem();
		ICompositableLens item2 = (ICompositableLens) lens2.getItem();
		return (item1.getProps(lens1) & item2.getProps(lens2)) != 0;
	}

	public static Lens getLens(int index) {
		if(index == STORM)
			return stormLens;

		Lens lens = lenses[index];
		return lens == null ? fallbackLens : lens;
	}

	@Override
	public boolean canCombineLenses(ItemStack sourceLens, ItemStack compositeLens) {
		ICompositableLens sourceItem = (ICompositableLens) sourceLens.getItem();
		ICompositableLens compositeItem = (ICompositableLens) compositeLens.getItem();
		if(sourceItem == compositeItem && sourceLens.getItemDamage() == compositeLens.getItemDamage())
			return false;

		if(!sourceItem.isCombinable(sourceLens) || !compositeItem.isCombinable(compositeLens))
			return false;

		if(isBlacklisted(sourceLens, compositeLens))
			return false;

		return true;
	}

	@Override
	public ItemStack getCompositeLens(ItemStack stack) {
		NBTTagCompound cmp = ItemNBTHelper.getCompound(stack, TAG_COMPOSITE_LENS, false);
		ItemStack lens = ItemStack.loadItemStackFromNBT(cmp);
		return lens;
	}

	@Override
	public ItemStack setCompositeLens(ItemStack sourceLens, ItemStack compositeLens) {
		NBTTagCompound cmp = new NBTTagCompound();
		compositeLens.writeToNBT(cmp);
		ItemNBTHelper.setCompound(sourceLens, TAG_COMPOSITE_LENS, cmp);

		return sourceLens;
	}

	@Override
	public boolean shouldPull(ItemStack stack) {
		return stack.getItemDamage() != STORM;
	}

	@Override
	public boolean isControlLens(ItemStack stack) {
		return (getProps(stack) & PROP_CONTROL) != 0;
	}

	@Override
	public boolean allowBurstShooting(ItemStack stack, IManaSpreader spreader, boolean redstone) {
		return lenses[stack.getItemDamage()].allowBurstShooting(stack, spreader, redstone);
	}

	@Override
	public void onControlledSpreaderTick(ItemStack stack, IManaSpreader spreader, boolean redstone) {
		lenses[stack.getItemDamage()].onControlledSpreaderTick(stack, spreader, redstone);
	}

	@Override
	public void onControlledSpreaderPulse(ItemStack stack, IManaSpreader spreader, boolean redstone) {
		lenses[stack.getItemDamage()].onControlledSpreaderPulse(stack, spreader, redstone);
	}

	@Override
	public int getProps(ItemStack stack) {
		return props[stack.getItemDamage()];
	}

	@Override
	public boolean isCombinable(ItemStack stack) {
		return stack.getItemDamage() != NORMAL;
	}
}
