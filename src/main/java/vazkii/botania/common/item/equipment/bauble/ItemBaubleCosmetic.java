/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Feb 22, 2015, 2:01:01 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

import org.lwjgl.opengl.GL11;

import vazkii.botania.api.item.ICosmeticBauble;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.crafting.recipe.CosmeticAttachRecipe;
import vazkii.botania.common.crafting.recipe.CosmeticRemoveRecipe;
import vazkii.botania.common.lib.LibItemNames;
import baubles.api.BaubleType;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemBaubleCosmetic extends ItemBauble implements ICosmeticBauble {

	public static final int SUBTYPES = 32;

	public ItemBaubleCosmetic() {
		super(LibItemNames.COSMETIC);
		setHasSubtypes(true);

		GameRegistry.addRecipe(new CosmeticAttachRecipe());
		GameRegistry.addRecipe(new CosmeticRemoveRecipe());
		RecipeSorter.register("botania:cosmeticAttach", CosmeticAttachRecipe.class, Category.SHAPELESS, "");
		RecipeSorter.register("botania:cosmeticRemove", CosmeticRemoveRecipe.class, Category.SHAPELESS, "");
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for(int i = 0; i < SUBTYPES; i++)
			list.add(new ItemStack(item, 1, i));
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		return super.getUnlocalizedName(par1ItemStack) + par1ItemStack.getItemDamage();
	}

	@Override
	public void addHiddenTooltip(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		addStringToTooltip(StatCollector.translateToLocal("botaniamisc.cosmeticBauble"), par3List);
		super.addHiddenTooltip(par1ItemStack, par2EntityPlayer, par3List, par4);
	}

	@Override
	public BaubleType getBaubleType(ItemStack arg0) {
		return BaubleType.AMULET;
	}

	@Override
	public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, float partialTicks, RenderType type) {
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		if(type == RenderType.HEAD) {
			Helper.translateToHeadLevel(player);
			switch(stack.getItemDamage()) {
			case 2:
				faceTranslate();
				scale(0.75F);
				GlStateManager.translate(0.04F, -2.45F, 0F);
				renderIcon(stack);
				break;
			case 4:
				faceTranslate();
				scale(0.75F);
				GlStateManager.translate(0.04F, -2.45F, 0F);
				renderIcon(stack);
				break;
			case 5:
				faceTranslate();
				scale(0.35F);
				GlStateManager.scale(-1f, 1f, 1f);
				GlStateManager.translate(0.35F, -5.25F, 0F);
				renderIcon(stack);
				break;
			case 6:
				faceTranslate();
				scale(0.35F);
				GlStateManager.translate(0.35F, -5.25F, 0F);
				renderIcon(stack);
				break;
			case 7:
				faceTranslate();
				scale(0.6F);
				GlStateManager.translate(0.1F, -2.25F, 0.6F);
				renderIcon(stack);
				break;
			case 8:
				faceTranslate();
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				scale(0.6F);
				GlStateManager.scale(-1F, 1F, 1F);
				GlStateManager.translate(0.5F, -2.6F, -0.5F);
				renderIcon(stack);
				break;
			case 9:
				faceTranslate();
				GlStateManager.pushMatrix();
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				scale(0.6F);
				GlStateManager.translate(-0.5F, -2.75F, -0.5F);
				renderIcon(stack);
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				scale(0.6F);
				GlStateManager.translate(-0.5F, -2.75F, 0.5F);
				renderIcon(stack);
				GlStateManager.popMatrix();
				break;
			case 10:
				faceTranslate();
				GlStateManager.pushMatrix();
				GlStateManager.rotate(90F, 0F, -1F, 0F);
				scale(0.4F);
				GlStateManager.translate(1F, -4.1F, 1F);
				GlStateManager.rotate(145F, 0F, 1F, 0F);
				renderIcon(stack);
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.rotate(90F, 0F, -1F, 0F);
				scale(0.4F);
				GlStateManager.translate(0.25F, -4.1F, 1F);
				GlStateManager.rotate(35F, 0F, 1F, 0F);
				renderIcon(stack);
				GlStateManager.popMatrix();
				break;
			case 11:
				faceTranslate();
				scale(0.6F);
				GlStateManager.translate(0.05F, -2.7F, 0.6F);
				renderIcon(stack);
				break;
			case 15:
				faceTranslate();
				GlStateManager.scale(-1F, 1F, 1F);
				GlStateManager.translate(-0.025F, -1.9F, 0F);
				renderIcon(stack);
				break;
			case 17:
				faceTranslate();
				scale(0.35F);
				GlStateManager.scale(-1F, 1F, 1F);
				GlStateManager.translate(0.3F, -5.4F, 0F);
				renderIcon(stack);
				break;
			case 18:
				faceTranslate();
				scale(0.75F);
				GlStateManager.scale(-1F, 1F, 1F);
				GlStateManager.rotate(90F, 0F, -1F, 0F);
				GlStateManager.translate(0F, -2F, 0.025F);
				renderIcon(stack);
				break;
			case 19:
				faceTranslate();
				scale(0.6F);
				GlStateManager.translate(0.05F, -2.775F, 0.15F);
				renderIcon(stack);
				break;
			case 20:
				faceTranslate();
				GlStateManager.pushMatrix();
				scale(0.25F);
				GlStateManager.translate(-0.55F, -6.5F, -0.1F);
				renderIcon(stack);
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				scale(0.25F);
				GlStateManager.translate(0.8F, -6.5F, -0.1F);
				renderIcon(stack);
				GlStateManager.popMatrix();
				break;
			case 22:
				faceTranslate();
				scale(0.75F);
				GlStateManager.translate(0.04F, -2.35F, 0F);
				renderIcon(stack);
				break;
			case 23:
				faceTranslate();
				scale(0.75F);
				GlStateManager.translate(0.04F, -2.35F, 0F);
				renderIcon(stack);
				break;
			case 24:
				faceTranslate();
				scale(0.6F);
				GlStateManager.scale(-1F, 1F, 1F);
				GlStateManager.translate(0.25F, -2.4F, 0.1F);
				GlStateManager.rotate(-60F, 0F, 0F, 1F);
				renderIcon(stack);
				break;
			case 25:
				faceTranslate();
				scale(0.75F);
				GlStateManager.translate(0.04F, -2.45F, 0F);
				renderIcon(stack);
				break;
			case 26:
				faceTranslate();
				GlStateManager.translate(0.025F, -1.85F, -0.01F);
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlStateManager.color(1F, 1F, 1F, 0.7F);
				renderIcon(stack);
				break;
			case 27:
				faceTranslate();
				scale(0.75F);
				GlStateManager.translate(0.04F, -2.6F, 0F);
				renderIcon(stack);
				break;
			case 28:
				faceTranslate();
				GlStateManager.pushMatrix();
				scale(0.25F);
				GlStateManager.translate(-0.35F, -7.1F, -0.1F);
				renderIcon(stack);
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				scale(0.25F);
				GlStateManager.scale(-1F, 1F, 1F);
				GlStateManager.translate(-0.55F, -7.1F, -0.1F);
				renderIcon(stack);
				GlStateManager.popMatrix();
				break;
			case 30:
				faceTranslate();
				scale(0.75F);
				GlStateManager.translate(0.04F, -2.35F, 0F);
				renderIcon(stack);
				break;
			case 31:
				faceTranslate();
				scale(0.5F);
				GlStateManager.translate(0F, -2.45F, 0.5F);
				renderIcon(stack);
				break;
			}
		} else {
			Helper.rotateIfSneaking(player);
			switch(stack.getItemDamage()) {
			case 0:
				chestTranslate();
				scale(0.5F);
				GlStateManager.translate(0.5F, 0.7F, 0F);
				renderIcon(stack);
				break;
			case 1:
				chestTranslate();
				scale(0.75F);
				GlStateManager.translate(0.35F, 0.15F, 0F);
				renderIcon(stack);
				break;
			case 3:
				chestTranslate();
				scale(0.6F);
				GlStateManager.translate(0.45F, 0.3F, 0F);
				renderIcon(stack);
				break;
			case 12:
				chestTranslate();
				scale(0.225F);
				GlStateManager.translate(0.7F, 1.5F, 0F);
				renderIcon(stack);
				break;
			case 13:
				chestTranslate();
				GlStateManager.rotate(-90F, 0F, 1F, 0F);
				scale(0.5F);
				GlStateManager.scale(-1F, 1F, 1F);
				GlStateManager.translate(0.8F, -0.4F, -0.5F);
				renderIcon(stack);
				break;
			case 14:
				chestTranslate();
				GlStateManager.pushMatrix();
				scale(0.5F);
				GlStateManager.scale(-1F, 1F, 1F);
				GlStateManager.translate(-1.3F, 1F, -0.05F);
				GlStateManager.rotate(180F, 0F, 1F, 0F);
				renderIcon(stack);
				GlStateManager.popMatrix();
				// OH GOD, I had to do this mess because using color on renderItem doesn't wok
				// SEND HELP!
				GlStateManager.pushMatrix();
				scale(0.5F);
				GlStateManager.scale(-1F, 1F, 1F);
				GlStateManager.rotate(180F, 0F, 1F, 0F);
				GlStateManager.translate(-0.8F, 0.5F, -0.05F);
				GlStateManager.color(0F, 0F, 0.3F, 1F);
				IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
				TextureAtlasSprite itemIcon = model.getParticleTexture();
				float f = itemIcon.getMinU();
				float f1 = itemIcon.getMaxU();
				float f2 = itemIcon.getMinV();
				float f3 = itemIcon.getMaxV();
				IconHelper.renderIconIn3D(Tessellator.getInstance(), f1, f2, f, f3, itemIcon.getIconWidth(), itemIcon.getIconHeight(), 1F / 16F);
				GlStateManager.popMatrix();
				break;
			case 16:
				chestTranslate();
				scale(0.225F);
				GlStateManager.translate(1.65F, 1.5F, 0F);
				renderIcon(stack);
				break;
			case 21:
				chestTranslate();
				scale(0.3F);
				GlStateManager.translate(0.85F, 0.5F, 0F);
				renderIcon(stack);
				break;
			case 29:
				chestTranslate();
				scale(0.8F);
				GlStateManager.translate(0.3F, 0.1F, -0.35F);
				GlStateManager.rotate(10F, 0F, 0F, 1F);
				renderIcon(stack);
				break;
			}
		}
	}

	public void faceTranslate() {
		GlStateManager.rotate(90F, 0F, 1F, 0F);
		GlStateManager.rotate(180F, 1F, 0F, 0F);
		GlStateManager.translate(-0.025F, 0.45F, -0.3F);
	}

	public void chestTranslate() {
		GlStateManager.rotate(180F, 1F, 0F, 0F);
		GlStateManager.translate(-0.25F, -0.5F, 0.15F);
	}

	public void scale(float f) {
		GlStateManager.scale(f, f, f);
	}

	public void renderIcon(ItemStack stack) {
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
	}
}
