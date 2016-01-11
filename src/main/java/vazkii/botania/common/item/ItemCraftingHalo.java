/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Dec 15, 2014, 3:53:30 PM (GMT)]
 */
package vazkii.botania.common.item;

import java.awt.Color;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.gui.crafting.InventoryCraftingHalo;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.Botania;
import vazkii.botania.common.achievement.ICraftAchievement;
import vazkii.botania.common.achievement.ModAchievements;
import vazkii.botania.common.core.helper.InventoryHelper;
import vazkii.botania.common.core.helper.InventoryHelper.GenericInventory;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.lib.LibGuiIDs;
import vazkii.botania.common.lib.LibItemNames;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.common.lib.LibObfuscation;

public class ItemCraftingHalo extends ItemMod implements ICraftAchievement {

	private static final ResourceLocation glowTexture = new ResourceLocation(LibResources.MISC_GLOW_GREEN);
	private static final ItemStack craftingTable = new ItemStack(Blocks.crafting_table);

	public static final int SEGMENTS = 12;

	private static final String TAG_LAST_CRAFTING = "lastCrafting";
	private static final String TAG_STORED_RECIPE_PREFIX = "storedRecipe";
	private static final String TAG_ITEM_PREFIX = "item";
	private static final String TAG_EQUIPPED = "equipped";
	private static final String TAG_ROTATION_BASE = "rotationBase";

	public ItemCraftingHalo() {
		this(LibItemNames.CRAFTING_HALO);
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}

	public ItemCraftingHalo(String name) {
		setUnlocalizedName(name);
		setMaxStackSize(1);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		int segment = getSegmentLookedAt(stack, player);
		ItemStack itemForPos = getItemForSlot(stack, segment);

		if(segment == 0)
			player.openGui(Botania.instance, LibGuiIDs.CRAFTING_HALO, world, 0, 0, 0);
		else {
			if(itemForPos == null)
				assignRecipe(stack, itemForPos, segment);
			else tryCraft(player, stack, segment, true, getFakeInv(player), true);
		}

		return stack;
	}

	public static IInventory getFakeInv(EntityPlayer player) {
		GenericInventory tempInv = new GenericInventory("temp", false, player.inventory.getSizeInventory() - 4);
		tempInv.copyFrom(player.inventory);
		return tempInv;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int pos, boolean equipped) {
		boolean eqLastTick = wasEquipped(stack);
		if(eqLastTick != equipped)
			setEquipped(stack, equipped);

		if(!equipped && entity instanceof EntityLivingBase) {
			int angles = 360;
			int segAngles = angles / SEGMENTS;
			float shift = segAngles / 2;
			setRotationBase(stack, getCheckingAngle((EntityLivingBase) entity) - shift);
		}
	}

	void tryCraft(EntityPlayer player, ItemStack stack, int slot, boolean particles, IInventory inv, boolean validate) {
		ItemStack itemForPos = getItemForSlot(stack, slot);
		if(itemForPos == null)
			return;

		ItemStack[] recipe = getCraftingItems(stack, slot);
		if(validate)
			recipe = validateRecipe(player, stack, recipe, slot);

		if(canCraft(player, recipe, inv))
			doCraft(player, recipe, particles);
	}

	private static ItemStack[] validateRecipe(EntityPlayer player, ItemStack stack, ItemStack[] recipe, int slot) {
		InventoryCrafting fakeInv = new InventoryCrafting(new ContainerWorkbench(player.inventory, player.worldObj, BlockPos.ORIGIN), 3, 3);
		for(int i = 0; i < 9; i++)
			fakeInv.setInventorySlotContents(i, recipe[i]);

		ItemStack result = CraftingManager.getInstance().findMatchingRecipe(fakeInv, player.worldObj);
		if(result == null) {
			assignRecipe(stack, recipe[9], slot);
			return null;
		}

		if(!result.isItemEqual(recipe[9]) || result.stackSize != recipe[9].stackSize || !ItemStack.areItemStackTagsEqual(recipe[9], result)) {
			assignRecipe(stack, recipe[9], slot);
			return null;
		}

		return recipe;
	}

	private static boolean canCraft(EntityPlayer player, ItemStack[] recipe, IInventory inv) {
		if(recipe == null)
			return false;

		if(recipe[9].stackSize != InventoryHelper.testInventoryInsertion(inv, recipe[9], null))
			return false;

		return consumeRecipeIngredients(recipe, inv, null);
	}

	private static void doCraft(EntityPlayer player, ItemStack[] recipe, boolean particles) {
		consumeRecipeIngredients(recipe, player.inventory, player);
		if(!player.inventory.addItemStackToInventory(recipe[9]))
			player.dropPlayerItemWithRandomChoice(recipe[9], false);

		if(!particles)
			return;

		Vec3 lookVec3 = player.getLookVec();
		Vector3 centerVector = Vector3.fromEntityCenter(player).add(lookVec3.xCoord * 3, 1.3, lookVec3.zCoord * 3);
		float m = 0.1F;
		for(int i = 0; i < 4; i++)
			Botania.proxy.wispFX(player.worldObj, centerVector.x, centerVector.y, centerVector.z, 1F, 0F, 1F, 0.2F + 0.2F * (float) Math.random(), ((float) Math.random() - 0.5F) * m, ((float) Math.random() - 0.5F) * m, ((float) Math.random() - 0.5F) * m);
	}

	private static boolean consumeRecipeIngredients(ItemStack[] recipe, IInventory inv, EntityPlayer player) {
		for(int i = 0; i < 9; i++) {
			ItemStack ingredient = recipe[i];
			if(ingredient != null && !consumeFromInventory(ingredient, inv, player))
				return false;
		}

		return true;
	}

	private static boolean consumeFromInventory(ItemStack stack, IInventory inv, EntityPlayer player) {
		for(int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stackAt = inv.getStackInSlot(i);
			if(stackAt != null && stack.isItemEqual(stackAt) && ItemStack.areItemStackTagsEqual(stack, stackAt)) {
				boolean consume = true;

				ItemStack container = stackAt.getItem().getContainerItem(stackAt);
				if(container != null) {
					if(container == stackAt)
						consume = false;
					else {
						InventoryHelper.insertItemIntoInventory(inv, container);
						if(container.stackSize != 0 && player != null)
							player.dropPlayerItemWithRandomChoice(container, false);
					}
				}

				if(consume) {
					stackAt.stackSize--;
					if(stackAt.stackSize == 0)
						inv.setInventorySlotContents(i, null);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onEntitySwing(EntityLivingBase player, ItemStack stack) {
		int segment = getSegmentLookedAt(stack, player);
		if(segment == 0)
			return false;

		ItemStack itemForPos = getItemForSlot(stack, segment);

		if(itemForPos != null && player.isSneaking()) {
			assignRecipe(stack, itemForPos, segment);
			return true;
		}

		return false;
	}

	private static int getSegmentLookedAt(ItemStack stack, EntityLivingBase player) {
		getRotationBase(stack);
		float yaw = getCheckingAngle(player, getRotationBase(stack));

		int angles = 360;
		int segAngles = angles / SEGMENTS;
		for(int seg = 0; seg < SEGMENTS; seg++) {
			float calcAngle = (float) seg * segAngles;
			if(yaw >= calcAngle && yaw < calcAngle + segAngles)
				return seg;
		}
		return -1;
	}

	private static float getCheckingAngle(EntityLivingBase player) {
		return getCheckingAngle(player, 0F);
	}

	// Screw the way minecraft handles rotation
	// Really...
	private static float getCheckingAngle(EntityLivingBase player, float base) {
		float yaw = MathHelper.wrapAngleTo180_float(player.rotationYaw) + 90F;
		int angles = 360;
		int segAngles = angles / SEGMENTS;
		float shift = segAngles / 2;

		if(yaw < 0)
			yaw = 180F + (180F + yaw);
		yaw -= 360F - base;
		float angle = 360F - yaw + shift;

		if(angle < 0)
			angle = 360F + angle;

		return angle;
	}

	public static ItemStack getItemForSlot(ItemStack stack, int slot) {
		if(slot == 0)
			return craftingTable;
		else if(slot >= SEGMENTS)
			return null;
		else {
			NBTTagCompound cmp = getStoredRecipeCompound(stack, slot);

			if(cmp != null) {
				ItemStack cmpStack = getLastCraftingItem(cmp, 9);
				return cmpStack;
			} else return null;
		}
	}

	public static void assignRecipe(ItemStack stack, ItemStack itemForPos, int pos) {
		if(itemForPos != null)
			ItemNBTHelper.setCompound(stack, TAG_STORED_RECIPE_PREFIX + pos, new NBTTagCompound());
		else
			ItemNBTHelper.setCompound(stack, TAG_STORED_RECIPE_PREFIX + pos, getLastCraftingCompound(stack, false));
	}

	@SubscribeEvent
	public void onItemCrafted(ItemCraftedEvent event) {
		if(!(event.craftMatrix instanceof InventoryCraftingHalo))
			return;

		for(int i = 0; i < event.player.inventory.getSizeInventory(); i++) {
			ItemStack stack = event.player.inventory.getStackInSlot(i);
			if(stack != null && stack.getItem() instanceof ItemCraftingHalo)
				saveRecipeToStack(event, stack);
		}
	}

	private void saveRecipeToStack(ItemCraftedEvent event, ItemStack stack) {
		NBTTagCompound cmp = new NBTTagCompound();
		NBTTagCompound cmp1 = new NBTTagCompound();

		ItemStack result = CraftingManager.getInstance().findMatchingRecipe((InventoryCrafting) event.craftMatrix, event.player.worldObj);
		if(result != null) {
			result.writeToNBT(cmp1);
			cmp.setTag(TAG_ITEM_PREFIX + 9, cmp1);

			for(int i = 0; i < 9; i++) {
				cmp1 = new NBTTagCompound();
				ItemStack stackSlot = event.craftMatrix.getStackInSlot(i);

				if(stackSlot != null) {
					ItemStack writeStack = stackSlot.copy();
					writeStack.stackSize = 1;
					writeStack.writeToNBT(cmp1);
				}
				cmp.setTag(TAG_ITEM_PREFIX + i, cmp1);
			}
		}

		ItemNBTHelper.setCompound(stack, TAG_LAST_CRAFTING, cmp);
	}

	public static ItemStack[] getLastCraftingItems(ItemStack stack) {
		return getCraftingItems(stack, SEGMENTS);
	}

	public static ItemStack[] getCraftingItems(ItemStack stack, int slot) {
		ItemStack[] stackArray = new ItemStack[10];

		NBTTagCompound cmp = getStoredRecipeCompound(stack, slot);
		if(cmp != null)
			for(int i = 0; i < stackArray.length; i++)
				stackArray[i] = getLastCraftingItem(cmp, i);

		return stackArray;
	}

	public static NBTTagCompound getLastCraftingCompound(ItemStack stack, boolean nullify) {
		return ItemNBTHelper.getCompound(stack, TAG_LAST_CRAFTING, nullify);
	}

	public static NBTTagCompound getStoredRecipeCompound(ItemStack stack, int slot) {
		return slot == SEGMENTS ? getLastCraftingCompound(stack, true) : ItemNBTHelper.getCompound(stack, TAG_STORED_RECIPE_PREFIX + slot, true);
	}

	public static ItemStack getLastCraftingItem(ItemStack stack, int pos) {
		return getLastCraftingItem(getLastCraftingCompound(stack, true), pos);
	}

	public static ItemStack getLastCraftingItem(NBTTagCompound cmp, int pos) {
		if(cmp == null)
			return null;

		NBTTagCompound cmp1 = cmp.getCompoundTag(TAG_ITEM_PREFIX + pos);
		if(cmp1 == null)
			return null;

		return ItemStack.loadItemStackFromNBT(cmp1);
	}

	public static boolean wasEquipped(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_EQUIPPED, false);
	}

	public static void setEquipped(ItemStack stack, boolean equipped) {
		ItemNBTHelper.setBoolean(stack, TAG_EQUIPPED, equipped);
	}

	public static float getRotationBase(ItemStack stack) {
		return ItemNBTHelper.getFloat(stack, TAG_ROTATION_BASE, 0F);
	}

	public static void setRotationBase(ItemStack stack, float rotation) {
		ItemNBTHelper.setFloat(stack, TAG_ROTATION_BASE, rotation);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		ItemStack stack = player.getCurrentEquippedItem();
		if(stack != null && stack.getItem() instanceof ItemCraftingHalo)
			render(stack, player, event.partialTicks);
	}

	private double getRenderPosX() { // todo 1.8
		return ObfuscationReflectionHelper.getPrivateValue(RenderManager.class, Minecraft.getMinecraft().getRenderManager(), LibObfuscation.RENDERPOSX);
	}

	private double getRenderPosY() {
		return ObfuscationReflectionHelper.getPrivateValue(RenderManager.class, Minecraft.getMinecraft().getRenderManager(), LibObfuscation.RENDERPOSY);
	}

	private double getRenderPosZ() {
		return ObfuscationReflectionHelper.getPrivateValue(RenderManager.class, Minecraft.getMinecraft().getRenderManager(), LibObfuscation.RENDERPOSZ);
	}

	@SideOnly(Side.CLIENT)
	public void render(ItemStack stack, EntityPlayer player, float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator tess = Tessellator.getInstance();
		// todo 1.8.8 Tessellator.renderingWorldRenderer = false;

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		float alpha = ((float) Math.sin((ClientTickHandler.ticksInGame + partialTicks) * 0.2F) * 0.5F + 0.5F) * 0.4F + 0.3F;

		double posX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
		double posY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
		double posZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

		GlStateManager.translate(posX - getRenderPosX(), posY - getRenderPosY() + player.getDefaultEyeHeight(), posZ - getRenderPosZ());


		float base = getRotationBase(stack);
		int angles = 360;
		int segAngles = angles / SEGMENTS;
		float shift = base - segAngles / 2;

		float u = 1F;
		float v = 0.25F;

		float s = 3F;
		float m = 0.8F;
		float y = v * s * 2;
		float y0 = 0;

		int segmentLookedAt = getSegmentLookedAt(stack, player);

		for(int seg = 0; seg < SEGMENTS; seg++) {
			boolean inside = false;
			float rotationAngle = (seg + 0.5F) * segAngles + shift;
			GlStateManager.pushMatrix();
			GlStateManager.rotate(rotationAngle, 0F, 1F, 0F);
			GlStateManager.translate(s * m, -0.75F, 0F);

			if(segmentLookedAt == seg)
				inside = true;

			ItemStack slotStack = getItemForSlot(stack, seg);
			if(slotStack != null) {
				mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

				if(slotStack.getItem() instanceof ItemBlock) {
					float scale = seg == 0 ? 0.75F : 0.6F;
					GlStateManager.scale(scale, scale, scale);
					GlStateManager.rotate(180F, 0F, 1F, 0F);
					GlStateManager.translate(seg == 0 ? 0.5F : 0F, seg == 0 ? -0.1F : 0.6F, 0F);
					GlStateManager.translate(0, -0.5, 0.5F); // todo 1.8 refine

					IBlockState state = ((ItemBlock) slotStack.getItem()).block.getStateFromMeta(slotStack.getItemDamage());
					Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(state, 1.0F);
				} else {
					GlStateManager.scale(0.75F, 0.75F, 0.75F);
					GlStateManager.rotate(90F, 0F, 1F, 0F);
					Minecraft.getMinecraft().getRenderItem().renderItem(slotStack, ItemCameraTransforms.TransformType.GUI);
				}
			}
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.rotate(180F, 1F, 0F, 0F);
			float a = alpha;
			if(inside) {
				a += 0.3F;
				y0 = -y;
			}

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			if(seg % 2 == 0)
				GlStateManager.color(0.6F, 0.6F, 0.6F, a);
			else GlStateManager.color(1F, 1F, 1F, a);

			GlStateManager.disableCull();
			ItemCraftingHalo item = (ItemCraftingHalo) stack.getItem();
			mc.renderEngine.bindTexture(item.getGlowResource());
			tess.getWorldRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			for(int i = 0; i < segAngles; i++) {
				float ang = i + seg * segAngles + shift;
				double xp = Math.cos(ang * Math.PI / 180F) * s;
				double zp = Math.sin(ang * Math.PI / 180F) * s;

				tess.getWorldRenderer().pos(xp * m, y, zp * m).tex(u, v).endVertex();
				tess.getWorldRenderer().pos(xp, y0, zp).tex(u, 0).endVertex();

				xp = Math.cos((ang + 1) * Math.PI / 180F) * s;
				zp = Math.sin((ang + 1) * Math.PI / 180F) * s;

				tess.getWorldRenderer().pos(xp, y0, zp).tex(0, 0).endVertex();
				tess.getWorldRenderer().pos(xp * m, y, zp * m).tex(0, v).endVertex();
			}
			y0 = 0;
			tess.draw();
			GlStateManager.enableCull();
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
	}

	@SideOnly(Side.CLIENT)
	public ResourceLocation getGlowResource() {
		return glowTexture;
	}

	@SideOnly(Side.CLIENT)
	public static void renderHUD(ScaledResolution resolution, EntityPlayer player, ItemStack stack) {
		Minecraft mc = Minecraft.getMinecraft();
		int slot = getSegmentLookedAt(stack, player);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		if(slot == 0) {
			String name = craftingTable.getDisplayName();
			int l = mc.fontRendererObj.getStringWidth(name);
			int x = resolution.getScaledWidth() / 2 - l / 2;
			int y = resolution.getScaledHeight() / 2 - 65;

			Gui.drawRect(x - 6, y - 6, x + l + 6, y + 37, 0x22000000);
			Gui.drawRect(x - 4, y - 4, x + l + 4, y + 35, 0x22000000);
			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.enableRescaleNormal();
			mc.getRenderItem().renderItemAndEffectIntoGUI(craftingTable, resolution.getScaledWidth() / 2 - 8, resolution.getScaledHeight() / 2 - 52);
			net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();

			mc.fontRendererObj.drawStringWithShadow(name, x, y, 0xFFFFFF);
		} else {
			ItemStack[] recipe = getCraftingItems(stack, slot);
			String label = StatCollector.translateToLocal("botaniamisc.unsetRecipe");
			boolean setRecipe = false;

			if(recipe[9] == null)
				recipe = getCraftingItems(stack, SEGMENTS);
			else {
				label = recipe[9].getDisplayName();
				setRecipe = true;
			}

			renderRecipe(resolution, label, recipe, player, setRecipe);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void renderRecipe(ScaledResolution resolution, String label, ItemStack[] recipe, EntityPlayer player, boolean setRecipe) {
		Minecraft mc = Minecraft.getMinecraft();

		if(recipe[9] != null) {
			int x = resolution.getScaledWidth() / 2 - 45;
			int y = resolution.getScaledHeight() / 2 - 90;

			Gui.drawRect(x - 6, y - 6, x + 90 + 6, y + 60, 0x22000000);
			Gui.drawRect(x - 4, y - 4, x + 90 + 4, y + 58, 0x22000000);

			Gui.drawRect(x + 66, y + 14, x + 92, y + 40, 0x22000000);
			Gui.drawRect(x - 2, y - 2, x + 56, y + 56, 0x22000000);

			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.enableRescaleNormal();
			for(int i = 0; i < 9; i++) {
				ItemStack stack = recipe[i];
				if(stack != null) {
					int xpos = x + i % 3 * 18;
					int ypos = y + i / 3 * 18;
					Gui.drawRect(xpos, ypos, xpos + 16, ypos + 16, 0x22000000);

					mc.getRenderItem().renderItemAndEffectIntoGUI(stack, xpos, ypos);
				}
			}

			mc.getRenderItem().renderItemAndEffectIntoGUI(recipe[9], x + 72, y + 18);
			mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, recipe[9], x + 72, y + 18, "");

			net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		}

		int yoff = 110;
		if(setRecipe && !canCraft(player, recipe, getFakeInv(player))) {
			String warning = EnumChatFormatting.RED + StatCollector.translateToLocal("botaniamisc.cantCraft");
			mc.fontRendererObj.drawStringWithShadow(warning, resolution.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(warning) / 2, resolution.getScaledHeight() / 2 - yoff, 0xFFFFFF);
			yoff += 12;
		}

		mc.fontRendererObj.drawStringWithShadow(label, resolution.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(label) / 2, resolution.getScaledHeight() / 2 - yoff, 0xFFFFFF);
	}

	@Override
	public Achievement getAchievementOnCraft(ItemStack stack, EntityPlayer player, IInventory matrix) {
		return ModAchievements.craftingHaloCraft;
	}

}
