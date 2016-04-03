/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Apr 14, 2014, 2:58:13 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.armor.terrasteel;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.client.model.armor.ModelArmorTerrasteel;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.armor.manasteel.ItemManasteelArmor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ItemTerrasteelArmor extends ItemManasteelArmor {

	public ItemTerrasteelArmor(EntityEquipmentSlot type, String name) {
		super(type, name, BotaniaAPI.terrasteelArmorMaterial);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped provideArmorModelForSlot(ItemStack stack, EntityEquipmentSlot slot) {
		models.put(slot, new ModelArmorTerrasteel(slot));
		return models.get(slot);
	}

	@Override
	public String getArmorTextureAfterInk(ItemStack stack, EntityEquipmentSlot slot) {
		return ConfigHandler.enableArmorModels ? LibResources.MODEL_TERRASTEEL_NEW : slot == EntityEquipmentSlot.CHEST ? LibResources.MODEL_TERRASTEEL_1 : LibResources.MODEL_TERRASTEEL_0;
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
		return par2ItemStack.getItem() == ModItems.manaResource && par2ItemStack.getItemDamage() == 4 || super.getIsRepairable(par1ItemStack, par2ItemStack);
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
		UUID uuid = new UUID((getUnlocalizedName() + slot.toString()).hashCode(), 0);
		if (slot == this.armorType) {
			multimap.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getAttributeUnlocalizedName(), new AttributeModifier(uuid, "Terrasteel modifier " + type, (double) getArmorDisplay(null, new ItemStack(this), type.getIndex()) / 20, 0));
		}
		return multimap;
	}

	static ItemStack[] armorset;

	@Override
	public ItemStack[] getArmorSetStacks() {
		if(armorset == null)
			armorset = new ItemStack[] {
				new ItemStack(ModItems.terrasteelHelm),
				new ItemStack(ModItems.terrasteelChest),
				new ItemStack(ModItems.terrasteelLegs),
				new ItemStack(ModItems.terrasteelBoots)
		};

		return armorset;
	}

	@Override
	public boolean hasArmorSetItem(EntityPlayer player, int i) {
		ItemStack stack = player.inventory.armorInventory[3 - i];
		if(stack == null)
			return false;

		switch(i) {
		case 0: return stack.getItem() == ModItems.terrasteelHelm || stack.getItem() == ModItems.terrasteelHelmRevealing;
		case 1: return stack.getItem() == ModItems.terrasteelChest;
		case 2: return stack.getItem() == ModItems.terrasteelLegs;
		case 3: return stack.getItem() == ModItems.terrasteelBoots;
		}

		return false;
	}

	@Override
	public String getArmorSetName() {
		return I18n.translateToLocal("botania.armorset.terrasteel.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<String> list) {
		addStringToTooltip(I18n.translateToLocal("botania.armorset.terrasteel.desc0"), list);
		addStringToTooltip(I18n.translateToLocal("botania.armorset.terrasteel.desc1"), list);
		addStringToTooltip(I18n.translateToLocal("botania.armorset.terrasteel.desc2"), list);
	}

}
